package com.rosetta.model.lib.meta;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

public interface ReferenceWithMetaBuilder<I> extends ReferenceWithMetaBuilderBase<I> {
	
	RosettaModelObjectBuilder getValue();
	ReferenceWithMetaBuilder<I> setValue(I value);
	Class<I> getValueType();
}