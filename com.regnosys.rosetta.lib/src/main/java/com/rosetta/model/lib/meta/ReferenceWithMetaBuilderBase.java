package com.rosetta.model.lib.meta;

import com.rosetta.model.lib.meta.Reference.ReferenceBuilder;

public interface ReferenceWithMetaBuilderBase<I> {
	ReferenceWithMetaBuilderBase<I> setGlobalReference(String globalKey);

	ReferenceWithMetaBuilderBase<I> setExternalReference(String ExternalKey);
	
	ReferenceWithMetaBuilderBase<I> setReference(ReferenceBuilder reference);

	String getGlobalReference();

	String getExternalReference();

	ReferenceBuilder getReference();

	Class<I> getValueType();
}