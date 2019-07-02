package com.rosetta.model.lib.meta;


public interface ReferenceWithMetaBuilder<I>{
	public ReferenceWithMetaBuilder<I> setGlobalReference(String globalKey) ;
	public ReferenceWithMetaBuilder<I> setExternalReference(String ExternalKey) ;
	String getGlobalReference();
	String getExternalReference();
}