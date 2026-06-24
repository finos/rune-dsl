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

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.generator.TargetLanguageRepresentation;

/**
 * Migration bridge: lets a fluent {@link CodeRenderer} render into the legacy Xtend
 * template machinery by exposing a {@link CodeWriter} that appends to a
 * {@link TargetStringConcatenation}. This is the counterpart of
 * {@link CodeWriterTargetStringConcatenation}, and lets a value rendered with the
 * fluent API still be embedded in a not-yet-migrated Xtend template (e.g. a
 * {@link TargetLanguageRepresentation} whose {@code appendTo} delegates to its
 * {@code render}). To be removed once all generators use the fluent API.
 */
public final class TargetStringConcatenationCodeWriter implements CodeWriter {
	private static final String INDENT = "    ";

	private final TargetStringConcatenation target;
	private int indent = 0;
	private boolean atStartOfLine = true;

	public TargetStringConcatenationCodeWriter(TargetStringConcatenation target) {
		this.target = target;
	}

	@Override
	public void write(Object object) {
		if (object == null) {
			return;
		}
		// Legacy representations (e.g. generated identifiers) must go through the
		// target's own machinery, which defers identifier resolution until scopes
		// are resolvable and substitutes desired names when debugging.
		if (object instanceof CodeRenderer renderer && !(object instanceof TargetLanguageRepresentation)) {
			renderer.render(this);
			return;
		}
		if (atStartOfLine) {
			target.append(INDENT.repeat(indent));
			atStartOfLine = false;
		}
		target.append(object);
	}

	@Override
	public void newline() {
		target.newLine();
		atStartOfLine = true;
	}

	@Override
	public void indent() {
		indent++;
	}

	@Override
	public void dedent() {
		if (indent == 0) {
			throw new IllegalStateException("Cannot dedent below zero");
		}
		indent--;
	}
}
