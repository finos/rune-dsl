package com.regnosys.rosetta.generator.java.statement;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.JavaScope;
import com.rosetta.util.types.JavaType;

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
	public JavaStatementBuilder then(JavaStatementBuilder after, BiFunction<JavaExpression, JavaExpression, JavaExpression> combineExpressions, JavaScope scope) {
		if (after instanceof JavaExpression) {
			return this.then((JavaExpression)after, combineExpressions, scope);
		}
		return after.mapExpression(expr -> combineExpressions.apply(this, expr));
	}
	public JavaExpression then(JavaExpression after, BiFunction<JavaExpression, JavaExpression, JavaExpression> combineExpressions, JavaScope scope) {
		return combineExpressions.apply(this, after);
	}

	@Override
	public JavaStatement complete(Function<JavaExpression, JavaStatement> completer) {
		return completer.apply(this);
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
