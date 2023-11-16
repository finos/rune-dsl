package com.rosetta.util.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Objects;

public class JavaArrayType implements JavaReferenceType {
	private final JavaType baseType;

	public JavaArrayType(JavaType baseType) {
		Objects.requireNonNull(baseType);
		this.baseType = baseType;
	}
	
	public static JavaArrayType from(Type t, Map<TypeVariable<?>, JavaTypeVariable> context) {
		if (t instanceof Class<?>) {
			Class<?> c = (Class<?>) t;
			if (!c.isArray()) {
				return null;
			}
			return new JavaArrayType(JavaType.from(c.getComponentType(), context));
		} else if (t instanceof GenericArrayType) {
			GenericArrayType at = (GenericArrayType) t;
			return new JavaArrayType(JavaType.from(at.getGenericComponentType(), context));
		} else {
			return null;
		}
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
	public boolean isSubtypeOf(JavaType other) {
		if (other.equals(JavaClass.OBJECT) || other.equals(JavaClass.CLONEABLE) || other.equals(JavaClass.SERIALIZABLE)) {
			return true;
		}
		if (other instanceof JavaArrayType && baseType instanceof JavaReferenceType) {
			JavaType otherBaseType = ((JavaArrayType)other).getBaseType();
			if (otherBaseType instanceof JavaReferenceType) {
				return baseType.isSubtypeOf(otherBaseType);
			}
		}
		return false;
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
