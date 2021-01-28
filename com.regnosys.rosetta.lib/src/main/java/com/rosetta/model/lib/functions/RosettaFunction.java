package com.rosetta.model.lib.functions;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;

/**
 * A marker interface to be implemented by all generated instances of RosettaFunction
 */
public interface RosettaFunction {
	@SuppressWarnings("unchecked")
	default <R extends RosettaModelObjectBuilder> R toBuilder(RosettaModelObject object) {
		if (object==null) return null;
		return (R) object.toBuilder();
	}
}
