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

package com.regnosys.rosetta.generator.java.scoping;


public class JavaStatementScope extends AbstractJavaScope<AbstractJavaScope<?>> {
	// private final Set<DottedPath> defaultPackages = new HashSet<>();

	JavaStatementScope(String description, AbstractJavaScope<?> parent) {
		super(description, parent);
	}

	public JavaStatementScope lambdaScope() {
		return childScope("Lambda[]");
	}
	private JavaStatementScope childScope(String description) {
		return new JavaStatementScope(description, this);
	}
	
	// Make sure identifiers from package "java.lang" are always in scope.
//	@Override
//	public Optional<GeneratedIdentifier> getIdentifier(Object obj) {
//		return super.getIdentifier(obj).or(() -> {
//			JavaType t = JavaType.from(obj);
//			if (t != null) {
//				if (t instanceof JavaClass) {
//					JavaClass<?> clazz = (JavaClass<?>)t;
//					String desiredName = clazz.getSimpleName();
//					if (this.getIdentifiers().stream().anyMatch(id -> id.getDesiredName().equals(desiredName))) {
//						// Another class with the same name is already imported. Use the canonical name instead.
//						return Optional.of(overwriteIdentifier(clazz, clazz.getCanonicalName().withDots()));
//					}
//					if (this.defaultPackages.contains(clazz.getPackageName())) {
//						// Classes from namespaces that are implicitly imported can be directly referenced.
//						return Optional.of(overwriteIdentifier(clazz, clazz.getSimpleName()));
//					}
//					// The class needs an import first.
//					return Optional.empty();
//				}
//			}
//			return Optional.empty();
//		});
//	}
}
