package com.rosetta.model.lib.meta;


public interface BasicReferenceWithMetaBuilder<I>{
	public BasicReferenceWithMetaBuilder<I> setGlobalReference(String globalKey) ;
	public BasicReferenceWithMetaBuilder<I> setExternalReference(String ExternalKey) ;
	String getGlobalReference();
	String getExternalReference();
	
	I getValue();
	BasicReferenceWithMetaBuilder<I> setValue(I value);
	Class<I> getValueType();
}