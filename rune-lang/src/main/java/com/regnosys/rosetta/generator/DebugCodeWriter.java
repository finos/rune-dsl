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

package com.regnosys.rosetta.generator;

import java.util.Optional;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriterConfig;
import com.regnosys.rosetta.codegen.support.StringCodeWriter;

/**
 * Renders target language representations to a debug string. A
 * {@link GeneratedIdentifier} is written as its desired name, so code can be
 * rendered before its identifiers are resolvable, e.g., in a {@code toString}.
 */
public final class DebugCodeWriter extends StringCodeWriter {
	private static final CodeWriterConfig CONFIG = CodeWriterConfig.builder().indent("\t").build();
	private static final ThreadLocal<Integer> recursionDepth = ThreadLocal.withInitial(() -> 0);
	private static final int MAX_RECURSION_DEPTH = 3;

	private DebugCodeWriter() {
		super(CONFIG);
	}

	public static String toDebugString(CodeRenderer renderer) {
		int depth = recursionDepth.get();
		if (depth >= MAX_RECURSION_DEPTH) {
			// Prevent infinite recursion during debug string generation
			return getRecursionLimitMessage(renderer);
		}

		recursionDepth.set(depth + 1);
		try {
			DebugCodeWriter out = new DebugCodeWriter();
			out.write(renderer);
			return out.toString();
		} finally {
			recursionDepth.set(depth);
		}
	}

	private static String getRecursionLimitMessage(Object object) {
		String className = object.getClass().getSimpleName();
		if (className.isEmpty()) {
			// Anonymous class - use the full name
			className = object.getClass().getName();
		}
		return "<recursion-limit:" + className + "@" + Integer.toHexString(System.identityHashCode(object)) + ">";
	}

	@Override
	public void write(Object object) {
		if (object instanceof GeneratedIdentifier identifier) {
			super.write(identifier.getDesiredName());
		} else if (object instanceof Optional<?> optional) {
			write(optional.orElseThrow());
		} else {
			super.write(object);
		}
	}
}
