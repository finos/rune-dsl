package com.rosetta.model.lib.sequence;

import com.rosetta.model.lib.RosettaModelObject;

public interface Sequence<T extends RosettaModelObject, U extends RosettaModelObject> {
	
	U enrich(T modelObject);

}
