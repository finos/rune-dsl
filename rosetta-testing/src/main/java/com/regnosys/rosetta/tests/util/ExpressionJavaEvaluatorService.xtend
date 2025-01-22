package com.regnosys.rosetta.tests.util

import com.regnosys.rosetta.tests.util.ExpressionParser
import javax.inject.Inject
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.rosetta.util.types.JavaType
import com.regnosys.rosetta.generator.java.JavaScope
import com.rosetta.util.DottedPath
import com.regnosys.rosetta.tests.compiler.InMemoryJavacCompiler
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.validation.IResourceValidator
import org.eclipse.xtext.testing.validation.ValidationTestHelper

class ExpressionJavaEvaluatorService {
	@Inject
	ExpressionParser expressionParser
	@Inject
	ExpressionValidationHelper validation
	@Inject
	ExpressionGenerator expressionGenerator
	@Inject
	IResourceValidator resourceValidator
	@Inject
	ValidationTestHelper validationHelper
	@Inject
	extension ImportManagerExtension
	
	def Object evaluate(CharSequence rosettaExpression, JavaType expectedType, InMemoryJavacCompiler compiler) {
		val expr = expressionParser.parseExpression(rosettaExpression)
		validationHelper.assertNoIssues(expr)
		
		val packageName = DottedPath.splitOnDots("com.regnosys.rosetta.tests.testexpression")
		val className = "TestExpressionEvaluator"
		val methodName = "evaluate"
		
		val packageScope = new JavaScope(packageName)
		val classScope = packageScope.classScope(className)
		val evaluateScope = classScope.methodScope(methodName)
		val javaCode = expressionGenerator.javaCode(expr, expectedType, evaluateScope)
		val StringConcatenationClient content = '''
			public class «className» {
				public «expectedType» «methodName»() «javaCode.completeAsReturn»
			}
		'''
		
		val sourceCode = buildClass(packageName, content, packageScope)
		val evaluatorClass = compiler.compile(packageName.child(className).withDots, sourceCode)
		val instance = evaluatorClass.getDeclaredConstructor.newInstance
		
		evaluatorClass.getDeclaredMethod(methodName).invoke(instance)
	}
}
