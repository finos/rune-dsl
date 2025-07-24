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

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

public class JavaTypeVariable implements JavaReferenceType {
	private final JavaGenericTypeDeclaration<?> declaration;
	private final String name;
	private final List<JavaReferenceType> bounds;
	public JavaTypeVariable(JavaGenericTypeDeclaration<?> declaration, String name, JavaReferenceType... bounds) {
		Objects.requireNonNull(declaration);
		Objects.requireNonNull(name);
		Validate.noNullElements(bounds);
		this.declaration = declaration;
		this.name = name;
		this.bounds = Arrays.asList(bounds);
	}
	
	public static JavaTypeVariable from(JavaGenericTypeDeclaration<?> declaration, TypeVariable<?> var, Map<TypeVariable<?>, JavaTypeVariable> context) {
		JavaReferenceType[] bounds = new JavaReferenceType[var.getBounds().length];
		for (int i=0; i<var.getBounds().length; i++) {
			JavaType bound = JavaType.from(var.getBounds()[i], context);
			if (bound instanceof JavaReferenceType) {
				bounds[i] = (JavaReferenceType) bound;
			} else {
				return null;
			}
		}
		return new JavaTypeVariable(declaration, var.getName(), bounds);
	}
	public static JavaTypeVariable from(TypeVariable<?> var, Map<TypeVariable<?>, JavaTypeVariable> context) {
		if (context.containsKey(var)) {
			return context.get(var);
		}
		if (var.getGenericDeclaration() instanceof Class<?>) {
			JavaGenericTypeDeclaration<?> declaration = JavaGenericTypeDeclaration.from((Class<?>)var.getGenericDeclaration());
			return declaration.getParameters().stream()
					.filter(p -> p.getName().equals(var.getName()))
					.findAny()
					.orElse(null);
		}
		return null;
	}
	
	public JavaGenericTypeDeclaration<?> getDeclaration() {
		return declaration;
	}
	
	public String getName() {
		return name;
	}
	
	public List<JavaReferenceType> getBounds() {
		return bounds;
	}
	
	@Override
	public String getSimpleName() {
		return name;
	}
	
	@Override
	public boolean isSubtypeOf(JavaType other) {
		return bounds.stream().anyMatch(b -> b.isSubtypeOf(other));
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(declaration.getBaseType(), name, bounds);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        JavaTypeVariable other = (JavaTypeVariable) object;
        return Objects.equals(declaration.getBaseType(), other.declaration.getBaseType())
        		&& Objects.equals(name, other.name)
        		&& Objects.equals(bounds, other.bounds);
	}
	
	@Override
	public void accept(JavaTypeVisitor visitor) {
		visitor.visitType(this);
	}
}
