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

package com.regnosys.rosetta.generator.java.statement;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.rosetta.util.types.JavaType;

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-14.html#jls-LocalVariableDeclarationStatement
 * 
 * Examples:
 * - `final int x = 42;`
 * - `float y;`
 * 
 * See `JavaStatement` for more documentation.
 */
public class JavaLocalVariableDeclarationStatement extends JavaStatement {
	private final boolean isFinal;
	private final JavaType variableType;
	private final GeneratedIdentifier variableId;
	private final JavaExpression initialExpression;

	public JavaLocalVariableDeclarationStatement(boolean isFinal, JavaType variableType, GeneratedIdentifier variableId) {
		this(isFinal, variableType, variableId, null);
	}
	public JavaLocalVariableDeclarationStatement(boolean isFinal, JavaType variableType, GeneratedIdentifier variableId, JavaExpression initialExpression) {
		this.isFinal = isFinal;
		this.variableType = variableType;
		this.variableId = variableId;
		this.initialExpression = initialExpression;
	}
	
	@Override
	public void appendTo(TargetStringConcatenation target) {
		if (isFinal) {
			target.append("final ");
		}
		target.append(variableType);
		target.append(" ");
		target.append(variableId);
		if (initialExpression != null) {
			target.append(" = ");
			target.append(initialExpression);
		}
		target.append(";");
	}
}
