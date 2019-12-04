package com.rosetta.model.lib.meta;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

public interface ReferenceWithMetaBuilder<I>{
	public ReferenceWithMetaBuilder<I> setGlobalReference(String globalKey) ;
	public ReferenceWithMetaBuilder<I> setExternalReference(String ExternalKey) ;
	String getGlobalReference();
	String getExternalReference();
	
	RosettaModelObjectBuilder getValue();
	ReferenceWithMetaBuilder<I> setValue(I value);
	Class<I> getValueType();
}