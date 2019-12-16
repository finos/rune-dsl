package com.rosetta.model.lib.meta;

public interface ReferenceWithMetaBuilderBase<I> {
	ReferenceWithMetaBuilderBase<I> setGlobalReference(String globalKey) ;
	ReferenceWithMetaBuilderBase<I> setExternalReference(String ExternalKey) ;
	String getGlobalReference();
	String getExternalReference();
	Class<I> getValueType();
}