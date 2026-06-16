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

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.codegen.support.StringCodeWriter;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.scoping.JavaFileScope;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;

class RecordingCodeWriterTest {
	private JavaFileScope scope;
	private RecordingCodeWriter out;

	@BeforeEach
	void setUp() {
		scope = new JavaFileScope("Test.java", DottedPath.splitOnDots("test.ns"));
		out = new RecordingCodeWriter(scope);
	}

	private String replayToString() {
		StringCodeWriter target = new StringCodeWriter();
		out.replay(target);
		return target.toString();
	}

	@Test
	void identifiersMayBeClaimedAfterTheyAffectEarlierWrites() {
		GeneratedIdentifier first = scope.createUniqueIdentifier("foo");
		out.writeln("a: ", first);
		// Claimed only after `first` was already written; both resolve consistently at replay.
		GeneratedIdentifier second = scope.createUniqueIdentifier("foo");
		out.write("b: ", second);

		assertEquals("""
				a: foo0
				b: foo1""", replayToString());
	}

	@Test
	void writingClassRegistersImport() {
		out.write(JavaClass.from(BigDecimal.class));

		assertEquals("BigDecimal", replayToString());
		assertEquals(List.of(DottedPath.splitOnDots("java.math.BigDecimal")), out.getImports());
	}

	@Test
	void javaLangClassNeedsNoImport() {
		out.write(JavaClass.from(String.class));

		assertEquals("String", replayToString());
		assertEquals(List.of(), out.getImports());
	}

	@Test
	void indentationIsAppliedByReplayTarget() {
		out.writeln("{");
		out.indented(() -> out.writeln("body"));
		out.write("}");

		assertEquals("""
				{
				    body
				}""", replayToString());
	}

	@Test
	void replayCanBeRepeated() {
		out.write("code");

		assertEquals("code", replayToString());
		assertEquals("code", replayToString());
	}
}
