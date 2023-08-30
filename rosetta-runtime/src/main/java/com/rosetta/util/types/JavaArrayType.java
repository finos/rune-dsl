package com.rosetta.util.types;

import java.util.Objects;

public class JavaArrayType implements JavaReferenceType {
	private final JavaType baseType;

	public JavaArrayType(JavaType baseType) {
		Objects.requireNonNull(baseType);
		this.baseType = baseType;
	}
	
	public static JavaArrayType from(Class<?> t) {
		if (!t.isArray()) {
			return null;
		}
		return new JavaArrayType(JavaType.from(t.getComponentType()));
	}
	
	public JavaType getBaseType() {
		return this.baseType;
	}
	
	@Override
	public String toString() {
		return baseType.toString() + "[]";
	}
	
	@Override
	public String getSimpleName() {
		return baseType.getSimpleName();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(baseType);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        JavaArrayType other = (JavaArrayType) object;
        return Objects.equals(baseType, other.baseType);
	}

	@Override
	public void accept(JavaTypeVisitor visitor) {
		visitor.visitType(this);
	}
}
