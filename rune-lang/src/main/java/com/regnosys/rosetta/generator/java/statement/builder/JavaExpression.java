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
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.DebuggingTargetLanguageStringConcatenation;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.JavaAssignment;
import com.regnosys.rosetta.generator.java.statement.JavaExpressionStatement;
import com.regnosys.rosetta.generator.java.statement.JavaLambdaBody;
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement;
import com.regnosys.rosetta.generator.java.statement.JavaReturnStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatementList;
import com.rosetta.util.types.JavaType;

/**
 * An arbitrary Java expression.
 * 
 * See `JavaStatementBuilder` for more documentation.
 */
public abstract class JavaExpression extends JavaStatementBuilder implements JavaLambdaBody {
	private final JavaType type;
	
	public JavaExpression(JavaType type) {
		this.type = type;
	}
	
	public static JavaExpression from(StringConcatenationClient value, JavaType type) {
		return new JavaExpression(type) {
			@Override
			public void appendTo(TargetStringConcatenation target) {
				target.append(value);
			}
		};
	}
	
	@Override
	public JavaType getExpressionType() {
		return type;
	}
	
	@Override
	public JavaStatementBuilder mapExpression(Function<JavaExpression, ? extends JavaStatementBuilder> mapper) {
		return mapper.apply(this);
	}
	
	@Override
	public JavaStatementBuilder then(JavaStatementBuilder after, BiFunction<JavaExpression, JavaExpression, JavaStatementBuilder> combineExpressions, JavaStatementScope scope) {
		if (after instanceof JavaExpression) {
			return this.then((JavaExpression)after, combineExpressions, scope);
		}
		return after.then(this, (otherExpr, thisExpr) -> combineExpressions.apply(thisExpr, otherExpr), scope);
	}
	public JavaStatementBuilder then(JavaExpression after, BiFunction<JavaExpression, JavaExpression, JavaStatementBuilder> combineExpressions, JavaStatementScope scope) {
		return combineExpressions.apply(this, after);
	}

	@Override
	public JavaStatement complete(Function<JavaExpression, JavaStatement> completer) {
		return completer.apply(this);
	}
	@Override
	public JavaReturnStatement completeAsReturn() {
		return new JavaReturnStatement(this);
	}
	@Override
	public JavaExpressionStatement completeAsExpressionStatement() {
		return new JavaExpressionStatement(this);
	}
	@Override
	public JavaAssignment completeAsAssignment(GeneratedIdentifier variableId) {
		return new JavaAssignment(variableId, this);
	}

	@Override
	public JavaStatementBuilder declareAsVariable(boolean isFinal, String variableId, JavaStatementScope scope) {
		GeneratedIdentifier id = scope.createIdentifier(this, variableId);
		return new JavaBlockBuilder(
				JavaStatementList.of(new JavaLocalVariableDeclarationStatement(isFinal, this.type, id, this)),
				new JavaVariable(id, this.type)
			);
	}
	
	@Override
	public JavaStatementBuilder collapseToSingleExpression(JavaStatementScope scope) {
		return this;
	}

	@Override
	public JavaLambdaBody toLambdaBody() {
		return this;
	}
	
	@Override
	public String toString() {
		return DebuggingTargetLanguageStringConcatenation.convertToDebugString(this);
	}
}
