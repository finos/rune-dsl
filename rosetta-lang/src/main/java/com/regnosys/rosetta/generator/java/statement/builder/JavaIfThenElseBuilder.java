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

package com.regnosys.rosetta.generator.java.statement.builder;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.xtend2.lib.StringConcatenation;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.JavaBlock;
import com.regnosys.rosetta.generator.java.statement.JavaIfThenElseStatement;
import com.regnosys.rosetta.generator.java.statement.JavaIfThenStatement;
import com.regnosys.rosetta.generator.java.statement.JavaLambdaBody;
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatementList;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.rosetta.util.types.JavaType;

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-14.html#jls-IfThenElseStatement
 * 
 * Example:
 * ```
 * if (cond) {
 *     int x = 42;
 *     x
 * } else {
 *     -1
 * }
 * ```
 * 
 * See `JavaStatementBuilder` for more documentation.
 */
public class JavaIfThenElseBuilder extends JavaStatementBuilder {
	private final JavaExpression condition;
	private final JavaStatementBuilder thenBranch;
	private final JavaStatementBuilder elseBranch;
	private final JavaType commonType;
	
	private final JavaTypeUtil typeUtil;

	public JavaIfThenElseBuilder(JavaExpression condition, JavaStatementBuilder thenBranch, JavaStatementBuilder elseBranch, JavaType commonType, JavaTypeUtil typeUtil) {
		this.condition = condition;
		this.thenBranch = thenBranch;
		this.elseBranch = elseBranch;
		this.commonType = commonType;
		
		this.typeUtil = typeUtil;
	}
	public JavaIfThenElseBuilder(JavaExpression condition, JavaStatementBuilder thenBranch, JavaStatementBuilder elseBranch, JavaTypeUtil typeUtil) {
		this(condition, thenBranch, elseBranch, typeUtil.join(thenBranch.getExpressionType(), elseBranch.getExpressionType()), typeUtil);
	}
	
	@Override
	public JavaType getExpressionType() {
		return commonType;
	}
	
	@Override
	public JavaIfThenElseBuilder mapExpression(Function<JavaExpression, ? extends JavaStatementBuilder> mapper) {
		JavaStatementBuilder mappedThenBranch = thenBranch.mapExpression(mapper);
		JavaStatementBuilder mappedElseBranch = elseBranch.mapExpression(mapper);
		return new JavaIfThenElseBuilder(condition, mappedThenBranch, mappedElseBranch, typeUtil);
	}

	@Override
	public JavaStatementBuilder then(JavaStatementBuilder after, BiFunction<JavaExpression, JavaExpression, JavaStatementBuilder> combineExpressions, JavaStatementScope scope) {
		return this.collapseToSingleExpression(scope)
				.then(after, combineExpressions, scope);
	}

	private JavaIfThenElseStatement completeBranches(Function<JavaStatementBuilder, JavaStatement> mapper) {
		return new JavaIfThenElseStatement(condition, mapper.apply(thenBranch), mapper.apply(elseBranch));
	}
	@Override
	public JavaIfThenElseStatement complete(Function<JavaExpression, JavaStatement> completer) {
		return completeBranches(b -> b.complete(completer));
	}
	@Override
	public JavaBlock completeAsReturn() {
		return new JavaIfThenStatement(condition, thenBranch.completeAsReturn())
				.append(elseBranch.completeAsReturn());
	}
	@Override
	public JavaIfThenElseStatement completeAsExpressionStatement() {
		return completeBranches(b -> b.completeAsExpressionStatement());
	}
	@Override
	public JavaIfThenElseStatement completeAsAssignment(GeneratedIdentifier variableId) {
		return completeBranches(b -> b.completeAsAssignment(variableId));
	}

	@Override
	public JavaBlockBuilder declareAsVariable(boolean isFinal, String variableId, JavaStatementScope scope) {
		GeneratedIdentifier id = scope.createIdentifier(this, variableId);
		if (elseBranch instanceof JavaLiteral) {
			return new JavaBlockBuilder(
					JavaStatementList.of(
						new JavaLocalVariableDeclarationStatement(false, commonType, id, (JavaLiteral) elseBranch),
						new JavaIfThenStatement(condition, thenBranch.completeAsAssignment(id))
					),
					new JavaVariable(id, commonType)
				);
		}
		return new JavaBlockBuilder(
				JavaStatementList.of(
					new JavaLocalVariableDeclarationStatement(isFinal, commonType, id),
					this.completeAsAssignment(id)
				),
				new JavaVariable(id, commonType)
			);
	}
	
	@Override
	public JavaStatementBuilder collapseToSingleExpression(JavaStatementScope scope) {
		return this.declareAsVariable(true, "ifThenElseResult", scope);
	}

	@Override
	public JavaLambdaBody toLambdaBody() {
		return new JavaBlockBuilder(this).toLambdaBody();
	}
	
	@Override
	public String toString() {
		StringConcatenation result = new StringConcatenation();
		result.append("if (");
		result.append(condition);
		result.append(") ");
		// Wrapping in a `JavaBlockBuilder` will make sure that the `then` branch is always enclosed in curly braces.
		// This is a style preference, and is technically not necessary.
		result.append(toBlock(thenBranch));
		result.append(" else ");
		if (elseBranch instanceof JavaIfThenElseBuilder) {
			result.append(elseBranch);
		} else {
			result.append(toBlock(elseBranch));
		}
		return result.toString();
	}
	private JavaBlockBuilder toBlock(JavaStatementBuilder stat) {
		if (stat instanceof JavaBlockBuilder) {
			return (JavaBlockBuilder) stat;
		} else {
			return new JavaBlockBuilder(stat);
		}
	}
}
