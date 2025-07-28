package com.regnosys.rosetta.generator.java.statement;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.rosetta.util.types.JavaType;

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-14.html#jls-EnhancedForStatement
 * 
 * Example:
 * ```
 * for (Foo foo : fooList) {
 *     processFoo(foo);
 * }
 * ```
 * 
 * See `JavaStatementBuilder` for more documentation.
 */
public class JavaEnhancedForLoop extends JavaStatement {
	private final boolean isFinal;
	private final JavaType variableType;
	private final GeneratedIdentifier variableId;
	private final JavaExpression iterableExpression;
	private final JavaStatement forBody;

	public JavaEnhancedForLoop(boolean isFinal, JavaType variableType, GeneratedIdentifier variableId, JavaExpression iterableExpression, JavaStatement forBody) {
		this.isFinal = isFinal;
		this.variableType = variableType;
		this.variableId = variableId;
		this.iterableExpression = iterableExpression;
		this.forBody = forBody;
	}

	@Override
	public void appendTo(TargetStringConcatenation target) {
		target.append("for (");
		if (isFinal) {
			target.append("final ");
		}
		target.append(variableType);
		target.append(" ");
		target.append(variableId);
		target.append(" : ");
		target.append(iterableExpression);
		target.append(") ");
		// Calling `toBlock()` will make sure that the body is always enclosed in curly braces.
		// This is a style preference, and is technically not necessary.
		target.append(forBody.toBlock());
	}
}
