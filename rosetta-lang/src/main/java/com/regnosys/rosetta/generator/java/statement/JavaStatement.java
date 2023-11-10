package com.regnosys.rosetta.generator.java.statement;

import com.regnosys.rosetta.generator.TargetLanguageRepresentation;

/**
 * Based on the Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-14.html#jls-BlockStatement
 */
public abstract class JavaStatement implements TargetLanguageRepresentation {	
	public JavaBlock toBlock() {
		return new JavaBlock(JavaStatementList.of(this));
	}
	public JavaStatementList asStatementList() {
		return JavaStatementList.of(this);
	}
	public JavaBlock prepend(JavaStatement other) {
		if (other instanceof JavaBlock) {
			return other.append(this);
		}
		return new JavaBlock(JavaStatementList.of(other, this));
	}
	public JavaBlock append(JavaStatement other) {
		if (other instanceof JavaBlock) {
			return other.prepend(this);
		}
		return new JavaBlock(JavaStatementList.of(this, other));
	}
}
