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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rosetta.util.DottedPath;


public abstract class JavaParameterizedType<T> extends JavaClass<T> {
	protected static class JavaParameterizedTypeImpl<T> extends JavaParameterizedType<T> {
		private final JavaGenericTypeDeclaration<? super T> genericTypeDeclaration;
		private final List<JavaTypeArgument> arguments;

		public JavaParameterizedTypeImpl(JavaGenericTypeDeclaration<? super T> genericTypeDeclaration, List<JavaTypeArgument> arguments) {
			Validate.isTrue(genericTypeDeclaration.getParameters().size() == arguments.size());
			this.genericTypeDeclaration = genericTypeDeclaration;
			this.arguments = arguments;
		}

		@Override
		public JavaGenericTypeDeclaration<? super T> getGenericTypeDeclaration() {
			return genericTypeDeclaration;
		}

		@Override
		public List<JavaTypeArgument> getArguments() {
			return arguments;
		}
	}
	
	public static <T> JavaParameterizedType<T> from(TypeReference<T> typeRef, List<JavaTypeArgument> arguments) {
		Type t = typeRef.getType();
		if (t instanceof ParameterizedType) {
			return from(JavaGenericTypeDeclaration.from(extractRawClass(t)), arguments);
		}
		throw new IllegalArgumentException("Type " + t + " is not a parameterized type.");
	}
	public static <T> JavaParameterizedType<T> from(TypeReference<T> typeRef, JavaTypeArgument... arguments) {
		return from(typeRef, Arrays.asList(arguments));
	}
	public static <T> JavaParameterizedType<T> from(Class<T> rawType, ParameterizedType t, Map<TypeVariable<?>, JavaTypeVariable> context) {
		return from(
				JavaGenericTypeDeclaration.from(rawType), 
				Arrays.stream(t.getActualTypeArguments())
					.map(ta -> JavaTypeArgument.from(ta, context))
					.collect(Collectors.toList()));
	}
	public static <T> JavaParameterizedType<T> from(JavaGenericTypeDeclaration<? super T> typeDeclaration, JavaTypeArgument... arguments) {
		return from(typeDeclaration, Arrays.asList(arguments));
	}
	public static <T> JavaParameterizedType<T> from(JavaGenericTypeDeclaration<? super T> typeDeclaration, List<JavaTypeArgument> arguments) {
		return new JavaParameterizedTypeImpl<>(typeDeclaration, arguments);
	}
	@SuppressWarnings("unchecked")
	public static <U> Class<U> extractRawClass(Type t) {
		if (t instanceof Class<?>) {
			return (Class<U>) t;
		} else if (t instanceof GenericArrayType) {
			return extractRawClass(((GenericArrayType) t).getGenericComponentType());
		} else if (t instanceof ParameterizedType) {
			return extractRawClass(((ParameterizedType) t).getRawType());
		}
		throw new IllegalArgumentException("Cannot use a type reference to " + t + ". No raw class found.");
	}
	
	public abstract JavaGenericTypeDeclaration<? super T> getGenericTypeDeclaration();
	public abstract List<JavaTypeArgument> getArguments();
	
