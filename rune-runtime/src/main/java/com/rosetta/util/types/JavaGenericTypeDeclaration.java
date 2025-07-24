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

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass.JavaClassImpl;

public abstract class JavaGenericTypeDeclaration<T> implements JavaTypeDeclaration<T> {
	protected static class JavaGenericTypeDeclarationImpl<T> extends JavaGenericTypeDeclaration<T> {
		private final Class<T> backingClass;
		private final JavaClass<T> baseType;
		private final List<JavaTypeVariable> parameters;
		private final Map<TypeVariable<?>, JavaTypeVariable> parameterMap;

		public JavaGenericTypeDeclarationImpl(Class<T> backingClass) {
			this.backingClass = backingClass;
			this.baseType = JavaClass.from(backingClass);
			this.parameters = new ArrayList<>();
			this.parameterMap = new HashMap<>();
			for (TypeVariable<?> tp : backingClass.getTypeParameters()) {
				JavaTypeVariable result = JavaTypeVariable.from(this, tp, parameterMap);
				parameters.add(result);
				parameterMap.put(tp, result);
			}
		}
		
		public Class<T> getBackingClass() {
			return backingClass;
		}

		@Override
		public JavaClass<T> getBaseType() {
			return baseType;
		}

		@Override
		public List<JavaTypeVariable> getParameters() {
			return parameters;
		}
		
		@Override
		public JavaTypeDeclaration<? super T> getSuperclassDeclaration() {
			Class<? super T> superclass = backingClass.getSuperclass();
			if (superclass == null) {
				return JavaClass.OBJECT;
			}
			return JavaTypeDeclaration.from(superclass);
		}
		
		@Override
		public JavaClass<? super T> getSuperclass() {
			JavaTypeDeclaration<? super T> superDeclaration = getSuperclassDeclaration();
			if (superDeclaration == null) {
				return null;
			} else if (superDeclaration instanceof JavaClass<?>) {
				return (JavaClass<? super T>) superDeclaration;
			} else {
				return JavaClass.from(backingClass.getSuperclass(), backingClass.getGenericSuperclass(), parameterMap);
			}
		}
		
		@Override
		public List<JavaTypeDeclaration<?>> getInterfaceDeclarations() {
			return Arrays.stream(backingClass.getInterfaces())
					.map(i -> JavaTypeDeclaration.from(i))
					.collect(Collectors.toList());
		}
		
		@Override
		public List<JavaClass<?>> getInterfaces() {
			List<JavaClass<?>> result = new ArrayList<>();
			Class<?>[] interfaces = backingClass.getInterfaces();
			Type[] genericInterfaces = backingClass.getGenericInterfaces();
			for (int i=0; i<interfaces.length; i++) {
				JavaTypeDeclaration<?> decl = JavaTypeDeclaration.from(interfaces[i]);
				if (decl instanceof JavaClass<?>) {
					result.add((JavaClass<?>) decl);
				} else {
					result.add(JavaClass.from(interfaces[i], genericInterfaces[i], parameterMap));
				}
			}
			return result;
		}
		
		@Override
		public boolean extendsDeclaration(JavaTypeDeclaration<?> other) {
			if (other.equals(JavaClass.OBJECT)) {
				return true;
			}
			if (other instanceof JavaGenericTypeDeclarationImpl<?>) {
				return ((JavaGenericTypeDeclarationImpl<?>)other).getBackingClass().isAssignableFrom(backingClass);
			}
			if (other instanceof JavaClassImpl<?>) {
				return ((JavaClassImpl<?>)other).getBackingClass().isAssignableFrom(backingClass);
			}
			if (other instanceof JavaParameterizedType<?>) {
				return this.extendsDeclaration(((JavaParameterizedType<?>)other).getGenericTypeDeclaration());
			}
			return false;
		}
		
		@Override
		public Class<T> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
			return backingClass;
		}

		@Override
		public boolean isFinal() {
			return Modifier.isFinal(backingClass.getModifiers()) || backingClass.isEnum();
		}
	}
	
	public static <T> JavaGenericTypeDeclaration<T> from(Class<T> c) {
		return new JavaGenericTypeDeclarationImpl<>(c);
	}
	public static <T> JavaGenericTypeDeclaration<T> from(TypeReference<T> typeRef) {
		return from(JavaParameterizedType.extractRawClass(typeRef.getType()));
	}

	public abstract JavaClass<T> getBaseType();
	public abstract List<JavaTypeVariable> getParameters();
	
	public abstract JavaClass<? super T> getSuperclass();
	public abstract List<JavaClass<?>> getInterfaces();
	
	@Override
	public DottedPath getPackageName() {
		return getBaseType().getPackageName();
	}
	@Override
	public DottedPath getNestedTypeName() {
		return getBaseType().getNestedTypeName();
	}
	
	@Override
	public JavaParameterizedType<T> applySubstitution(Map<JavaTypeVariable, JavaTypeArgument> substitution) {
		return JavaParameterizedType.from(
				this,
				this.getParameters().stream()
					.map(p -> {
						if (!substitution.containsKey(p)) {
							throw new IllegalArgumentException("Substitution must contain a key for " + p + ".");
						}
						return substitution.get(p);
					})
					.collect(Collectors.toList())
			);
	}
	
	@Override
	public String toString() {
		return getBaseType().toString() + "<" + getParameters().stream().map(Object::toString).collect(Collectors.joining(", ")) + ">";
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getBaseType(), getParameters());
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        JavaGenericTypeDeclaration<?> other = (JavaGenericTypeDeclaration<?>) object;
        return Objects.equals(getBaseType(), other.getBaseType())
        		&& Objects.equals(getParameters(), other.getParameters());
	}
}
