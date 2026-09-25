package com.regnosys.rosetta.generator.java.statement;

import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-14.html#jls-BasicForStatement
 * 
 * Example:
 * ```
 * for (int i=0; i<42; i++) {
 *     list.add(i);
 * }
 * ```
 * 
 * See `JavaStatementBuilder` for more documentation.
 */
public class JavaForLoop extends JavaStatement {
	private final JavaLocalVariableDeclarationStatement forInit;
	private final JavaExpression forCondition;
	private final JavaExpression forUpdate;
	private final JavaStatement forBody;

	public JavaForLoop(JavaLocalVariableDeclarationStatement forInit, JavaExpression forCondition, JavaExpression forUpdate, JavaStatement forBody) {
		this.forInit = forInit;
		this.forCondition = forCondition;
		this.forUpdate = forUpdate;
		this.forBody = forBody;
	}

	@Override
	public void render(CodeWriter out) {
		out.write("for (", forInit, " ", forCondition, "; ", forUpdate, ") ", forBody.toBlock());
	}
}
