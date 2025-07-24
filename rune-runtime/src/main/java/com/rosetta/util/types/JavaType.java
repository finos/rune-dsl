/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rosetta.util.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.Map;

/**
 * A simplified model of types in Java, based on the Java specification:
 * https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html
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
	
	// See https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.6
	default JavaType getTypeErasure() {
		return this;
	}
}
