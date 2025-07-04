package com.regnosys.rosetta.generator.java.expression;

import com.regnosys.rosetta.generator.java.scoping.JavaFileScope;
import com.regnosys.rosetta.generator.java.scoping.JavaPackageName;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.rosetta.util.DottedPath;

public class ExpressionScopeUtility {
	public JavaStatementScope createTestExpressionScope(DottedPath packageName) {
		JavaFileScope fileScope = new JavaFileScope("expression.java", packageName);
		return new JavaStatementScope("expression", fileScope);
	}
}
