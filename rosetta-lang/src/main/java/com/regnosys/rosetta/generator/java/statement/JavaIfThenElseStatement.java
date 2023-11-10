package com.regnosys.rosetta.generator.java.statement;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

public class JavaIfThenElseStatement extends JavaStatement {
	private final JavaExpression condition;
	private final JavaStatement thenBranch;
	private final JavaStatement elseBranch;
	
	public JavaIfThenElseStatement(JavaExpression condition, JavaStatement thenBranch, JavaStatement elseBranch) {
		this.condition = condition;
		this.thenBranch = thenBranch;
		this.elseBranch = elseBranch;
	}

	@Override
	public void appendTo(TargetStringConcatenation target) {
		target.append("if (");
		target.append(condition);
		target.append(") ");
		target.append(thenBranch.toBlock());
		target.append(" else ");
		if (elseBranch instanceof JavaIfThenElseStatement) {
			target.append(elseBranch);
		} else {
			target.append(elseBranch.toBlock());
		}
	}
}
