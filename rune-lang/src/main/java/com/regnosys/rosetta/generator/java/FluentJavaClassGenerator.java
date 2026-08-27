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

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.util.FluentImportManager;
import com.regnosys.rosetta.generator.java.util.RecordingCodeWriter;
import com.rosetta.util.types.JavaTypeDeclaration;

import jakarta.inject.Inject;

/**
 * Base class for generators that produce a Java class using the fluent
 * {@link CodeRenderer} API. This is the fluent counterpart of
 * {@link XtendJavaClassGenerator}, which it will replace once all generators
 * are migrated.
 *
 * <p>The renderer returned by {@link #generateClass} is rendered once into a
 * {@link RecordingCodeWriter}, which collects imports and claims identifiers
 * in the file scope; the recording is then replayed with all identifiers
 * resolved to produce the final file. Identifiers may therefore be created
 * while rendering.
 */
public abstract class FluentJavaClassGenerator<T, C extends JavaTypeDeclaration<?>> extends JavaClassGenerator<T, C> {
	@Inject
	private FluentImportManager importManager;

	protected abstract CodeRenderer generateClass(T object, C typeRepresentation, String version, JavaClassScope scope);

	@Override
	protected String generate(T object, C typeRepresentation, String version, JavaClassScope scope) {
		CodeRenderer classCode = generateClass(object, typeRepresentation, version, scope);
		return importManager.buildClass(typeRepresentation.getPackageName(), classCode, scope.getFileScope());
	}
}
