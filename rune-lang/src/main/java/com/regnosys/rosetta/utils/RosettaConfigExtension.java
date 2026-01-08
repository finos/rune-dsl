package com.regnosys.rosetta.utils;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.RosettaQualifiableConfiguration;
import com.regnosys.rosetta.rosetta.RosettaQualifiableType;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.simple.Data;
import jakarta.inject.Inject;
import java.util.Objects;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceDescriptionsProvider;

public class RosettaConfigExtension {

	@Inject
	private IResourceDescriptionsProvider index;

	@Inject
	private RosettaEcoreUtil rosettaEcoreUtil;

	public boolean isRootEventOrProduct(RosettaType type) {
		return type.getName() != null && (Objects.equals(type, findEventRootName(type)) || Objects.equals(type, findProductRootName(type)));
	}

	public Data findProductRootName(EObject ctx) {
		return findRosettaQualifiableConfiguration(ctx, RosettaQualifiableType.PRODUCT);
	}

	public Data findEventRootName(EObject ctx) {
		return findRosettaQualifiableConfiguration(ctx, RosettaQualifiableType.EVENT);
	}

	//TODO: remove metaTypes from the model and then we can stop relying on this
	@Deprecated
	public Iterable<IEObjectDescription> findMetaTypes(Iterable<String> names, EObject ctx) {
		IResourceDescriptions descriptions = index.getResourceDescriptions(ctx.eResource().getResourceSet());
		return Iterables.filter(
				Iterables.concat(
						Iterables.transform(
								names,
								name -> descriptions.getExportedObjects(RosettaPackage.Literals.ROSETTA_META_TYPE, QualifiedName.create(name), false)
						)), 
				it -> isProjectLocal(ctx.eResource().getURI(), it.getEObjectURI())
		);
	}

	public boolean isProjectLocal(URI platformResourceURI, URI candidateUri) {
		if (!platformResourceURI.isPlatformResource()) {
			// synthetic tests URI
			return true;
		}
		String projectName = platformResourceURI.segment(1);
		if (candidateUri.isPlatformResource()) {
			return Objects.equals(projectName, candidateUri.segment(1));
		}
		return false;
	}

	/**
	 * Can return <code>null</code> if any found
	 * @param ctx Context to resolve proxies
	 * @param type type for look up EVENT or PRODUCT
	 *
	 * @returns a class name which is configured as root for the passed <code>RosettaQualifiableType</code>
	 */
	private Data findRosettaQualifiableConfiguration(EObject ctx, RosettaQualifiableType type) {
		Iterable<IEObjectDescription> filtered = Iterables.filter(
			index.getResourceDescriptions(ctx.eResource().getResourceSet())
				.getExportedObjectsByType(RosettaPackage.Literals.ROSETTA_QUALIFIABLE_CONFIGURATION),
			it -> isProjectLocal(ctx.eResource().getURI(), it.getEObjectURI())
		);
		Iterable<Data> mapped = Iterables.transform(filtered, it -> {
			EObject eObj = it.getEObjectOrProxy().eIsProxy()
				? EcoreUtil.resolve(it.getEObjectOrProxy(), ctx)
				: it.getEObjectOrProxy();
			if (rosettaEcoreUtil.isResolved(eObj) && type == ((RosettaQualifiableConfiguration) eObj).getQType()) {
				return ((RosettaQualifiableConfiguration) eObj).getRosettaClass();
			}
			return null;
		});
		return Iterables.getFirst(Iterables.filter(mapped, Objects::nonNull), null);
	}
}
