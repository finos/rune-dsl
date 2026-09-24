/*
 * Copyright 2026 REGnosys
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
import java.util.Arrays;

import org.eclipse.xtend2.lib.StringConcatenationClient;

import com.regnosys.rosetta.generator.java.scoping.JavaFileScope;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;

/**
 * @deprecated Part of the legacy Xtend template machinery; superseded by
 * FluentJavaClassGenerator#buildClass and removed once all generators are
 * migrated to the fluent CodeWriter API.
 */
@Deprecated
public class ImportManagerExtension {
	public Method method(Class<?> clazz, String methodName) {
		return Arrays.stream(clazz.getMethods())
				.filter(m -> m.getName().equals(methodName))
				.findFirst()
				.orElse(null);
	}

	public PreferWildcardImportClass importWildcard(Class<?> clazz) {
		return importWildcard(JavaClass.from(clazz));
	}

	public PreferWildcardImportClass importWildcard(JavaClass<?> t) {
		return new PreferWildcardImportClass(t);
	}

	public PreferWildcardImportMethod importWildcard(Method method) {
		return new PreferWildcardImportMethod(method);
	}

	/**
	 * Given the body of a Java class represented as a StringConcatenationClient,
	 * generate a full Java class file by adding imports and resolving identifiers.
	 */
	public String buildClass(DottedPath packageName, StringConcatenationClient classCode, JavaFileScope fileScope) {
		if (fileScope.isClosed()) {
			throw new IllegalStateException("The top scope may not be closed, as imports will be added to it.");
		}
		ImportingStringConcatenation isc = new ImportingStringConcatenation(fileScope);
		StringConcatenationClient resolvedCode = isc.preprocess(classCode);
		StringConcatenationClient fullClass = new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append("package ");
				target.append(packageName);
				target.append(";");
				target.newLineIfNotEmpty();
				target.newLine();
				for (DottedPath imp : isc.getImports()) {
					target.append("import ");
					target.append(imp);
					target.append(";");
					target.newLineIfNotEmpty();
				}
				target.newLine();
				for (DottedPath imp : isc.getStaticImports()) {
					target.append("import static ");
					target.append(imp);
					target.append(";");
					target.newLineIfNotEmpty();
				}
				target.newLine();
				target.append(resolvedCode);
				target.newLineIfNotEmpty();
			}
		};
		isc.append(fullClass);
		return isc.toString();
	}
}