	@Override
	public boolean isSubtypeOf(JavaType other) {
		// TODO: implement capture conversion (see https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.2)
		if (this.equals(other) || other.equals(OBJECT)) {
			return true;
		}
		if (other instanceof JavaParameterizedType<?>) {
			return ((JavaParameterizedType<?>) other).isSupertypeOf(this);
		}
		if (other instanceof JavaClass) {
			return getGenericTypeDeclaration().getBaseType().isSubtypeOf(other);
		}
		return false;
	}
	public boolean isSupertypeOf(JavaClass<?> other) {
		if (this.equals(other)) {
			return true;
		}
		JavaGenericTypeDeclaration<? super T> typeDeclaration = getGenericTypeDeclaration();
		if (other.extendsDeclaration(typeDeclaration)) {
			JavaClass<?> currentSuper = other;
			JavaClass<?> nextSuper = currentSuper.getSuperclass();
			// First check superclasses
			while (nextSuper.extendsDeclaration(typeDeclaration)) {
				currentSuper = nextSuper;
				nextSuper = currentSuper.getSuperclass();
			}
			// Then check interfaces
			Optional<JavaClass<?>> nextInterface = currentSuper.getInterfaces().stream()
					.filter(i -> i.extendsDeclaration(typeDeclaration))
					.findAny();
			while (nextInterface.isPresent()) {
				currentSuper = nextInterface.get();
				nextInterface = currentSuper.getInterfaces().stream()
						.filter(i -> i.extendsDeclaration(typeDeclaration))
						.findAny();
			}
			Map<JavaTypeVariable, JavaTypeArgument> substitution = ((JavaParameterizedType<?>)currentSuper).getTypeVariableSubstitution();
			
			int paramCount = getGenericTypeDeclaration().getParameters().size();
			for (int i=0; i<paramCount; i++) {
				JavaTypeArgument argument = this.getArguments().get(i);
				JavaTypeArgument otherArgument = substitution.get(getGenericTypeDeclaration().getParameters().get(i));
				if (!argument.contains(otherArgument)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public Map<JavaTypeVariable, JavaTypeArgument> getTypeVariableSubstitution() {
		Map<JavaTypeVariable, JavaTypeArgument> substitution = new HashMap<>();
		addTypeVariableSubstitution(substitution);
		return substitution;
	}
	public void addTypeVariableSubstitution(Map<JavaTypeVariable, JavaTypeArgument> substitution) {		
		JavaGenericTypeDeclaration<?> declaration = getGenericTypeDeclaration();
		for (int i=0; i<declaration.getParameters().size(); i++) {
			JavaTypeVariable parameter = declaration.getParameters().get(i);
			JavaTypeArgument argument = this.getArguments().get(i);
			if (substitution.containsKey(argument)) {
				argument = substitution.get(argument);
			}
			substitution.put(parameter, argument);
		}
	}
	
	@Override
	public boolean extendsDeclaration(JavaTypeDeclaration<?> other) {
		return getGenericTypeDeclaration().extendsDeclaration(other);
	}
	
	@Override
	public JavaTypeDeclaration<? super T> getSuperclassDeclaration() {
		return getGenericTypeDeclaration().getSuperclassDeclaration();
	}
	
	@Override
	public JavaClass<? super T> getSuperclass() {
		return getGenericTypeDeclaration().getSuperclass().applySubstitution(getTypeVariableSubstitution());
	}
	
	@Override
	public List<? extends JavaTypeDeclaration<?>> getInterfaceDeclarations() {
		return getGenericTypeDeclaration().getInterfaceDeclarations();
	}
	
	@Override
	public List<JavaClass<?>> getInterfaces() {
		return getGenericTypeDeclaration().getInterfaces().stream()
				.map(i -> i.applySubstitution(getTypeVariableSubstitution()))
				.collect(Collectors.toList());
	}
	
	@Override
	public JavaParameterizedType<T> applySubstitution(Map<JavaTypeVariable, JavaTypeArgument> substitution) {
		List<JavaTypeArgument> newArguments =
				this.getArguments().stream()
					.map(arg -> substitution.get(arg))
					.collect(Collectors.toList());
		return new JavaParameterizedTypeImpl<>(getGenericTypeDeclaration(), newArguments);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends T> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
		return (Class<? extends T>) getGenericTypeDeclaration().loadClass(classLoader);
	}
	
	@Override
	public boolean isFinal() {
		return getGenericTypeDeclaration().isFinal();
	}
	
	@Override
	public DottedPath getNestedTypeName() {
		return getGenericTypeDeclaration().getBaseType().getNestedTypeName();
	}
	
	@Override
	public DottedPath getPackageName() {
		return getGenericTypeDeclaration().getBaseType().getPackageName();
	}
	
	@Override
	public String toString() {
		return getSimpleName() + "<" + getArguments().stream().map(Object::toString).collect(Collectors.joining(", ")) + ">";
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getGenericTypeDeclaration(), getArguments());
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        JavaParameterizedType<?> other = (JavaParameterizedType<?>) object;
        return Objects.equals(getGenericTypeDeclaration(), other.getGenericTypeDeclaration())
        		&& Objects.equals(getArguments(), other.getArguments());
	}
	
	@Override
	public void accept(JavaTypeVisitor visitor) {
		visitor.visitType(this);
	}
	
	@Override
	public JavaClass<? super T> getTypeErasure() {
		return getGenericTypeDeclaration().getBaseType();
	}
}
