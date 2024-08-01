package com.regnosys.rosetta.ide.symbol;

import static com.google.common.collect.Iterables.size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.lsp4j.Location;
import org.eclipse.xtext.findReferences.IReferenceFinder;
import org.eclipse.xtext.findReferences.ReferenceAcceptor;
import org.eclipse.xtext.findReferences.TargetURIs;
import org.eclipse.xtext.findReferences.IReferenceFinder.IResourceAccess;
import org.eclipse.xtext.ide.server.DocumentExtensions;
import org.eclipse.xtext.ide.server.symbol.DocumentSymbolService;
import org.eclipse.xtext.ide.util.CancelIndicatorProgressMonitor;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

import com.google.common.base.Predicate;
import com.regnosys.rosetta.resource.IImplicitReferenceDescription;
import com.regnosys.rosetta.resource.IRosettaResourceDescription;

@Singleton
public class RosettaDocumentSymbolService extends DocumentSymbolService {
	protected static final int MONITOR_CHUNK_SIZE = 100;
	
	@Inject
	private DocumentExtensions documentExtensions;
	@Inject
	private EObjectAtOffsetHelper eObjectAtOffsetHelper;
	@Inject
	private IReferenceFinder referenceFinder;
	@Inject
	private IResourceServiceProvider.Registry resourceServiceProviderRegistry;
	
	@Override
	public List<? extends Location> getReferences(XtextResource resource, int offset,
			IReferenceFinder.IResourceAccess resourceAccess, IResourceDescriptions indexData,
			CancelIndicator cancelIndicator) {
		EObject element = eObjectAtOffsetHelper.resolveElementAt(resource, offset);
		if (element == null) {
			return Collections.emptyList();
		}
		List<Location> locations = new ArrayList<>();
		TargetURIs targetURIs = collectTargetURIs(element);
		
		// TODO: join functionalities so we only iterate once over all resources
		Consumer<IImplicitReferenceDescription> acceptor = (IImplicitReferenceDescription implicitReference) -> {
			doRead(resourceAccess, implicitReference.getSourceEObjectUri(), (EObject obj) -> {
				Location location = documentExtensions.newLocation(obj);
				if (location != null) {
					locations.add(location);
				}
			});
		};
		findAllImplicitReferences(targetURIs, resourceAccess, indexData, acceptor, new CancelIndicatorProgressMonitor(cancelIndicator));

		referenceFinder.findAllReferences(targetURIs, resourceAccess, indexData,
				new ReferenceAcceptor(resourceServiceProviderRegistry, (IReferenceDescription reference) -> {
					doRead(resourceAccess, reference.getSourceEObjectUri(), (EObject obj) -> {
						Location location = documentExtensions.newLocation(obj, reference.getEReference(),
								reference.getIndexInList());
						if (location != null) {
							locations.add(location);
						}
					});
				}), new CancelIndicatorProgressMonitor(cancelIndicator));
		return locations;
	}
	
	private void findAllImplicitReferences(TargetURIs targetURIs, IResourceAccess resourceAccess,
			IResourceDescriptions indexData, Consumer<IImplicitReferenceDescription> acceptor, IProgressMonitor monitor) {
		if (!targetURIs.isEmpty()) {
			Iterable<IResourceDescription> allResourceDescriptions = indexData.getAllResourceDescriptions();
			SubMonitor subMonitor = SubMonitor.convert(monitor, size(allResourceDescriptions) / MONITOR_CHUNK_SIZE + 1);
			IProgressMonitor useMe = subMonitor.newChild(1);
			int i = 0;
			for (IResourceDescription resourceDescription : allResourceDescriptions) {
				if (subMonitor.isCanceled())
					throw new OperationCanceledException();
				if (resourceDescription instanceof IRosettaResourceDescription) {
					findImplicitReferences(targetURIs, (IRosettaResourceDescription) resourceDescription, resourceAccess, acceptor, useMe);
				}
				i++;
				if (i % MONITOR_CHUNK_SIZE == 0) {
					useMe = subMonitor.newChild(1);
				}
			}
		}
	}
	
