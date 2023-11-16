package com.regnosys.rosetta.generator.java.statement.builder;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.JavaScope;
import com.regnosys.rosetta.generator.java.statement.JavaBlock;
import com.regnosys.rosetta.generator.java.statement.JavaIfThenElseStatement;
import com.regnosys.rosetta.generator.java.statement.JavaIfThenStatement;
import com.regnosys.rosetta.generator.java.statement.JavaLambdaBody;
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatementList;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.rosetta.util.types.JavaType;

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
	public JavaStatementBuilder then(JavaStatementBuilder after, BiFunction<JavaExpression, JavaExpression, JavaExpression> combineExpressions, JavaScope scope) {
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
	public JavaBlockBuilder declareAsVariable(boolean isFinal, String variableId, JavaScope scope) {
		GeneratedIdentifier id = scope.createIdentifier(this, variableId);
		if (elseBranch == JavaExpression.NULL) {
			return new JavaBlockBuilder(
					JavaStatementList.of(
						new JavaLocalVariableDeclarationStatement(false, commonType, id, JavaExpression.NULL),
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
	public JavaStatementBuilder collapseToSingleExpression(JavaScope scope) {
		return this.declareAsVariable(true, "ifThenElseResult", scope);
	}

	@Override
	public JavaLambdaBody toLambdaBody() {
		return new JavaBlockBuilder(this).toLambdaBody();
	}
}
