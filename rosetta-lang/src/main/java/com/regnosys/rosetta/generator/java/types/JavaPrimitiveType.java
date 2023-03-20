package com.regnosys.rosetta.generator.java.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

public class JavaPrimitiveType implements JavaType {
	public static JavaPrimitiveType INT = create(int.class, Integer.class);
	public static JavaPrimitiveType BYTE = create(byte.class, Byte.class);
	public static JavaPrimitiveType SHORT = create(short.class, Short.class);
	public static JavaPrimitiveType LONG = create(long.class, Long.class);
	public static JavaPrimitiveType FLOAT = create(float.class, Float.class);
	public static JavaPrimitiveType DOUBLE = create(double.class, Double.class);
	public static JavaPrimitiveType BOOLEAN = create(boolean.class, Boolean.class);
	public static JavaPrimitiveType CHAR = create(char.class, Character.class);
	public static JavaPrimitiveType VOID = create(void.class, Void.class);
	
	private static Map<Class<?>, JavaPrimitiveType> typeMap = new HashMap<>();
	
	private final Class<?> type;
	private final Class<?> wrapperType;
	private JavaPrimitiveType(Class<?> type, Class<?> wrapperType) {
		this.type = type;
		this.wrapperType = wrapperType;
	}
	
	private static JavaPrimitiveType create(Class<?> type, Class<?> wrapperType) {
		JavaPrimitiveType t = new JavaPrimitiveType(type, wrapperType);
		typeMap.put(type, t);
		return t;
	}
	
	public static JavaPrimitiveType from(Class<?> type) {
		return typeMap.get(type);
	}
	
	public Class<?> getType() {
		return type;
	}
	public Class<?> getWrapperType() {
		return wrapperType;
	}
	
	public JavaClass toReferenceType() {
		return JavaClass.from(wrapperType);
	}
	
	@Override
	public void appendTo(TargetStringConcatenation target) {
		target.append(type.getSimpleName());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(type);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        JavaPrimitiveType other = (JavaPrimitiveType) object;
        return Objects.equals(type, other.type);
	}
}