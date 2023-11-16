package com.regnosys.rosetta.generator.java.statement.builder;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.JavaScope;
import com.regnosys.rosetta.generator.java.statement.JavaAssignment;
import com.regnosys.rosetta.generator.java.statement.JavaIfThenElseStatement;
import com.regnosys.rosetta.generator.java.statement.JavaLambdaBody;
import com.regnosys.rosetta.generator.java.statement.JavaReturnStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.rosetta.util.types.JavaType;

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-15.html#jls-ConditionalExpression
 */
public class JavaConditionalExpression extends JavaStatementBuilder implements JavaLambdaBody {
	private final JavaExpression condition;
	private final JavaExpression thenBranch;
	private final JavaExpression elseBranch;
	private final JavaType commonType;
	
	private final JavaTypeUtil typeUtil;

	public JavaConditionalExpression(JavaExpression condition, JavaExpression thenBranch, JavaExpression elseBranch, JavaType commonType, JavaTypeUtil typeUtil) {
		this.condition = condition;
		this.thenBranch = thenBranch;
		this.elseBranch = elseBranch;
		this.commonType = commonType;
		
		this.typeUtil = typeUtil;
	}
	public JavaConditionalExpression(JavaExpression condition, JavaExpression thenBranch, JavaExpression elseBranch, JavaTypeUtil typeUtil) {
		this(condition, thenBranch, elseBranch, typeUtil.join(thenBranch.getExpressionType(), elseBranch.getExpressionType()), typeUtil);
	}

	@Override
	public JavaType getExpressionType() {
		return commonType;
	}

	@Override
	public JavaStatementBuilder mapExpression(Function<JavaExpression, ? extends JavaStatementBuilder> mapper) {
		JavaStatementBuilder newThenBranch = mapper.apply(thenBranch);
		JavaStatementBuilder newElseBranch = mapper.apply(elseBranch);
		if (newThenBranch instanceof JavaExpression && newElseBranch instanceof JavaExpression) {
			return new JavaConditionalExpression(condition, (JavaExpression)newThenBranch, (JavaExpression)newElseBranch, typeUtil);
		} else {
			return new JavaIfThenElseBuilder(condition, newThenBranch, newElseBranch, typeUtil);
		}
	}

	@Override
	public JavaStatementBuilder then(JavaStatementBuilder after, BiFunction<JavaExpression, JavaExpression, JavaExpression> combineExpressions, JavaScope scope) {
		return this.collapseToSingleExpression(scope)
				.then(after, combineExpressions, scope);
	}

	@Override
	public JavaStatement complete(Function<JavaExpression, JavaStatement> completer) {
		return completer.apply(this.toExpression());
	}
	@Override
	public JavaReturnStatement completeAsReturn() {
		return this.toExpression().completeAsReturn();
	}
	@Override
	public JavaIfThenElseStatement completeAsExpressionStatement() {
		return new JavaIfThenElseBuilder(condition, thenBranch, elseBranch, commonType, typeUtil).completeAsExpressionStatement();
	}
	@Override
	public JavaAssignment completeAsAssignment(GeneratedIdentifier variableId) {
		return this.toExpression().completeAsAssignment(variableId);
	}

	@Override
	public JavaStatementBuilder declareAsVariable(boolean isFinal, String variableId, JavaScope scope) {
		return this.toExpression().declareAsVariable(isFinal, variableId, scope);
	}

	@Override
	public JavaExpression collapseToSingleExpression(JavaScope scope) {
		return JavaExpression.from(new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append("(");
				target.append(JavaConditionalExpression.this);
				target.append(")");
			}
		}, commonType);
	}

	@Override
	public JavaLambdaBody toLambdaBody() {
		return this;
	}

	@Override
	public void appendTo(TargetStringConcatenation target) {
		target.append(condition);
		target.append(" ? ");
		target.append(thenBranch);
		target.append(" : ");
		target.append(elseBranch);
	}
	
	private JavaExpression toExpression() {
		return JavaExpression.from(new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append(JavaConditionalExpression.this);
			}
		}, commonType);
	}
}
