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

import org.eclipse.xtend2.lib.StringConcatenationClient;

import com.regnosys.rosetta.generator.DebuggingTargetLanguageStringConcatenation;
import com.regnosys.rosetta.generator.TargetLanguageRepresentation;
import com.regnosys.rosetta.generator.java.statement.builder.JavaBlockBuilder;
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder;

/**
 * A representation of a statement in Java. Examples:
 * - a return statement `return foo;`
 * - a local variable declaration: `int x = 42;`
 * - an if-then statement: `if (cond) return foo;`
 * - a block of statements:
 * ```
 * {
 *     int x = 42;
 *     if (cond) return foo;
 *     return x;
 * }
 * ```
 * 
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-14.html#jls-BlockStatement
 */
public abstract class JavaStatement implements TargetLanguageRepresentation {
	/**
	 * Convert this statement into a block enclosed by curly braces, if this statement is not already a block.
	 * 
	 * For example, given the statement `return x;`, the following block statement will be returned:
	 * ```
	 * {
	 *     return x;
	 * }
	 * ```
	 */
	public JavaBlock toBlock() {
		return new JavaBlock(JavaStatementList.of(this));
	}
	/*
	 * Get the list of statements in this block, or a singleton list if this is not a block.
	 * 
	 * For example, given a block of statements,
	 * ```
	 * {
	 *     int x = 42;
	 *     if (cond) return foo;
	 *     return x;
	 * }
	 * ```
	 * this would return a list containing the elements `int x = 42;`, `if (cond) return foo;` and `return x;`.
	 */
	public JavaStatementList asStatementList() {
		return JavaStatementList.of(this);
	}
	/*
	 * Prepend the given statement to this one. Behaves the same as `other.append(this)`.
	 * 
	 * See #append for examples.
	 */
	public JavaBlock prepend(JavaStatement other) {
		if (other instanceof JavaBlock) {
			return other.append(this);
		}
		return new JavaBlock(JavaStatementList.of(other, this));
	}
	/**
	 * Append the given statement to this one. Behaves the same as `other.prepend(this)`.
	 * 
	 * This operation flattens block statements. Examples:
	 * - given the statements `int x = 42;` and `return x;`, appending the latter to the former results in the following block:
	 * ```
	 * {
	 *     int x = 42;
	 *     return x;
	 * }
	 * ```
	 * - given a statement `int x = 42;` and a block
	 * ```
	 * {
	 *     int y = x + 1;
	 *     return y;
	 * }
	 * ```
	 *   appending the latter to the former results in the following block:
	 * ```
	 * {
	 *     int x = 42;
	 *     int y = x + 1;
	 *     return y;
	 * }
	 * ```
	 */
	public JavaBlock append(JavaStatement other) {
		if (other instanceof JavaBlock) {
			return other.prepend(this);
		}
		return new JavaBlock(JavaStatementList.of(this, other));
	}
	/**
	 * Append the given statement builder to this statement. Behaves the same as `builder.prepend(this)`.
	 * 
	 * This operation flattens block statements. See #append(JavaStatement) for examples.
	 * ```
	 */
	public JavaBlockBuilder append(JavaStatementBuilder builder) {
		return new JavaBlockBuilder(this.asStatementList(), builder);
	}
	
	@Override
	public String toString() {
		return DebuggingTargetLanguageStringConcatenation.convertToDebugString(this);
	}
}
