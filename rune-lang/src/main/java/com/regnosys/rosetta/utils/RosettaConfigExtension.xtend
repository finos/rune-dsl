package com.regnosys.rosetta.utils

import com.regnosys.rosetta.rosetta.RosettaQualifiableConfiguration
import com.regnosys.rosetta.rosetta.RosettaQualifiableType
import com.regnosys.rosetta.rosetta.RosettaType
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.resource.IResourceDescriptionsProvider

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import jakarta.inject.Inject
import org.eclipse.emf.common.util.URI
import com.regnosys.rosetta.RosettaEcoreUtil

class RosettaConfigExtension {

	@Inject IResourceDescriptionsProvider index
	@Inject extension RosettaEcoreUtil


	def isRootEventOrProduct(RosettaType type) {
		type.name !== null && (type == findEventRootName(type) || type == findProductRootName(type))
	}

	def findProductRootName(EObject ctx) {
		return findRosettaQualifiableConfiguration(ctx, RosettaQualifiableType.PRODUCT)
	}

	def findEventRootName(EObject ctx) {
		return findRosettaQualifiableConfiguration(ctx, RosettaQualifiableType.EVENT)
	}
	
	//TODO: remove metaTypes from the model and then we can stop relying on this
	@Deprecated
	def findMetaTypes(EObject ctx) {
		return index.getResourceDescriptions(ctx.eResource.resourceSet).getExportedObjectsByType(ROSETTA_META_TYPE).
			filter [
				isProjectLocal(ctx.eResource.URI, it.EObjectURI)
			]
	}
	
	def boolean isProjectLocal(URI platformResourceURI, URI candidateUri) {
		if (!platformResourceURI.isPlatformResource) {
			// synthetic tests URI
			return true
		}
		val projectName = platformResourceURI.segment(1)
		if (candidateUri.isPlatformResource) {
			return projectName == candidateUri.segment(1)
		}
		return false
	}
	
	/**
	 * Can return <code>null</code> if any found
	 * @param ctx Context to resolve proxies
	 * @param type type for look up EVENT or PRODUCT
	 * 
	 * @returns a class name which is configured as root for the passed <code>RosettaQualifiableType</code>
	 */
	def private findRosettaQualifiableConfiguration(EObject ctx, RosettaQualifiableType type) {
		return index.getResourceDescriptions(ctx.eResource.resourceSet).getExportedObjectsByType(
			ROSETTA_QUALIFIABLE_CONFIGURATION).filter[isProjectLocal(ctx.eResource.URI, it.EObjectURI)].map [
			val eObj = if(it.EObjectOrProxy.eIsProxy) EcoreUtil.resolve(it.EObjectOrProxy, ctx) else it.EObjectOrProxy
			if (eObj.isResolved && type === (eObj as RosettaQualifiableConfiguration).QType)
				(eObj as RosettaQualifiableConfiguration).rosettaClass
		].filterNull.head
	}

}
