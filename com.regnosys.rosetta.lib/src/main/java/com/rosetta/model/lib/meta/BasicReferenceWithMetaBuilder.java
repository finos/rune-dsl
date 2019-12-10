package com.rosetta.model.lib.meta;


public interface BasicReferenceWithMetaBuilder<I> extends ReferenceWithMetaBuilderBase<I> {
	BasicReferenceWithMetaBuilder<I> setGlobalReference(String globalKey) ;
	BasicReferenceWithMetaBuilder<I> setExternalReference(String ExternalKey) ;
	String getGlobalReference();
	String getExternalReference();
	
	I getValue();
	BasicReferenceWithMetaBuilder<I> setValue(I value);
	Class<I> getValueType();
}