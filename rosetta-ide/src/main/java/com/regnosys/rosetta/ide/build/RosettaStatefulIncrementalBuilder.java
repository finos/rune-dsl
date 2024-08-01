package com.regnosys.rosetta.ide.build;

import org.eclipse.xtext.build.IncrementalBuilder;
import org.eclipse.xtext.resource.IResourceDescription;

import com.regnosys.rosetta.resource.IRosettaResourceDescription;

public class RosettaStatefulIncrementalBuilder extends IncrementalBuilder.InternalStatefulIncrementalBuilder {
	@Override
	protected IResourceDescription getSerializableResourceDescription(IResourceDescription description) {
		if (description instanceof IRosettaResourceDescription) {
			return RosettaSerializableResourceDescription.createCopy((IRosettaResourceDescription)description);
		}
		return super.getSerializableResourceDescription(description);
	}
}
