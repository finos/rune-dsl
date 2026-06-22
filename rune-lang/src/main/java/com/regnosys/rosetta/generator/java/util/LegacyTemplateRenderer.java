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

import org.eclipse.xtend2.lib.StringConcatenationClient;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.generator.TargetLanguageStringConcatenation;
import com.regnosys.rosetta.generator.java.types.JavaTypeRepresentation;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaType;

/**
 * Migration bridge: adapts a Java class body that is still produced as a legacy
 * Xtend template ({@link StringConcatenationClient}) into a {@link CodeRenderer},
 * so that a generator can move to the fluent {@code FluentJavaClassGenerator} API
 * before its template has been rewritten with the fluent {@code CodeWriter} API.
 *
 * <p>Layout (indentation, line breaks) is produced by Xtend's own
 * {@code StringConcatenation}, exactly as the legacy
 * {@code ImportManagerExtension#buildClass} does; the embedded {@code JavaClass}es
 * and reflective {@code Method}s are resolved against the {@link RecordingCodeWriter}
 * so that the fluent file builder collects their imports and resolves their
 * identifiers. The output is therefore identical to the legacy path.
 *
 * <p>To be removed once all generators produce their bodies via the fluent API.
 */
public final class LegacyTemplateRenderer {
	private LegacyTemplateRenderer() {
	}

	/**
	 * Wraps a legacy Xtend class-body template as a fluent {@link CodeRenderer}.
	 */
	public static CodeRenderer asCodeRenderer(StringConcatenationClient template) {
		return out -> render(template, out);
	}

	private static void render(StringConcatenationClient template, CodeWriter out) {
		if (!(out instanceof RecordingCodeWriter recording)) {
			throw new IllegalStateException(
					"A legacy template can only be rendered into a " + RecordingCodeWriter.class.getSimpleName()
							+ ", which collects imports and resolves identifiers.");
		}
		ImportingConcatenation concatenation = new ImportingConcatenation(recording);
		concatenation.append(concatenation.preprocess(template));
		out.write(concatenation.toString());
	}

	/**
	 * The legacy {@code ImportingStringConcatenation}, but registering imports and
	 * resolving identifiers through a {@link RecordingCodeWriter} (whose import
	 * resolution logic it shares) instead of directly against the file scope.
	 */
	private static final class ImportingConcatenation extends TargetLanguageStringConcatenation {
		private final RecordingCodeWriter out;

		ImportingConcatenation(RecordingCodeWriter out) {
			this.out = out;
		}

		@Override
		protected Object handle(Object object) {
			if (object instanceof JavaClass<?> clazz) {
				return out.getOrImportIdentifier(clazz, clazz.getPackageName(), clazz.getNestedTypeName());
			} else if (object instanceof PreferWildcardImportClass clazz) {
				var javaClass = clazz.getJavaClass();
				return out.getOrWildcardImportIdentifier(javaClass, javaClass.getPackageName(), javaClass.getNestedTypeName());
			} else if (object instanceof Method method) {
				return out.getOrStaticImportIdentifier(method, DottedPath.splitOnDots(method.getDeclaringClass().getCanonicalName()), method.getName());
			} else if (object instanceof PreferWildcardImportMethod preferWildcardMethod) {
				var method = preferWildcardMethod.getMethod();
				return out.getOrStaticWildcardImportIdentifier(method, DottedPath.splitOnDots(method.getDeclaringClass().getCanonicalName()), method.getName());
			}
			return super.handle(object);
		}

		@Override
		protected Object normalize(Object object) {
			var normalized = super.normalize(object);
			var type = JavaType.from(normalized);
			if (type == null) {
				return normalized;
			}
			if (type instanceof JavaClass) {
				if (type instanceof JavaParameterizedType) {
					return new JavaTypeRepresentation(type);
				}
				return type;
			}
			return new JavaTypeRepresentation(type);
		}
	}
}
