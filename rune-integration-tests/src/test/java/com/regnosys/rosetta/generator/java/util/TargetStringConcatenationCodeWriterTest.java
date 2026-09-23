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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Consumer;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.codegen.api.CodeWriter;

class TargetStringConcatenationCodeWriterTest {
	private static String render(Consumer<CodeWriter> code) {
		StringConcatenation result = new StringConcatenation("\n");
		result.append(new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				code.accept(new TargetStringConcatenationCodeWriter(target));
			}
		});
		return result.toString();
	}

	@Test
	void singleLineTextIsIndentedAtStartOfLine() {
		assertEquals("{\n    body\n}", render(out -> {
			out.writeln("{");
			out.indented(() -> out.writeln("body"));
			out.write("}");
		}));
	}

	@Test
	void multiLineTextIsIndentedOnEveryLine() {
		assertEquals("{\n    /**\n     * doc\n     */\n}", render(out -> {
			out.writeln("{");
			out.indented(() -> out.write("/**\n * doc\n */\n"));
			out.write("}");
		}));
	}

	@Test
	void emptyLinesStayEmptyAndCarriageReturnsAreDropped() {
		assertEquals("    first\n\n    second", render(out -> {
			out.indent();
			out.write("first\r\n\r\nsecond");
		}));
	}

	@Test
	void textAfterLastLineFeedLeavesWriterMidLine() {
		assertEquals("    a = 1 +\n    2;", render(out -> {
			out.indent();
			out.write("a = ");
			out.write("1 +\n2");
			out.write(";");
		}));
	}

	@Test
	void textStartingWithLineFeedEndsTheCurrentLine() {
		assertEquals("    a\n    b", render(out -> {
			out.indent();
			out.write("a");
			out.write("\nb");
		}));
	}

	@Test
	void nonStringObjectsAreHandedToTheTargetUnsplit() {
		Object multiLine = new Object() {
			@Override
			public String toString() {
				return "a\nb";
			}
		};
		// The target splits the object's text itself, without this writer's indentation
		assertEquals("    a\nb", render(out -> {
			out.indent();
			out.write(multiLine);
		}));
	}
}
