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

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-15.html#jls-Assignment
 * 
 * Example: `x = 42;`
 * 
 * See `JavaStatement` for more documentation.
 */
public class JavaAssignment extends JavaStatement {

	private final GeneratedIdentifier variableId;
	private final JavaExpression expression;
	
	public JavaAssignment(GeneratedIdentifier variableId, JavaExpression expression) {
		this.variableId = variableId;
		this.expression = expression;
	}
	
	@Override
	public void appendTo(TargetStringConcatenation target) {
		target.append(variableId);
		target.append(" = ");
		target.append(expression);
		target.append(";");
	}
}
