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

package com.regnosys.rosetta.generator.java.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.regnosys.rosetta.codegen.api.TargetLanguageRepresentation;
import com.regnosys.rosetta.codegen.support.StringCodeWriter;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.scoping.JavaFileScope;
import com.regnosys.rosetta.generator.java.types.JavaTypeRepresentation;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaType;

/**
 * A code writer for Java files which resolves written {@link JavaType}s and
 * reflective {@link Method}s to identifiers in the given file scope, registering
 * an import for them when possible. The collected imports can be retrieved with
 * {@link #getImports()} and {@link #getStaticImports()}.
 *
 * <p>When constructed with {@code resolveIdentifiers} set to {@code false},
 * generated identifiers are written using their desired name instead of their
 * resolved actual name. This is used for the import-gathering pass, which runs
 * before identifier names are final. See {@link com.regnosys.rosetta.generator.java.FluentJavaClassGenerator}.
 *
 * <p>Migration note: this is the fluent counterpart of {@code ImportingStringConcatenation},
 * and intentionally duplicates its import resolution logic. The latter will be
 * deleted once all generators use the fluent API.
 */
public class ImportingCodeWriter extends StringCodeWriter {
	private final Map<DottedPath, DottedPath> imports = new HashMap<>();
	private final Map<DottedPath, DottedPath> staticImports = new HashMap<>();

	private final JavaFileScope scope;
	private final boolean resolveIdentifiers;

	public ImportingCodeWriter(JavaFileScope topScope) {
		this(topScope, true);
	}

	public ImportingCodeWriter(JavaFileScope topScope, boolean resolveIdentifiers) {
		this.scope = topScope;
		this.resolveIdentifiers = resolveIdentifiers;
	}

	@Override
	public void write(Object object) {
		if (object == null) {
			return;
		}
		Object processed = handle(normalize(object));
		if (!resolveIdentifiers && processed instanceof GeneratedIdentifier identifier) {
			super.write(identifier.getDesiredName());
			return;
		}
		if (processed instanceof TargetLanguageRepresentation representation) {
			representation.render(this);
			return;
		}
		super.write(processed);
	}

	private Object handle(Object object) {
		if (object instanceof JavaClass<?> clazz) {
			return getOrImportIdentifier(clazz, clazz.getPackageName(), clazz.getNestedTypeName());
		} else if (object instanceof PreferWildcardImportClass clazz) {
			var javaClass = clazz.getJavaClass();
			return getOrWildcardImportIdentifier(javaClass, javaClass.getPackageName(), javaClass.getNestedTypeName());
		} else if (object instanceof Method method) {
			return getOrStaticImportIdentifier(method, DottedPath.splitOnDots(method.getDeclaringClass().getCanonicalName()), method.getName());
		} else if (object instanceof PreferWildcardImportMethod preferWildcardMethod) {
			var method = preferWildcardMethod.getMethod();
			return getOrStaticWildcardImportIdentifier(method, DottedPath.splitOnDots(method.getDeclaringClass().getCanonicalName()), method.getName());
		}
		return object;
	}

	private Object normalize(Object object) {
		Object normalized = object;
		if (normalized instanceof Optional<?> optional) {
			normalized = optional.orElseThrow();
		}
		var type = JavaType.from(normalized);
		if (type == null) {
			return normalized;
		}
		if (type instanceof JavaClass && !(type instanceof JavaParameterizedType)) {
			return type;
		}
		return new JavaTypeRepresentation(type);
	}

	public GeneratedIdentifier getOrImportIdentifier(Object object, DottedPath packageName, DottedPath nestedTypeName) {
		return scope.getIdentifier(object)
				.orElseGet(() -> internalDoImportIfPossible(object, nestedTypeName, packageName, topLevelTypeInFile -> addImportIfNotAlreadyImported(packageName, packageName.child(topLevelTypeInFile))));
	}

	public GeneratedIdentifier getOrWildcardImportIdentifier(Object object, DottedPath packageName, DottedPath nestedTypeName) {
		var canonicalName = packageName.concat(nestedTypeName);
		return scope.getIdentifier(object)
				.map(it -> {
					if (imports.containsKey(canonicalName)) {
						addWildcardImport(packageName);
					}
					return it;
				})
				.orElseGet(() -> internalDoImportIfPossible(object, nestedTypeName, packageName, topLevelTypeInFile -> addWildcardImport(packageName)));
	}

