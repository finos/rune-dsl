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

package com.rosetta.util.types.generated;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaGenericTypeDeclaration;
import com.rosetta.util.types.JavaTypeDeclaration;
import com.rosetta.util.types.JavaTypeVariable;

public class GeneratedJavaGenericTypeDeclaration<T> extends JavaGenericTypeDeclaration<T> {
	private final GeneratedJavaClass<T> baseType;
	private final List<JavaTypeVariable> parameters;
	
	public GeneratedJavaGenericTypeDeclaration(GeneratedJavaClass<T> baseType, String... typeParameterNames) {
		Objects.requireNonNull(baseType);
		Validate.noNullElements(typeParameterNames);
		this.baseType = baseType;
		this.parameters = Arrays.stream(typeParameterNames)
				.map(n -> new JavaTypeVariable(this, n))
				.collect(Collectors.toList());
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
	public JavaClass<? super T> getSuperclassDeclaration() {
		return JavaClass.OBJECT;
	}
	
	@Override
	public JavaClass<? super T> getSuperclass() {
		return getSuperclassDeclaration();
	}
	
	@Override
	public List<JavaClass<?>> getInterfaceDeclarations() {
		return Collections.emptyList();
	}
	
	@Override
	public List<JavaClass<?>> getInterfaces() {
		return getInterfaceDeclarations();
	}

	@Override
	public Class<? extends T> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
		return baseType.loadClass(classLoader);
	}

	@Override
	public boolean extendsDeclaration(JavaTypeDeclaration<?> other) {
		return this.equals(other) || JavaClass.OBJECT.equals(other);
	}

	@Override
	public boolean isFinal() {
		return false;
	}
}
