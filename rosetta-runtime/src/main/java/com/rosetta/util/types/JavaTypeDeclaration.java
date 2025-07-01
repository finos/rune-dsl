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

import java.util.List;
import java.util.Map;

import com.rosetta.util.DottedPath;

public interface JavaTypeDeclaration<T> {
	public static <T> JavaTypeDeclaration<T> from(Class<T> c) {
		if (c.getTypeParameters().length >= 1) {
			return JavaGenericTypeDeclaration.from(c);
		}
		return JavaClass.from(c);
	}
	
	DottedPath getPackageName();
	DottedPath getNestedTypeName();
	default String getSimpleName() {
		return getNestedTypeName().last();
	}
	default DottedPath getCanonicalName() {
		return getPackageName().concat(getNestedTypeName());
	}
	
	JavaTypeDeclaration<? super T> getSuperclassDeclaration();
	List<? extends JavaTypeDeclaration<?>> getInterfaceDeclarations();
	boolean extendsDeclaration(JavaTypeDeclaration<?> other);
	boolean isFinal();
	JavaClass<T> applySubstitution(Map<JavaTypeVariable, JavaTypeArgument> substitution);
	Class<? extends T> loadClass(ClassLoader classLoader) throws ClassNotFoundException;
}
