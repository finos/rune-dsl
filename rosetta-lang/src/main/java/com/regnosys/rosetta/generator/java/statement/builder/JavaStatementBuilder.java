package com.regnosys.rosetta.generator.java.statement.builder;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.JavaScope;
import com.regnosys.rosetta.generator.java.statement.JavaLambdaBody;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.rosetta.util.types.JavaType;

public abstract class JavaStatementBuilder {
	public abstract JavaType getExpressionType();
	
	public abstract JavaStatementBuilder mapExpression(Function<JavaExpression, ? extends JavaStatementBuilder> mapper);
	public JavaStatementBuilder mapExpressionIfNotNull(Function<JavaExpression, ? extends JavaStatementBuilder> mapper) {
		return mapExpression(expr -> expr == JavaExpression.NULL ? JavaExpression.NULL : mapper.apply(expr));
	}
	
	public abstract JavaStatementBuilder then(JavaStatementBuilder after, BiFunction<JavaExpression, JavaExpression, JavaExpression> combineExpressions, JavaScope scope);
	
	public abstract JavaStatement complete(Function<JavaExpression, JavaStatement> completer);
	public abstract JavaStatement completeAsReturn();
	public abstract JavaStatement completeAsExpressionStatement();
	public abstract JavaStatement completeAsAssignment(GeneratedIdentifier variableId);
	
	public abstract JavaStatementBuilder declareAsVariable(boolean isFinal, String variableId, JavaScope scope);
	
	public abstract JavaStatementBuilder collapseToSingleExpression(JavaScope scope);
	
	public abstract JavaLambdaBody toLambdaBody();
}
