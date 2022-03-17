package com.rosetta.model.lib.functions;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A marker interface to be implemented by all generated instances of RosettaFunction
 */
public interface RosettaFunction {
	@SuppressWarnings("unchecked")
	default <R extends RosettaModelObjectBuilder> R toBuilder(RosettaModelObject object) {
		if (object==null) return null;
		return (R) object.build().toBuilder();
	}
	
	@SuppressWarnings("unchecked")
	default <R extends RosettaModelObjectBuilder> List<R> toBuilder(List<? extends RosettaModelObject> objects) {
		if (objects==null) return null;
		return  objects.stream().map(b->(R)b.build().toBuilder()).collect(Collectors.toList());
	}
}
