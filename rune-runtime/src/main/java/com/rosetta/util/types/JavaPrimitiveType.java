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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JavaPrimitiveType implements JavaType {
	private static Map<Class<?>, JavaPrimitiveType> typeMap = new HashMap<>();
	private static Map<JavaClass<?>, JavaPrimitiveType> wrapperTypeMap = new HashMap<>();
	
	public static JavaPrimitiveType INT = create(int.class, Integer.class, 2);
	public static JavaPrimitiveType BYTE = create(byte.class, Byte.class, 0);
	public static JavaPrimitiveType SHORT = create(short.class, Short.class, 1);
	public static JavaPrimitiveType LONG = create(long.class, Long.class, 3);
	public static JavaPrimitiveType FLOAT = create(float.class, Float.class, 4);
	public static JavaPrimitiveType DOUBLE = create(double.class, Double.class, 5);
	public static JavaPrimitiveType BOOLEAN = create(boolean.class, Boolean.class, -1);
	public static JavaPrimitiveType CHAR = create(char.class, Character.class, -1);
	public static JavaPrimitiveType VOID = create(void.class, Void.class, -1);
	
	private final Class<?> type;
	private final Class<?> wrapperType;
	private final int numericHierarchy;
	private JavaPrimitiveType(Class<?> type, Class<?> wrapperType, int numericHierarchy) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(wrapperType);
		this.type = type;
		this.wrapperType = wrapperType;
		this.numericHierarchy = numericHierarchy;
	}
	
	private static JavaPrimitiveType create(Class<?> type, Class<?> wrapperType, int numericHierarchy) {
		JavaPrimitiveType t = new JavaPrimitiveType(type, wrapperType, numericHierarchy);
		typeMap.put(type, t);
		wrapperTypeMap.put(JavaClass.from(wrapperType), t);
		return t;
	}
	
	public static JavaPrimitiveType from(Class<?> type) {
		return typeMap.get(type);
	}
	public static JavaPrimitiveType fromWrapper(Class<?> wrapperType) {
		return fromWrapper(JavaClass.from(wrapperType));
	}
	public static JavaPrimitiveType fromWrapper(JavaClass<?> wrapperType) {
		return wrapperTypeMap.get(wrapperType);
	}
	
	public Class<?> getType() {
		return type;
	}
	public Class<?> getWrapperType() {
		return wrapperType;
	}
	
	@Override
	public JavaClass<?> toReferenceType() {
		return JavaClass.from(wrapperType);
	}
	
	@Override
	public String getSimpleName() {
		return type.getSimpleName();
	}
	
	@Override
	public String toString() {
		return type.getSimpleName();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(type);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        JavaPrimitiveType other = (JavaPrimitiveType) object;
        return Objects.equals(type, other.type);
	}
	
	@Override
	public void accept(JavaTypeVisitor visitor) {
		visitor.visitType(this);
	}

	@Override
	public boolean isSubtypeOf(JavaType other) {
		if (this.equals(other)) {
			return true;
		}
		if (other instanceof JavaPrimitiveType) {
			JavaPrimitiveType otherPrim = (JavaPrimitiveType) other;
			if (this.equals(CHAR)) {
				return INT.numericHierarchy <= otherPrim.numericHierarchy;
			} else if (numericHierarchy >= 0 && otherPrim.numericHierarchy >= 0) {
				return numericHierarchy <= otherPrim.numericHierarchy;
			}
		}
		return false;
	}
}
