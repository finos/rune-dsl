package com.rosetta.model.lib.meta;

public interface ReferenceWithMeta<T> extends FieldWithMeta<T>{
	String getGlobalReference();
	String getExternalReference();
	
	Reference getReference();
}