	private GeneratedIdentifier internalDoImportIfPossible(Object object, DottedPath nestedTypeName, DottedPath packageName, Consumer<String> addImport) {
		String desiredName = nestedTypeName.withDots();
		String topLevelTypeInFile = nestedTypeName.first();
		DottedPath topLevelCanonicalName = packageName.child(topLevelTypeInFile);
		if (!scope.isNameTaken(desiredName) && isAlreadyAccessible(packageName, topLevelCanonicalName)) {
			return scope.createIdentifier(object, desiredName);
		}
		if (scope.isNameTaken(topLevelTypeInFile)) {
			var canonicalName = packageName.concat(nestedTypeName);
			return scope.createIdentifier(object, canonicalName.withDots());
		}
		addImport.accept(topLevelTypeInFile);
		return scope.createIdentifier(object, desiredName);
	}

	public GeneratedIdentifier getOrStaticImportIdentifier(Object object, DottedPath canonicalClassName, String staticMemberName) {
		var canonicalStaticMemberName = canonicalClassName.child(staticMemberName);
		return scope.getIdentifier(object)
				.orElseGet(() -> internalDoStaticImportIfPossible(object, staticMemberName, canonicalStaticMemberName, () -> addStaticImportIfNotAlreadyImported(canonicalClassName, canonicalStaticMemberName)));
	}

	public GeneratedIdentifier getOrStaticWildcardImportIdentifier(Object object, DottedPath canonicalClassName, String staticMemberName) {
		var canonicalStaticMemberName = canonicalClassName.child(staticMemberName);
		return scope.getIdentifier(object)
				.map(it -> {
					if (staticImports.containsKey(canonicalStaticMemberName)) {
						addStaticWildcardImport(canonicalClassName);
					}
					return it;
				})
				.orElseGet(() -> internalDoStaticImportIfPossible(object, staticMemberName, canonicalStaticMemberName, () -> addStaticWildcardImport(canonicalClassName)));
	}

	private GeneratedIdentifier internalDoStaticImportIfPossible(Object object, String staticMemberName, DottedPath canonicalStaticMemberName, Runnable addImport) {
		if (scope.isNameTaken(staticMemberName)) {
			return scope.createIdentifier(object, canonicalStaticMemberName.withDots());
		}
		addImport.run();
		return scope.createIdentifier(object, staticMemberName);
	}

	public void addImportIfNotAlreadyImported(DottedPath packageName, DottedPath canonicalName) {
		if (needsImport(packageName)) {
			imports.put(canonicalName, packageName);
		}
	}

	private boolean needsImport(DottedPath packageName) {
		return !packageName.equals(scope.getPackageName()) && !imports.containsKey(packageName.child("*"));
	}

	public boolean isAlreadyAccessible(DottedPath packageName, DottedPath canonicalName) {
		return !needsImport(packageName) || imports.containsKey(canonicalName);
	}

	public void addWildcardImport(DottedPath packageName) {
		var wildcard = packageName.child("*");
		if (!packageName.equals(scope.getPackageName()) && imports.put(wildcard, packageName) == null) {
			imports.entrySet().removeIf(imp -> !imp.getKey().equals(wildcard) && imp.getValue().equals(packageName));
		}
	}

	public void addStaticImportIfNotAlreadyImported(DottedPath canonicalClassName, DottedPath canonicalStaticMemberName) {
		if (!staticImports.containsKey(canonicalClassName.child("*"))) {
			staticImports.put(canonicalStaticMemberName, canonicalClassName);
		}
	}

	public void addStaticWildcardImport(DottedPath canonicalClassName) {
		var wildcard = canonicalClassName.child("*");
		if (staticImports.put(wildcard, canonicalClassName) == null) {
			staticImports.entrySet().removeIf(imp -> !imp.getKey().equals(wildcard) && imp.getValue().equals(canonicalClassName));
		}
	}

	public List<DottedPath> getImports() {
		return imports.keySet().stream().sorted().toList();
	}

	public List<DottedPath> getStaticImports() {
		return staticImports.keySet().stream().sorted().toList();
	}
}
