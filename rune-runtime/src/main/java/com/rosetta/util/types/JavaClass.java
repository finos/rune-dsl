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

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaGenericTypeDeclaration.JavaGenericTypeDeclarationImpl;


public abstract class JavaClass<T> implements JavaReferenceType, JavaTypeDeclaration<T> {
	public static final JavaClass<Object> OBJECT = new JavaClassImpl<>(Object.class);
	public static final JavaClass<Cloneable> CLONEABLE = new JavaClassImpl<>(Cloneable.class);
	public static final JavaClass<Serializable> SERIALIZABLE = new JavaClassImpl<>(Serializable.class);
	
	protected static class JavaClassImpl<T> extends JavaClass<T> {
		private final Class<T> backingClass;
		private final DottedPath packageName;
		private final DottedPath nestedTypeName;

		public JavaClassImpl(Class<T> backingClass) {
			this.packageName = DottedPath.splitOnDots(backingClass.getPackage().getName());
			this.backingClass = backingClass;
			
			List<String> parts = new ArrayList<>();
			Class<?> current = backingClass;
		    while (current != null) {
		        parts.add(current.getSimpleName());
		        current = current.getDeclaringClass();
		    }
		    Collections.reverse(parts);
		    this.nestedTypeName = DottedPath.of(parts.toArray(new String[0]));
		}
		
		public Class<T> getBackingClass() {
			return backingClass;
		}
		
		@Override
		public boolean isSubtypeOf(JavaType other) {
			if (other instanceof JavaPrimitiveType) {
				return false;
			}
			if (other.equals(OBJECT)) {
				return true;
			}
			if (other instanceof JavaClassImpl<?>) {
				return ((JavaClassImpl<?>)other).backingClass.isAssignableFrom(backingClass);
			}
			if (other instanceof JavaParameterizedType<?>) {
				return ((JavaParameterizedType<?>)other).isSupertypeOf(this);
			}
			return false;
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
				return JavaClass.from(backingClass.getSuperclass(), backingClass.getGenericSuperclass(), Collections.emptyMap());
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
					result.add(JavaClass.from(interfaces[i], genericInterfaces[i], Collections.emptyMap()));
				}
			}
			return result;
		}

		@Override
		public DottedPath getNestedTypeName() {
			return nestedTypeName;
		}

		@Override
		public DottedPath getPackageName() {
			return packageName;
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
	
	public static <T> JavaClass<T> from(Class<T> raw, Type generic, Map<TypeVariable<?>, JavaTypeVariable> context) {
		if (generic instanceof Class<?>) {
			return JavaClass.from(raw);
		} else if (generic instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) generic;
			return JavaParameterizedType.from(raw, pt, context);
		} else {
			return null;
		}
	}
	public static JavaClass<?> from(Type t, Map<TypeVariable<?>, JavaTypeVariable> context) {
		if (t instanceof Class<?>) {
			return JavaClass.from((Class<?>)t);
		} else if (t instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) t;
			return JavaParameterizedType.from((Class<?>)pt.getRawType(), pt, context);
		} else {
			return null;
		}
	}
	public static <T> JavaClass<T> from(Class<T> c) {
		if (c.isArray() || c.isPrimitive() || c.getSimpleName().isEmpty()) {
			return null;
		}
		return new JavaClassImpl<>(c);
	}
	
	public abstract JavaClass<? super T> getSuperclass();
	public abstract List<JavaClass<?>> getInterfaces();
	@Override
	public String getSimpleName() {
		return JavaTypeDeclaration.super.getSimpleName();
	}
	
	@Override
	public JavaClass<T> applySubstitution(Map<JavaTypeVariable, JavaTypeArgument> substitution) {
		return this;
	}
	
	@Override
	public String toString() {
		return getCanonicalName().withDots();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getPackageName(), getSimpleName());
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        JavaClass<?> other = (JavaClass<?>) object;
        return Objects.equals(getPackageName(), other.getPackageName())
        		&& Objects.equals(getSimpleName(), other.getSimpleName());
	}
	
	@Override
	public void accept(JavaTypeVisitor visitor) {
		visitor.visitType(this);
	}
}
