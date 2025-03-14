package com.regnosys.rosetta.generator.java.types;

import java.util.Collection;

import com.rosetta.util.types.JavaReferenceType;

public abstract class RJavaWithMetaValue extends JavaPojoInterface {
	protected final JavaReferenceType valueType;
	
	public RJavaWithMetaValue(JavaReferenceType valueType) {
		this.valueType = valueType;
	}

	public JavaReferenceType getValueType() {
		return valueType;
	}
	
	@Override
	public Collection<JavaPojoProperty> getAllProperties() {
		return getOwnProperties();
	}
	
	@Override
	public JavaPojoInterface getSuperPojo() {
		return null;
	}

	@Override
	public String getJavadoc() {
		return null;
	}

	@Override
	public String getRosettaName() {
		return getSimpleName();
	}

	@Override
	public String getVersion() {
		return "0.0.0";
	}
}
