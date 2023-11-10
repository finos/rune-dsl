package com.regnosys.rosetta.generator.java.statement;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.JavaScope;
import com.rosetta.util.types.JavaType;

public class JavaIfThenElseBuilder extends JavaStatementBuilder {
	private final JavaExpression condition;
	private final JavaStatementBuilder thenBranch;
	private final JavaStatementBuilder elseBranch;
	private final JavaType commonType;

	public JavaIfThenElseBuilder(JavaExpression condition, JavaStatementBuilder thenBranch, JavaStatementBuilder elseBranch, JavaType commonType) {
		this.condition = condition;
		this.thenBranch = thenBranch;
		this.elseBranch = elseBranch;
		this.commonType = commonType;
	}
	
	@Override
	public JavaType getExpressionType() {
		return commonType;
	}
	
	@Override
	public JavaIfThenElseBuilder mapExpression(Function<JavaExpression, ? extends JavaStatementBuilder> mapper) {
		JavaStatementBuilder mappedThenBranch = thenBranch.mapExpression(mapper);
		JavaStatementBuilder mappedElseBranch = elseBranch.mapExpression(mapper);
		return new JavaIfThenElseBuilder(condition, mappedThenBranch, mappedElseBranch, mappedThenBranch.getExpressionType());
	}

	@Override
	public JavaStatementBuilder then(JavaStatementBuilder after, BiFunction<JavaExpression, JavaExpression, JavaExpression> combineExpressions, JavaScope scope) {
		return this.collapseToSingleExpression(scope)
				.then(after, combineExpressions, scope);
	}

	@Override
	public JavaIfThenElseStatement complete(Function<JavaExpression, JavaStatement> completer) {
		return new JavaIfThenElseStatement(condition, thenBranch.complete(completer), elseBranch.complete(completer));
	}

	@Override
	public JavaBlockBuilder declareAsVariable(boolean isFinal, String variableId, JavaScope scope) {
		GeneratedIdentifier id = scope.createIdentifier(this, variableId);
		return new JavaBlockBuilder(
				JavaStatementList.of(
					new JavaLocalVariableDeclarationStatement(isFinal, commonType, id),
					this.completeAsAssignment(id)
				),
				new JavaVariable(id, commonType)
			);
	}
	
	@Override
	public JavaStatementBuilder collapseToSingleExpression(JavaScope scope) {
		return this.declareAsVariable(true, "ifThenElseResult", scope);
	}

	@Override
	public JavaLambdaBody toLambdaBody() {
		return new JavaBlockBuilder(this).toLambdaBody();
	}
}
