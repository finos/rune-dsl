package com.regnosys.rosetta.generator.java.statement;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-14.html#jls-IfThenStatement
 * 
 * Example:
 * ```
 * if (true) {
 *     return 42;
 * }
 * ```
 * 
 * See `JavaStatement` for more documentation.
 */
public class JavaIfThenStatement extends JavaStatement {
	private final JavaExpression condition;
	private final JavaStatement thenBranch;
	
	public JavaIfThenStatement(JavaExpression condition, JavaStatement thenBranch) {
		this.condition = condition;
		this.thenBranch = thenBranch;
	}

	@Override
	public void appendTo(TargetStringConcatenation target) {
		target.append("if (");
		target.append(condition);
		target.append(") ");
		// Calling `toBlock()` will make sure that the `then` branch is always enclosed in curly braces.
		// This is a style preference, and is technically not necessary.
		target.append(thenBranch.toBlock());
	}
}
