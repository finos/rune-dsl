package com.regnosys.rosetta.resource

import java.util.Collection
import org.eclipse.xtext.resource.IResourceDescription.Delta
import org.eclipse.xtext.resource.IResourceDescription
import org.eclipse.xtext.resource.IResourceDescriptions
import org.eclipse.xtext.resource.DerivedStateAwareResourceDescriptionManager
import org.eclipse.xtext.util.RuntimeIOException
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.resource.DerivedStateAwareResource
import org.eclipse.xtext.resource.IDefaultResourceDescriptionStrategy
import java.io.IOException
import org.eclipse.xtext.resource.IEObjectDescription

class RosettaResourceDescriptionManager extends DerivedStateAwareResourceDescriptionManager {

	/**
	 * Need to re-validate all the resources with isEvent or isProduct when the root class configuration 'isEvent root' or 'isProduct root' changes
	 */
	override isAffected(Collection<Delta> deltas, IResourceDescription candidate, IResourceDescriptions context) {
		super.isAffected(deltas, candidate, context) // TODO implement
	}

	override IResourceDescription internalGetResourceDescription(Resource resource,
			IDefaultResourceDescriptionStrategy strategy) {
		if (resource instanceof DerivedStateAwareResource) {
			if (!resource.isLoaded()) {
				try {
					resource.load(resource.getResourceSet().getLoadOptions());
				} catch (IOException e) {
					throw new RuntimeIOException(e);
				}
			}
			val isInitialized = resource.fullyInitialized || resource.isInitializing;
			try {
				if (!isInitialized) {
					resource.eSetDeliver(false);
					resource.installDerivedState(true);
				}
				val description = createResourceDescription(resource, strategy);
				if (!isInitialized) {
					// eager initialize
					for (IEObjectDescription desc : description.getExportedObjects()) {
						desc.getEObjectURI();
					}
				}
				return description;
			} finally {
				if (!isInitialized) {
					resource.eSetDeliver(true);
				}
			}
		} else {
			return super.internalGetResourceDescription(resource, strategy);
		}
	}
}
