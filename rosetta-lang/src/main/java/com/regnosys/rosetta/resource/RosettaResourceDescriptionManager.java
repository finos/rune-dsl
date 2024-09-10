package com.regnosys.rosetta.resource;

import java.util.Collection;

import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.DerivedStateAwareResourceDescriptionManager;
import org.eclipse.xtext.util.RuntimeIOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.DerivedStateAwareResource;
import org.eclipse.xtext.resource.IDefaultResourceDescriptionStrategy;
import java.io.IOException;
import org.eclipse.xtext.resource.IEObjectDescription;

public class RosettaResourceDescriptionManager extends DerivedStateAwareResourceDescriptionManager {
	/**
	 * Need to re-validate all the resources with isEvent or isProduct when the root class configuration 'isEvent root' or 'isProduct root' changes
	 */
	@Override
	public boolean isAffected(Collection<Delta> deltas, IResourceDescription candidate, IResourceDescriptions context) {
		return super.isAffected(deltas, candidate, context); // TODO implement
	}

	/**
	 * This is the same implementation as in `DerivedStateAwareResourceDescriptionManager`,
	 * EXCEPT that this implementation does not remove derived state once installed.
	 */
	@Override
	public IResourceDescription internalGetResourceDescription(Resource resource,
			IDefaultResourceDescriptionStrategy strategy) {
		if (resource instanceof DerivedStateAwareResource) {
			DerivedStateAwareResource dsaResource = (DerivedStateAwareResource) resource;
			if (!dsaResource.isLoaded()) {
				try {
					dsaResource.load(dsaResource.getResourceSet().getLoadOptions());
				} catch (IOException e) {
					throw new RuntimeIOException(e);
				}
			}
			boolean isInitialized = dsaResource.isFullyInitialized() || dsaResource.isInitializing();
			try {
				if (!isInitialized) {
					dsaResource.eSetDeliver(false);
					dsaResource.installDerivedState(true);
				}
				IResourceDescription description = createResourceDescription(dsaResource, strategy);
				if (!isInitialized) {
					// eager initialize
					for (IEObjectDescription desc : description.getExportedObjects()) {
						desc.getEObjectURI();
					}
				}
				return description;
			} finally {
				if (!isInitialized) {
					dsaResource.eSetDeliver(true);
				}
			}
		} else {
			return super.internalGetResourceDescription(resource, strategy);
		}
	}
}
