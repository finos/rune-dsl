package com.rosetta.util.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.Map;

/**
 * A simplified model of types in Java, based on the Java specs:
 * https://docs.oracle.com/javase/specs/jls/se19/html/jls-4.html
 */
public interface JavaType {
	public static <T> JavaType from(Type t) {
		return from(t, Collections.emptyMap());
	}
	public static <T> JavaType from(Type t, Map<TypeVariable<?>, JavaTypeVariable> context) {
		if (t instanceof Class<?>) {
			Class<?> c = (Class<?>) t;
			if (c.isArray()) {
				return JavaArrayType.from(c, context);
			} else if (c.isPrimitive()) {
				return JavaPrimitiveType.from(c);
			} else {
				return JavaClass.from(c);
			}
		} else if (t instanceof GenericArrayType) {
			return JavaArrayType.from(t, context);
		} else if (t instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) t;
			return JavaParameterizedType.from((Class<?>)pt.getRawType(), pt, Collections.emptyMap());
		} else if (t instanceof TypeVariable<?>) {
			return JavaTypeVariable.from((TypeVariable<?>) t, Collections.emptyMap());
		} else {
			return null;
		}
	}
	public static JavaType from(Object obj) {
		return from(obj, Collections.emptyMap());
	}
	public static JavaType from(Object obj, Map<TypeVariable<?>, JavaTypeVariable> context) {
		if (obj instanceof JavaType) {
			return (JavaType)obj;
		}
		if (obj instanceof Type) {
			return from((Type)obj, context);
		}
		return null;
	}
	
	boolean isSubtypeOf(JavaType other);
	JavaReferenceType toReferenceType();
	String getSimpleName();
	void accept(JavaTypeVisitor visitor);
}
