package com.regnosys.rosetta.resource;

import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.impl.DefaultResourceDescription;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.util.IResourceScopeCache;

import com.google.common.collect.Lists;

public class DefaultRosettaResourceDescription extends DefaultResourceDescription implements IRosettaResourceDescription {
	
	private final IResourceScopeCache cache;
	private final RosettaResourceDescriptionStrategy strategy;
	
	public DefaultRosettaResourceDescription(Resource resource, RosettaResourceDescriptionStrategy strategy, IResourceScopeCache cache) {
		super(resource, strategy, cache);
		this.cache = cache;
		this.strategy = strategy;
	}
	
	private static final String IMPLICIT_REFERENCE_DESCRIPTIONS_CACHE_KEY = DefaultImplicitReferenceDescription.class.getName()
			+ "#getImplicitReferenceDescriptions";
	
	public Iterable<IImplicitReferenceDescription> getImplicitReferenceDescriptions() {
		return cache.get(IMPLICIT_REFERENCE_DESCRIPTIONS_CACHE_KEY, getResource(), () -> computeImplicitReferenceDescriptions());
	}
	
	protected List<IImplicitReferenceDescription> computeImplicitReferenceDescriptions() {
		final List<IImplicitReferenceDescription> implicitReferenceDescriptions = Lists.newArrayList();
		IAcceptor<IImplicitReferenceDescription> acceptor = new IAcceptor<IImplicitReferenceDescription>() {
			@Override
			public void accept(IImplicitReferenceDescription implicitReferenceDescription) {
				implicitReferenceDescriptions.add(implicitReferenceDescription);
			}
		};
		TreeIterator<EObject> allProperContents = EcoreUtil.getAllProperContents(getResource(), false);
		while (allProperContents.hasNext()) {
			EObject content = allProperContents.next();
			if (!strategy.createImplicitReferenceDescriptions(content, acceptor))
				allProperContents.prune();
		}
		return implicitReferenceDescriptions;
	}
}
