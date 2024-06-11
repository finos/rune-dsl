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

package com.regnosys.rosetta.generator.java;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.GeneratorScope;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaType;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.SourceVersion;

public class JavaScope extends GeneratorScope<JavaScope> {
	private final Set<DottedPath> defaultPackages = new HashSet<>();
	
	public JavaScope(DottedPath packageName) {
		super("Package[" + packageName.withDots() + "]");
		this.defaultPackages.add(DottedPath.of("java", "lang"));
		this.defaultPackages.add(packageName);
	}
	protected JavaScope(String description, JavaScope parent) {
		super(description, parent);
	}

	@Override
	public JavaScope childScope(String description) {
		return new JavaScope(description, this);
	}
	public JavaScope classScope(String className) {
		return childScope("Class[" + className + "]");
	}
	public JavaScope methodScope(String methodName) {
		return childScope("Method[" + methodName + "]");
	}
	public JavaScope lambdaScope() {
		return childScope("Lambda[]");
	}

	@Override
	public boolean isValidIdentifier(String name) {
		return SourceVersion.isName(name);
	}
	
	// Make sure identifiers from package "java.lang" are always in scope.
	@Override
	public Optional<GeneratedIdentifier> getIdentifier(Object obj) {
		return super.getIdentifier(obj).or(() -> {
			JavaType t = JavaType.from(obj);
			if (t != null) {
				if (t instanceof JavaClass) {
					JavaClass<?> clazz = (JavaClass<?>)t;
					String desiredName = clazz.getSimpleName();
					if (this.getIdentifiers().stream().anyMatch(id -> id.getDesiredName().equals(desiredName))) {
						// Another class with the same name is already imported. Use the canonical name instead.
						return Optional.of(overwriteIdentifier(clazz, clazz.getCanonicalName().withDots()));
					}
					if (this.defaultPackages.contains(clazz.getPackageName())) {
						// Classes from namespaces that are implicitly imported can be directly referenced.
						return Optional.of(overwriteIdentifier(clazz, clazz.getSimpleName()));
					}
					// The class needs an import first.
					return Optional.empty();
				}
			}
			return Optional.empty();
		});
	}
}
