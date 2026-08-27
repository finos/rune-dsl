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

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.support.StringCodeWriter;
import com.regnosys.rosetta.generator.java.scoping.JavaFileScope;
import com.rosetta.util.DottedPath;

/**
 * Given the body of a Java class represented as a {@link CodeRenderer},
 * generates a full Java class file by adding imports and resolving identifiers.
 * The fluent counterpart of {@link ImportManagerExtension}.
 */
public class FluentImportManager {
	public String buildClass(DottedPath packageName, CodeRenderer classCode, JavaFileScope fileScope) {
		if (fileScope.isClosed()) {
			throw new IllegalStateException("The top scope may not be closed, as imports will be added to it.");
		}
		RecordingCodeWriter recording = new RecordingCodeWriter(fileScope);
		classCode.render(recording);

		StringCodeWriter result = new StringCodeWriter();
		result.writeln("package ", packageName, ";");
		result.newline();
		recording.getImports().forEach(imp -> result.writeln("import ", imp, ";"));
		result.newline();
		recording.getStaticImports().forEach(imp -> result.writeln("import static ", imp, ";"));
		result.newline();
		recording.replay(result);
		if (!result.toString().endsWith("\n")) {
			result.newline();
		}
		return result.toString();
	}
}