	private void findImplicitReferences(final TargetURIs targetURIs, IRosettaResourceDescription resourceDescription,
			IResourceAccess resourceAccess, final Consumer<IImplicitReferenceDescription> acceptor, final IProgressMonitor monitor) {
//		final URI resourceURI = resourceDescription.getURI();
//		if (resourceAccess != null && targetURIs.containsResource(resourceURI)) {
//			IUnitOfWork.Void<ResourceSet> finder = new IUnitOfWork.Void<ResourceSet>() {
//				@Override
//				public void process(final ResourceSet state) throws Exception {
//					Resource resource = state.getResource(resourceURI, true);
//					findImplicitReferences(targetURIs, resource, acceptor, monitor);
//				}
//			};
//			resourceAccess.readOnly(resourceURI, finder);
//		} else {
			findImplicitReferencesInDescription(targetURIs, resourceDescription, resourceAccess, acceptor, monitor);
//		}
	}
	
	private void findImplicitReferencesInDescription(TargetURIs targetURIs, IRosettaResourceDescription resourceDescription,
			IResourceAccess resourceAccess, Consumer<IImplicitReferenceDescription> acceptor, IProgressMonitor monitor) {
		for (IImplicitReferenceDescription implicitReferenceDescription : resourceDescription.getImplicitReferenceDescriptions()) {
			if (targetURIs.contains(implicitReferenceDescription.getTargetEObjectUri())) {
				acceptor.accept(implicitReferenceDescription);
			}
		}
	}
	
	private void findImplicitReferences(Predicate<URI> targetURIs, Resource resource, Consumer<IImplicitReferenceDescription> acceptor, IProgressMonitor monitor) {
		for (EObject content : resource.getContents()) {
			findImplicitReferences(targetURIs, content, acceptor, monitor);
		}
	}
	
	private void findImplicitReferences(Predicate<URI> targetURIs, EObject scope, Consumer<IImplicitReferenceDescription> acceptor, IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
//		findLocalReferencesFromElement(targetURIs, scope, scope.eResource(), acceptor);
	}
	
//	protected void findLocalReferencesFromElement(
//			Predicate<URI> targetURIs, 
//			EObject sourceCandidate,
//			Resource localResource,
//			Acceptor acceptor) {
//		URI sourceURI = null;
//		for(EReference ref: sourceCandidate.eClass().getEAllReferences()) {
//			Object value = sourceCandidate.eGet(ref, false);
//			if(sourceCandidate.eIsSet(ref) && value != null) {
//				if(ref.isContainment()) {
//					if(ref.isMany()) {
//						@SuppressWarnings("unchecked")
//						InternalEList<EObject> contentList = (InternalEList<EObject>) value;
//						for(int i=0; i<contentList.size(); ++i) {
//							EObject childElement = contentList.basicGet(i);
//							if(!childElement.eIsProxy()) {
//								findLocalReferencesFromElement(targetURIs, childElement, localResource, acceptor);
//							}
//						}
//					} else {
//						EObject childElement = (EObject) value;
//						if(!childElement.eIsProxy()) {
//							findLocalReferencesFromElement(targetURIs, childElement, localResource, acceptor);
//						}
//					}
//				} else if (!ref.isContainer()) {
//					if (doProcess(ref, targetURIs)) {
//						if(ref.isMany()) {
//							@SuppressWarnings("unchecked")
//							InternalEList<EObject> values = (InternalEList<EObject>) value;
//							for(int i=0; i< values.size(); ++i) {
//								EObject instanceOrProxy = toValidInstanceOrNull(localResource, targetURIs, values.basicGet(i));
//								if (instanceOrProxy != null) {
//									URI refURI= EcoreUtil2.getPlatformResourceOrNormalizedURI(instanceOrProxy);
//									if(targetURIs.apply(refURI)) {
//										sourceURI = (sourceURI == null) ? EcoreUtil2.getPlatformResourceOrNormalizedURI(sourceCandidate) : sourceURI;
//										acceptor.accept(sourceCandidate, sourceURI, ref, i, instanceOrProxy, refURI);
//									}
//								}
//							}
//						} else {
//							EObject instanceOrProxy = toValidInstanceOrNull(localResource, targetURIs, (EObject) value);
//							if (instanceOrProxy != null) {
//								URI refURI = EcoreUtil2.getPlatformResourceOrNormalizedURI(instanceOrProxy);
//								if (targetURIs.apply(refURI)) {
//									sourceURI = (sourceURI == null) ? EcoreUtil2
//											.getPlatformResourceOrNormalizedURI(sourceCandidate) : sourceURI;
//									acceptor.accept(sourceCandidate, sourceURI, ref, -1, instanceOrProxy, refURI);
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//	}
}
