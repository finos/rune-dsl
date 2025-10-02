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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public class GeneratedJavaClass<T> extends JavaClass<T> {
	private final DottedPath packageName;
	private final String simpleName;
	private final Type supertype;
	private final Class<? super T> rawSupertype;
	
	private GeneratedJavaClass(DottedPath packageName, String simpleName, Type supertype, Class<? super T> rawSupertype) {
		Objects.requireNonNull(packageName);
		Objects.requireNonNull(simpleName);
		Objects.requireNonNull(supertype);
		Objects.requireNonNull(rawSupertype);
		this.packageName = packageName;
		this.simpleName = simpleName;
		this.supertype = supertype;
		this.rawSupertype = rawSupertype;
	}
	public GeneratedJavaClass(DottedPath packageName, String simpleName, Class<T> supertype) {
		this(packageName, simpleName, supertype, supertype);
	}
	public GeneratedJavaClass(DottedPath packageName, String simpleName, TypeReference<T> supertypeRef) {
		this(packageName, simpleName, supertypeRef.getType(), JavaParameterizedType.extractRawClass(supertypeRef.getType()));
	}
	
	@Override
	public boolean isSubtypeOf(JavaType other) {
		return this.equals(other);
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}
	
	@Override
	public DottedPath getNestedTypeName() {
		return DottedPath.of(simpleName);
	}
	
	@Override
	public DottedPath getPackageName() {
		return packageName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends T> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
		return (Class<? extends T>) Class.forName(getCanonicalName().toString(), true, classLoader).asSubclass(rawSupertype);
	}

	@Override
	public JavaTypeDeclaration<? super T> getSuperclassDeclaration() {
		if (rawSupertype.isInterface()) {
			return JavaClass.OBJECT;
		}
		return JavaTypeDeclaration.from(rawSupertype);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JavaClass<? super T> getSuperclass() {
		if (rawSupertype.isInterface()) {
			return JavaClass.OBJECT;
		}
		return (JavaClass<? super T>) JavaClass.from(supertype, Collections.emptyMap());
	}
	
	@Override
	public List<JavaTypeDeclaration<?>> getInterfaceDeclarations() {
		if (rawSupertype.isInterface()) {
			return Arrays.asList(JavaTypeDeclaration.from(rawSupertype));
		}
		return Collections.emptyList();
	}
	
	@Override
	public List<JavaClass<?>> getInterfaces() {
		if (rawSupertype.isInterface()) {
			return Arrays.asList(JavaClass.from(supertype, Collections.emptyMap()));
		}
		return Collections.emptyList();
	}

	@Override
	public boolean extendsDeclaration(JavaTypeDeclaration<?> other) {
		return this.equals(other) || OBJECT.equals(other);
	}

	@Override
	public boolean isFinal() {
		return false;
	}
}
