package com.regnosys.rosetta.resource

import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionManager
import java.util.Collection
import org.eclipse.xtext.resource.IResourceDescription.Delta
import org.eclipse.xtext.resource.IResourceDescription
import org.eclipse.xtext.resource.IResourceDescriptions

class RosettaResourceDescriptionManager extends DefaultResourceDescriptionManager {

	/**
	 * Need to re-validate all the resources with isEvent or isProduct when the root class configuration 'isEvent root' or 'isProduct root' changes
	 */
	override isAffected(Collection<Delta> deltas, IResourceDescription candidate, IResourceDescriptions context) {
		super.isAffected(deltas, candidate, context) // TODO implement
	}

}
