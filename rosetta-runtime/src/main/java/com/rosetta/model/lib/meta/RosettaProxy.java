package com.rosetta.model.lib.meta;

import com.rosetta.model.lib.RosettaModelObject;

public interface RosettaProxy<T extends RosettaModelObject> {
	String getKey();
	T getInstance();
	boolean isOriginal();
}
