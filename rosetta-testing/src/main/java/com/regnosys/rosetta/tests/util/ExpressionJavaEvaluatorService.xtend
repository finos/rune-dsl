package com.regnosys.rosetta.tests.util

import com.regnosys.rosetta.tests.util.ExpressionParser
import jakarta.inject.Inject
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.rosetta.util.types.JavaType
import com.rosetta.util.DottedPath
import com.regnosys.rosetta.tests.compiler.InMemoryJavacCompiler
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.generator.java.expression.JavaDependencyProvider
import com.google.inject.Injector
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass
import com.regnosys.rosetta.generator.java.scoping.JavaPackageName
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope

class ExpressionJavaEvaluatorService {
	@Inject
	ExpressionParser expressionParser
	@Inject
	ExpressionGenerator expressionGenerator
	@Inject
	JavaDependencyProvider dependencyProvider
	@Inject
	extension JavaIdentifierRepresentationService
	@Inject
	ValidationTestHelper validationHelper
	@Inject
	Injector injector
	@Inject
	extension ImportManagerExtension
	
	def Object evaluate(CharSequence rosettaExpression, RosettaModel context, JavaType expectedType, ClassLoader classLoader) {
		val expr = expressionParser.parseExpression(rosettaExpression, #[context])
		validationHelper.assertNoIssues(expr)
		
		val packageName = DottedPath.splitOnDots("com.regnosys.rosetta.tests.testexpression")
		val className = "TestExpressionEvaluator"
		val methodName = "evaluate"
		
		val classScope = JavaClassScope.createAndRegisterIdentifier(RGeneratedJavaClass.create(JavaPackageName.escape(packageName), className, Object))
		val evaluateScope = classScope.createMethodScope(methodName)
		val evaluateBodyScope = evaluateScope.bodyScope
		
		val dependencies = dependencyProvider.javaDependencies(expr)
		dependencies.forEach[classScope.createIdentifier(toDependencyInstance, simpleName.toFirstLower)]
		
		val javaCode = expressionGenerator.javaCode(expr, expectedType, evaluateBodyScope)
		val StringConcatenationClient content = '''
			public class «className» {
				«FOR dep : dependencies»
				@«Inject»
				private «dep» «classScope.getIdentifierOrThrow(dep.toDependencyInstance)»;
				«ENDFOR»
				
				public «expectedType» «methodName»() «javaCode.completeAsReturn.toBlock»
			}
		'''
		
		val sourceCode = buildClass(packageName, content, classScope.getFileScope())
		val expressionCompiler = InMemoryJavacCompiler
				.newInstance()
				.useParentClassLoader(classLoader)
				.useOptions("--release", "8", "-Xlint:all", "-Xdiags:verbose");
		val evaluatorClass = expressionCompiler.compile(packageName.child(className).withDots, sourceCode)
		val instance = injector.getInstance(evaluatorClass)
		
		evaluatorClass.getDeclaredMethod(methodName).invoke(instance)
	}
}
