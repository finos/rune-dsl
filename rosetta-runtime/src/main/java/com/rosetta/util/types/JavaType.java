package com.rosetta.util.types;


/**
 * A simplified model of types in Java, based on the Java specs:
 * https://docs.oracle.com/javase/specs/jls/se19/html/jls-4.html
 */
public interface JavaType {
	public static JavaType from(Class<?> t) {
		if (t.isArray()) {
			return JavaArrayType.from(t);
		} else if (t.isPrimitive()) {
			return JavaPrimitiveType.from(t);
		} else if (t.isInterface()) {
			return JavaInterface.from(t);
		} else {
			return JavaClass.from(t);
		}
	}
	public static JavaType from(Object obj) {
		if (obj instanceof JavaType) {
			return (JavaType)obj;
		}
		if (obj instanceof Class<?>) {
			return from((Class<?>)obj);
		}
		return null;
	}
	
	public String getSimpleName();
	
	public void accept(JavaTypeVisitor visitor);
}
