package com.regnosys.rosetta.generator.java.statement.builder;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.JavaScope;
import com.regnosys.rosetta.generator.java.statement.JavaAssignment;
import com.regnosys.rosetta.generator.java.statement.JavaExpressionStatement;
import com.regnosys.rosetta.generator.java.statement.JavaLambdaBody;
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement;
import com.regnosys.rosetta.generator.java.statement.JavaReturnStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatementList;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;

/**
 * An arbitrary Java expression.
 * 
 * See `JavaStatementBuilder` for more documentation.
 */
public abstract class JavaExpression extends JavaStatementBuilder implements JavaLambdaBody {
	/**
	 * An expression representing the value `null`.
	 */
	public static final JavaExpression NULL = new JavaExpression(JavaReferenceType.NULL_TYPE) {	
		@Override
		public void appendTo(TargetStringConcatenation target) {
			target.append("null");
		}
	};
	
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
	public JavaStatementBuilder then(JavaStatementBuilder after, BiFunction<JavaExpression, JavaExpression, JavaExpression> combineExpressions, JavaScope scope) {
		if (after instanceof JavaExpression) {
			return this.then((JavaExpression)after, combineExpressions, scope);
		}
		return after.then(this, (otherExpr, thisExpr) -> combineExpressions.apply(thisExpr, otherExpr), scope);
	}
	public JavaExpression then(JavaExpression after, BiFunction<JavaExpression, JavaExpression, JavaExpression> combineExpressions, JavaScope scope) {
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
	public JavaStatementBuilder declareAsVariable(boolean isFinal, String variableId, JavaScope scope) {
		GeneratedIdentifier id = scope.createIdentifier(this, variableId);
		return new JavaBlockBuilder(
				JavaStatementList.of(new JavaLocalVariableDeclarationStatement(isFinal, this.type, id, this)),
				new JavaVariable(id, this.type)
			);
	}
	
	@Override
	public JavaStatementBuilder collapseToSingleExpression(JavaScope scope) {
		return this;
	}

	@Override
	public JavaLambdaBody toLambdaBody() {
		return this;
	}
}
