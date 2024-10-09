package com.rosetta.util.types;

import com.rosetta.model.lib.RosettaModelObject;

public abstract class RJavaWithMetaValue extends JavaClass<RosettaModelObject>{
	protected final JavaReferenceType valueType;

	
	
	public RJavaWithMetaValue(JavaReferenceType valueType) {
		this.valueType = valueType;
	}

	public JavaReferenceType getValueType() {
		return valueType;
	}
	
	@Override
	public JavaClass<? super RosettaModelObject> getSuperclass() {
		return JavaClass.OBJECT;
	}

}
