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
import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.JavaBlock;
import com.regnosys.rosetta.generator.java.statement.JavaLambdaBody;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatementList;
import com.rosetta.util.types.JavaType;

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-14.html#jls-Block
 * 
 * Example:
 * ```
 * {
 *     int x = 42;
 *     x
 * }
 * ```
 * 
 * See `JavaStatementBuilder` for more documentation.
 */
public class JavaBlockBuilder extends JavaStatementBuilder {

	private final JavaStatementList statements;
	private final JavaStatementBuilder lastStatement;

	public JavaBlockBuilder(JavaStatementBuilder lastStatement) {
		this(new JavaStatementList(), lastStatement);
	}
	public JavaBlockBuilder(JavaStatementList statements, JavaStatementBuilder lastStatement) {
		this.statements = statements;
		this.lastStatement = lastStatement;
	}
	
	@Override
	public JavaType getExpressionType() {
		return this.lastStatement.getExpressionType();
	}
	
	@Override
	public JavaBlockBuilder mapExpression(Function<JavaExpression, ? extends JavaStatementBuilder> mapper) {
		return new JavaBlockBuilder(statements, lastStatement.mapExpression(mapper));
	}
	
	@Override
	public JavaBlockBuilder then(JavaStatementBuilder after, BiFunction<JavaExpression, JavaExpression, JavaStatementBuilder> combineExpressions, JavaStatementScope scope) {
		if (after instanceof JavaBlockBuilder) {
			return this.then((JavaBlockBuilder)after, combineExpressions, scope);
		}
		
		JavaStatementList result = new JavaStatementList();
		result.addAll(this.statements);
		
		JavaStatementBuilder combined = this.lastStatement.then(after, combineExpressions, scope);
		if (combined instanceof JavaBlockBuilder) {
			JavaBlockBuilder combinedBlock = (JavaBlockBuilder)combined;
			result.addAll(combinedBlock.statements);
			return new JavaBlockBuilder(result, combinedBlock.lastStatement);
		}
		return new JavaBlockBuilder(result, combined);
	}
	public JavaBlockBuilder then(JavaBlockBuilder after, BiFunction<JavaExpression, JavaExpression, JavaStatementBuilder> combineExpressions, JavaStatementScope scope) {
		JavaStatementList result = new JavaStatementList();
		result.addAll(this.statements);
		result.addAll(after.statements);
		
		JavaStatementBuilder combined = this.lastStatement.then(after.lastStatement, combineExpressions, scope);
		if (combined instanceof JavaBlockBuilder) {
			JavaBlockBuilder combinedBlock = (JavaBlockBuilder)combined;
			result.addAll(combinedBlock.statements);
			return new JavaBlockBuilder(result, combinedBlock.lastStatement);
		}
		return new JavaBlockBuilder(result, combined);
	}
	
	private JavaBlock completeLastStatement(Function<JavaStatementBuilder, JavaStatement> mapper) {
		return new JavaBlock(statements).append(mapper.apply(this.lastStatement));
	}
	@Override
	public JavaBlock complete(Function<JavaExpression, JavaStatement> completer) {
		return completeLastStatement(l -> l.complete(completer));
	}
	@Override
	public JavaBlock completeAsReturn() {
		return completeLastStatement(l -> l.completeAsReturn());
	}
	@Override
	public JavaBlock completeAsExpressionStatement() {
		return completeLastStatement(l -> l.completeAsExpressionStatement());
	}
	@Override
	public JavaBlock completeAsAssignment(GeneratedIdentifier variableId) {
		return completeLastStatement(l -> l.completeAsAssignment(variableId));
	}

	@Override
	public JavaBlockBuilder declareAsVariable(boolean isFinal, String variableId, JavaStatementScope scope) {
		JavaStatementList result = new JavaStatementList();
		result.addAll(this.statements);
		JavaStatementBuilder declaration = this.lastStatement.declareAsVariable(isFinal, variableId, scope);
		scope.createKeySynonym(this, this.lastStatement);
		if (declaration instanceof JavaBlockBuilder) {
			JavaBlockBuilder declarationBlock = (JavaBlockBuilder)declaration;
			result.addAll(declarationBlock.statements);
			return new JavaBlockBuilder(result, declarationBlock.lastStatement);
		}
		return new JavaBlockBuilder(result, declaration);
	}
	
	@Override
	public JavaStatementBuilder collapseToSingleExpression(JavaStatementScope scope) {
		JavaStatementList result = new JavaStatementList();
		result.addAll(this.statements);
		JavaStatementBuilder collapsed = this.lastStatement.collapseToSingleExpression(scope);
		if (collapsed instanceof JavaBlockBuilder) {
			JavaBlockBuilder collapsedBlock = (JavaBlockBuilder)collapsed;
			result.addAll(collapsedBlock.statements);
			return new JavaBlockBuilder(result, collapsedBlock.lastStatement);
		}
		return new JavaBlockBuilder(result, collapsed);
	}

	@Override
	public JavaLambdaBody toLambdaBody() {
		return completeAsReturn();
	}
	
	@Override
	public String toString() {
		StringConcatenation result = new StringConcatenation();
		result.append("{");
		result.newLine();
		result.append("\t");
		statements.forEach(stat -> {
			result.append(stat, "\t");
			result.newLine();
			result.append("\t");
		});
		result.append(lastStatement, "\t");
		result.newLine();
		result.append('}');
		return result.toString();
	}
}
