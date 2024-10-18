package com.regnosys.rosetta.generator.java.types;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaReferenceType;

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
