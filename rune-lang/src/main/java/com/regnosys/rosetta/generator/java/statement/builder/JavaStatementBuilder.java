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

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.JavaLambdaBody;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.rosetta.util.types.JavaType;

/**
 * A convenient API for building a Java statement.
 * 
 * In general, a statement builder consists of a list of statements, ending with one or more expressions.
 * The builder can be "completed" into a Java statement by converting the ending expression(s) into
 * a valid Java statement, e.g., by returning the expressions or assigning them to a variable.
 * 
 * The simples statement builder is a single `JavaExpression`.
 * 
 * Usage:
 * 
 * *Example 1*
 * Say we want to build the following block:
 * ```
 * {
 *     final int x = 42;
 *     return x;
 * }
 * ```
 * This can be done using the following Xtend code:
 * ```
 * JavaExpression.from('''42''', JavaPrimitiveType.INT)
 *     .declareAsVariable(true, "x", scope)
 *     .completeAsReturn
 * ```
 * 
 * *Example 2*
 * Say we want to build the following block (we don't really care about the name of the variable):
 * ```
 * {
 *     int ifThenElseResult;
 *     if (foo == bar) {
 *         ifThenElseResult = compute(42);
 *     } else {
 *         ifThenElseResult = compute(0);
 *     }
 *     return ifThenElseResult + 1;
 * }
 * ```
 * This can be done using the following Xtend code:
 * ```
 * JavaExpression.from('''foo == bar''', JavaPrimitiveType.BOOLEAN)
 *     .mapExpression[
 *         new JavaIfThenElseBuilder(
 *             it,
 *             JavaExpression.from('''42''', JavaPrimitiveType.INT),
 *             JavaExpression.from('''0''', JavaPrimitiveType.INT)
 *         )
 *     ]
 *     .mapExpression[
 *         JavaExpression.from('''compute(«it»)''', JavaPrimitiveType.INT)
 *     ]
 *     .collapseToSingleExpression
 *     .mapExpression[
 *         JavaExpression.from('''«it» + 1''', JavaPrimitiveType.INT)
 *     ]
 *     .completeAsReturn
 * ```
 */
public abstract class JavaStatementBuilder {
	
	public static JavaStatementBuilder invokeMethod(List<JavaStatementBuilder> arguments, Function<JavaExpression, ? extends JavaStatementBuilder> methodInvoker, JavaStatementScope scope) {
		if (arguments.isEmpty()) {
			return methodInvoker.apply(null);
		}
		JavaStatementBuilder argCode = arguments.get(0);
		for (var i = 1; i < arguments.size(); i++) {
			argCode = argCode.then(
				arguments.get(i),
				(argList, newArg) -> new JavaExpression(null) {
					@Override
					public void appendTo(TargetStringConcatenation target) {
						target.append(argList);
						target.append(", ");
						target.append(newArg);
					}
				},
				scope
			);
		}
		return argCode.collapseToSingleExpression(scope).mapExpression(methodInvoker);
	}
	
	
	/**
	 * Get the type of the last expression of this builder,
	 * or the least common supertype of all expressions in different branches of this builder.
	 */
	public abstract JavaType getExpressionType();
	
	/**
	 * Complete this statement builder by mapping all expressions to a statement.
	 */
	public abstract JavaStatement complete(Function<JavaExpression, JavaStatement> completer);
	/**
	 * Complete this statement builder by returning all expressions.
	 */
	public abstract JavaStatement completeAsReturn();
	/**
	 * Complete this statement builder by ending all expressions with a semicolon.
	 */
	public abstract JavaStatement completeAsExpressionStatement();
	/**
	 * Complete this statement builder by assigning all expressions to a given variable.
	 */
	public abstract JavaStatement completeAsAssignment(GeneratedIdentifier variableId);
	
	/**
	 * Map all expressions contained in this builder to a new builder, and append the result.
	 * 
	 * This will flatten Java block statements.
	 */
	public abstract JavaStatementBuilder mapExpression(Function<JavaExpression, ? extends JavaStatementBuilder> mapper);
	/**
	 * Map all non-null expressions contained in this builder to a new builder, and append the result.
	 * 
	 * See {@link #mapExpression(Function)}.
	 */
	public JavaStatementBuilder mapExpressionIfNotNull(Function<JavaExpression, ? extends JavaStatementBuilder> mapper) {
		return mapExpression(expr -> expr == JavaLiteral.NULL ? JavaLiteral.NULL : mapper.apply(expr));
	}
	
	/**
	 * Assign all expressions to a new variable, and return a new builder ending with that variable.
	 */
	public abstract JavaStatementBuilder declareAsVariable(boolean isFinal, String variableId, JavaStatementScope scope);
	
	/**
	 * If this statement builder ends with multiple branches, assign all expressions to a new variable
	 * and return a new builder ending with that variable.
	 * 
	 * If this statement builder does not end in multiple branches, return itself unmodified.
	 */
	public abstract JavaStatementBuilder collapseToSingleExpression(JavaStatementScope scope);
	
	/**
	 * Append another statement builder to this one, and combine this expression with the other using the given operation.
	 */
	public abstract JavaStatementBuilder then(JavaStatementBuilder after, BiFunction<JavaExpression, JavaExpression, JavaStatementBuilder> combineExpressions, JavaStatementScope scope);
	
	/**
	 * Convert this statement builder into a valid lambda body.
	 */
	public abstract JavaLambdaBody toLambdaBody();
}
