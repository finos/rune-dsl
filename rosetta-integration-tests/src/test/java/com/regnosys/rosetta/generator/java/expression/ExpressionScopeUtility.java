package com.regnosys.rosetta.generator.java.expression;

import com.regnosys.rosetta.generator.java.scoping.JavaFileScope;
import com.regnosys.rosetta.generator.java.scoping.JavaGlobalScope;
import com.regnosys.rosetta.generator.java.scoping.JavaPackageName;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.rosetta.util.DottedPath;

public class ExpressionScopeUtility {
	public JavaStatementScope createTestExpressionScope(DottedPath packageName) {
		JavaGlobalScope globalScope = new JavaGlobalScope();
		JavaFileScope fileScope = globalScope.getPackageScope(JavaPackageName.escape(packageName)).createFileScope("expression.java");
		return new JavaStatementScope("expression", fileScope);
	}
}
