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
import com.regnosys.rosetta.codegen.support.StringCodeWriter;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaFileScope;
import com.regnosys.rosetta.generator.java.util.ImportingCodeWriter;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaTypeDeclaration;

/**
 * Base class for generators that produce a Java class using the fluent
 * {@link CodeRenderer} API. This is the fluent counterpart of
 * {@link XtendJavaClassGenerator}, which it will replace once all generators
 * are migrated.
 *
 * <p>The renderer returned by {@link #generateClass} is rendered <em>twice</em>:
 * a first pass gathers imports and claims identifiers in the file scope, and a
 * second pass produces the class body with all identifiers resolved. The renderer
 * must therefore be free of side effects; in particular, all identifiers must be
 * created in {@code generateClass} itself, never inside the returned renderer.
 */
public abstract class FluentJavaClassGenerator<T, C extends JavaTypeDeclaration<?>> extends JavaClassGenerator<T, C> {
	protected abstract CodeRenderer generateClass(T object, C typeRepresentation, String version, JavaClassScope scope);

	@Override
	protected String generate(T object, C typeRepresentation, String version, JavaClassScope scope) {
		CodeRenderer classCode = generateClass(object, typeRepresentation, version, scope);
		return buildClass(typeRepresentation.getPackageName(), classCode, scope.getFileScope());
	}

	/**
	 * Given the body of a Java class represented as a {@link CodeRenderer},
	 * generate a full Java class file by adding imports and resolving identifiers.
	 */
	protected String buildClass(DottedPath packageName, CodeRenderer classCode, JavaFileScope fileScope) {
		if (fileScope.isClosed()) {
			throw new IllegalStateException("The top scope may not be closed, as imports will be added to it.");
		}
		// First pass: register identifiers in the file scope and collect imports.
		ImportingCodeWriter imports = new ImportingCodeWriter(fileScope, false);
		classCode.render(imports);

		// Second pass: render the class body with all identifiers resolved.
		ImportingCodeWriter body = new ImportingCodeWriter(fileScope);
		classCode.render(body);

		StringCodeWriter result = new StringCodeWriter();
		result.writeln("package ", packageName, ";");
		result.newline();
		imports.getImports().forEach(imp -> result.writeln("import ", imp, ";"));
		result.newline();
		imports.getStaticImports().forEach(imp -> result.writeln("import static ", imp, ";"));
		result.newline();
		String bodyCode = body.toString();
		result.write(bodyCode);
		if (!bodyCode.endsWith("\n")) {
			result.newline();
		}
		return result.toString();
	}
}
