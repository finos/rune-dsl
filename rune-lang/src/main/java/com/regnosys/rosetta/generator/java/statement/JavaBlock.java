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

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-14.html#jls-Block
 * 
 * Example:
 * ```
 * {
 *     int x = 42;
 *     return x;
 * }
 * ```
 * 
 * See `JavaStatement` for more documentation.
 */
public class JavaBlock extends JavaStatement implements JavaLambdaBody {
	
	public final static JavaBlock EMPTY = new JavaBlock(new JavaStatementList());

	private final JavaStatementList statements;
	
	public JavaBlock(JavaStatementList statements) {
		this.statements = statements;
	}

	@Override
	public void appendTo(TargetStringConcatenation target) {
		target.append('{');
		target.newLine();
		target.append("\t");
		target.append(statements, "\t");
		target.newLine();
		target.append('}');
	}
	
	@Override
	public JavaBlock toBlock() {
		return this;
	}
	
	@Override
	public JavaStatementList asStatementList() {
		return statements;
	}
	
	@Override
	public JavaBlock prepend(JavaStatement other) {
		if (other instanceof JavaBlock) {
			return ((JavaBlock) other).append(this);
		}
		JavaStatementList newStatements = new JavaStatementList();
		newStatements.add(other);
		newStatements.addAll(this.statements);
		return new JavaBlock(newStatements);
	}
	@Override
	public JavaBlock append(JavaStatement other) {
		if (other instanceof JavaBlock) {
			return this.append((JavaBlock)other);
		}
		JavaStatementList newStatements = new JavaStatementList();
		newStatements.addAll(this.statements);
		newStatements.add(other);
		return new JavaBlock(newStatements);
	}
	public JavaBlock append(JavaBlock other) {
		JavaStatementList newStatements = new JavaStatementList();
		newStatements.addAll(this.statements);
		newStatements.addAll(other.statements);
		return new JavaBlock(newStatements);
	}
}
