package com.rosetta.model.lib.meta;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

public interface ReferenceWithMeta<T> extends FieldWithMeta<T>{
	String getGlobalReference();
	String getExternalReference();
	
	Reference getReference();
	
	interface ReferenceWithMetaBuilder<I> extends ReferenceWithMeta<I>, RosettaModelObjectBuilder {
		
		ReferenceWithMetaBuilder<I> setGlobalReference(String globalKey);

		ReferenceWithMetaBuilder<I> setExternalReference(String ExternalKey);
		
		ReferenceWithMetaBuilder<I> setReference(Reference reference);
		
		I getValue();
		ReferenceWithMetaBuilder<I> setValue(I value);
		Class<I> getValueType();
	}
}
