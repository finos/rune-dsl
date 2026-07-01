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

import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;

/**
 * Migration bridge: streams a fragment of legacy Xtend template code
 * ({@link StringConcatenationClient}) into a fluent {@link CodeWriter}. Each
 * appended object - in particular embedded {@code JavaClass}es and reflective
 * {@code Method}s - is forwarded to {@link CodeWriter#write}, so that a
 * {@code RecordingCodeWriter} registers its imports and defers its identifier
 * resolution exactly like natively-rendered fluent code.
 *
 * <p>This lets a generator that has moved to the fluent {@code CodeWriter} API
 * keep embedding small fragments still produced as Xtend templates by
 * not-yet-migrated code (e.g. method parameter lists or call arguments): wrap
 * the fragment with {@link #asCodeRenderer}. Unlike resolving the template to a
 * string up front, this keeps the surrounding scope open so the rest of the
 * class can still claim identifiers while rendering. It is therefore suited to
 * inline fragments rather than whole, multi-line class bodies.
 */
public final class CodeWriterTargetStringConcatenation implements TargetStringConcatenation {
	private final CodeWriter out;
	private boolean lineHasContent = false;

	public CodeWriterTargetStringConcatenation(CodeWriter out) {
		this.out = out;
	}

	/**
	 * Wraps a legacy Xtend template fragment as a fluent {@link CodeRenderer}.
	 */
	public static CodeRenderer asCodeRenderer(StringConcatenationClient template) {
		return out -> StringConcatenationClient.appendTo(template, new CodeWriterTargetStringConcatenation(out));
	}

	@Override
	public int length() {
		throw new UnsupportedOperationException();
	}

	@Override
	public char charAt(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void newLineIfNotEmpty() {
		if (lineHasContent) {
			newLine();
		}
	}

	@Override
	public void newLine() {
		out.newline();
		lineHasContent = false;
	}

	@Override
	public void appendImmediate(Object object, String indentation) {
		append(object, indentation);
	}

	@Override
	public void append(Object object, String indentation) {
		if (object instanceof String value) {
			appendString(value, indentation);
		} else {
			append(object);
		}
	}

	@Override
	public void append(Object object) {
		if (object == null) {
			return;
		}
		if (object instanceof StringConcatenationClient client) {
			StringConcatenationClient.appendTo(client, this);
		} else {
			out.write(object);
			markWritten(object);
		}
	}

	private void appendString(String value, String indentation) {
		String[] lines = value.split("\n", -1);
		for (int i = 0; i < lines.length; i++) {
			if (i > 0) {
				newLine();
				if (!lines[i].isEmpty() && !indentation.isEmpty()) {
					out.write(indentation);
					lineHasContent = true;
				}
			}
			if (!lines[i].isEmpty()) {
				out.write(lines[i]);
				lineHasContent = true;
			}
		}
	}

	// Best-effort heuristic for newLineIfNotEmpty: infers whether the written object
	// left content on the current line from its toString(), which for identifiers and
	// renderers may not equal the rendered text. Good enough for the migration bridge.
	private void markWritten(Object object) {
		String value = object.toString();
		int lastNewline = value.lastIndexOf('\n');
		if (lastNewline >= 0) {
			lineHasContent = lastNewline < value.length() - 1;
		} else if (!value.isEmpty()) {
			lineHasContent = true;
		}
	}
}
