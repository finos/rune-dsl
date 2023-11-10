package com.regnosys.rosetta.generator.java.statement;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.JavaScope;
import com.rosetta.util.types.JavaType;

public abstract class JavaStatementBuilder {
	public abstract JavaType getExpressionType();
	
	public abstract JavaStatementBuilder mapExpression(Function<JavaExpression, ? extends JavaStatementBuilder> mapper);
	
	public abstract JavaStatementBuilder then(JavaStatementBuilder after, BiFunction<JavaExpression, JavaExpression, JavaExpression> combineExpressions, JavaScope scope);
	
	public abstract JavaStatement complete(Function<JavaExpression, JavaStatement> completer);
	
	public JavaStatement completeAsReturn() {
		return complete(JavaReturnStatement::new);
	}
	
	public JavaStatement completeAsExpressionStatement() {
		return complete(JavaExpressionStatement::new);
	}
	
	public JavaStatement completeAsAssignment(GeneratedIdentifier variableId) {
		return complete(expr -> new JavaAssignment(variableId, expr));
	}
		
	public abstract JavaStatementBuilder declareAsVariable(boolean isFinal, String variableId, JavaScope scope);
	
	public abstract JavaStatementBuilder collapseToSingleExpression(JavaScope scope);
	
	public abstract JavaLambdaBody toLambdaBody();
}
