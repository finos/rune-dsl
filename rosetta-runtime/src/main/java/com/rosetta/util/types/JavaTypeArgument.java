package com.rosetta.util.types;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;

public interface JavaTypeArgument {
	public static JavaTypeArgument from(Type t, Map<TypeVariable<?>, JavaTypeVariable> context) {
		if (t instanceof Class<?>) {
			JavaType result = JavaType.from(t, context);
			if (result instanceof JavaReferenceType) {
				return (JavaReferenceType) result;
			}
		} else if (t instanceof WildcardType) {
			return JavaWildcardTypeArgument.from((WildcardType) t, context);
		} else if (t instanceof TypeVariable) {
			return JavaTypeVariable.from((TypeVariable<?>)t, context);
		}
		return null;
	}
	
	// See Java specification of the `contains` relationship.
	// https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.5.1
	public boolean contains(JavaTypeArgument other);
	
	public void accept(JavaTypeArgumentVisitor visitor);
}
