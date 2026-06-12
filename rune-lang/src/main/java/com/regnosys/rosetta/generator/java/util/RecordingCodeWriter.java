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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.scoping.JavaFileScope;
import com.regnosys.rosetta.generator.java.types.JavaTypeRepresentation;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaType;

/**
 * A code writer for Java files which records what is written instead of
 * producing text immediately. While recording, written {@link JavaType}s and
 * reflective {@link Method}s are resolved against the file scope, registering
 * an import for them when possible. The recording can afterwards be written to
 * another code writer using {@link #replay(CodeWriter)}, at which point all
 * generated identifiers are resolved to their actual names.
 *
 * <p>Recording allows a class body to be rendered in a single pass, even though
 * the imports it determines precede the body in the generated file, and
 * identifier names can only be resolved once all identifiers in the file have
 * been claimed.
 *
 * <p>Migration note: this is the fluent counterpart of
 * {@code ImportingStringConcatenation} (which preprocesses and replays in a
 * similar way), and intentionally duplicates its import resolution logic. The
 * latter will be deleted once all generators use the fluent API.
 */
public class RecordingCodeWriter implements CodeWriter {
	private final Map<DottedPath, DottedPath> imports = new HashMap<>();
	private final Map<DottedPath, DottedPath> staticImports = new HashMap<>();

	private final List<Consumer<CodeWriter>> recording = new ArrayList<>();
	private final JavaFileScope scope;
	private int indentation = 0;

	public RecordingCodeWriter(JavaFileScope topScope) {
		this.scope = topScope;
	}

	/**
	 * Writes the recorded code to the given writer, resolving all generated
	 * identifiers to their actual names.
	 */
	public void replay(CodeWriter target) {
		recording.forEach(op -> op.accept(target));
	}

	@Override
	public void write(Object object) {
		if (object == null) {
			return;
		}
		Object processed = handle(normalize(object));
		if (processed instanceof GeneratedIdentifier identifier) {
			// Identifiers are recorded as is: their actual name is only known
			// once all identifiers in the file have been claimed.
			recording.add(target -> target.write(identifier));
			return;
		}
		if (processed instanceof CodeRenderer renderer) {
			renderer.render(this);
			return;
		}
		String text = processed.toString();
		recording.add(target -> target.write(text));
	}

	@Override
	public void newline() {
		recording.add(CodeWriter::newline);
	}

	@Override
	public void indent() {
		indentation++;
		recording.add(CodeWriter::indent);
	}

	@Override
	public void dedent() {
		if (indentation == 0) {
			throw new IllegalStateException("Cannot dedent below zero");
		}
		indentation--;
		recording.add(CodeWriter::dedent);
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
