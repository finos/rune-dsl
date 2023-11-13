package com.regnosys.rosetta.generator.java.statement;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

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
		target.append(thenBranch.toBlock());
	}
}
