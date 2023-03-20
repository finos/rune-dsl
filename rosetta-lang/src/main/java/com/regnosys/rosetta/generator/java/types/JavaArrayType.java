package com.regnosys.rosetta.generator.java.types;

import java.util.Objects;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

public class JavaArrayType implements JavaReferenceType {
	private final JavaType baseType;

	public JavaArrayType(JavaType baseType) {
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
	public void appendTo(TargetStringConcatenation target) {
		baseType.appendTo(target);
		target.append("[]");
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(baseType);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        JavaArrayType other = (JavaArrayType) object;
        return Objects.equals(baseType, other.baseType);
	}
}
