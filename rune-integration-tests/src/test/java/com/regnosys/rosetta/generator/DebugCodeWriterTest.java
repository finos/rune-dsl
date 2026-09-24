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

package com.regnosys.rosetta.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaPackageName;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaPrimitiveType;

class DebugCodeWriterTest {
	private final JavaStatementScope scope = JavaClassScope
			.createAndRegisterIdentifier(RGeneratedJavaClass.create(JavaPackageName.escape(DottedPath.of("test")), "Foo", Object.class))
			.createMethodScope("bar")
			.getBodyScope();

	@Test
	void generatedIdentifierIsWrittenAsItsDesiredName() {
		GeneratedIdentifier x = scope.createIdentifier(new Object(), "x");
		JavaStatement declaration = new JavaLocalVariableDeclarationStatement(true, JavaPrimitiveType.INT, x,
				JavaExpression.from(out -> out.write("42"), JavaPrimitiveType.INT));

		assertEquals("final int x = 42;", declaration.toString());
	}

	@Test
	void blockIsIndentedWithTabs() {
		GeneratedIdentifier x = scope.createIdentifier(new Object(), "x");
		JavaStatement block = new JavaLocalVariableDeclarationStatement(false, JavaPrimitiveType.INT, x).toBlock();

		assertEquals("{\n\tint x;\n}", block.toString());
	}
}
