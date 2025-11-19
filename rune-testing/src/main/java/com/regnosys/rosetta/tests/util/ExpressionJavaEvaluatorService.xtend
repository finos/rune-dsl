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
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.generator.java.object.MetaFieldGenerator
import com.regnosys.rosetta.tests.compiler.CompilationException
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.rosetta.model.lib.context.RuneContext
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope
import com.regnosys.rosetta.generator.GeneratedIdentifier
import com.rosetta.util.types.JavaClass
import com.regnosys.rosetta.generator.java.statement.JavaStatement
import com.rosetta.model.lib.context.RuneContextFactory

class ExpressionJavaEvaluatorService {
	@Inject
	ExpressionParser expressionParser
	@Inject
	MetaFieldGenerator metaFieldGenerator
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
	@Inject
	extension JavaTypeUtil
	@Inject
	RuneContextFactory contextFactory
	
	def Object evaluate(CharSequence rosettaExpression, RosettaModel context, JavaType expectedType, RuneContext runtimeContext, ClassLoader classLoader) {
		val expr = expressionParser.parseExpression(rosettaExpression, #[context])
		evaluate(expr, expectedType, runtimeContext, classLoader)
	}
	def Object evaluate(RosettaExpression expr, JavaType expectedType, RuneContext runtimeContext, ClassLoader classLoader) {
		validationHelper.assertNoIssues(expr)
		
		val packageName = DottedPath.splitOnDots("com.regnosys.rosetta.tests.testexpression")
		val className = "TestExpressionEvaluator"
		val methodName = "evaluate"
		
		val classScope = JavaClassScope.createAndRegisterIdentifier(RGeneratedJavaClass.create(JavaPackageName.escape(packageName), className, Object))
		val evaluateScope = classScope.createMethodScope(methodName)
		val contextId = evaluateScope.createUniqueIdentifier("context")
		val evaluateBodyScope = evaluateScope.bodyScope
		val scopeId = evaluateBodyScope.createUniqueIdentifier("scope")
		
		val dependencies = dependencyProvider.javaDependencies(expr)
		dependencies.forEach[evaluateBodyScope.createIdentifier(toDependencyInstance, simpleName.toFirstLower)]
		
		val javaCode = expressionGenerator.javaCode(expr, expectedType, contextId, evaluateBodyScope)
		val StringConcatenationClient content = '''
			public class «className» {
				public «expectedType» «methodName»(«RUNE_CONTEXT» «contextId») «
					if (dependencies.isEmpty) {
						javaCode.completeAsReturn.toBlock
					} else {
						var JavaStatement body = new JavaLocalVariableDeclarationStatement(true, RUNE_SCOPE, scopeId, JavaExpression.from('''«contextId».getScope()''', RUNE_SCOPE))
						for (dep : dependencies) {
							body = body.append(declareDependency(evaluateBodyScope, scopeId, dep))
						}
						body.append(javaCode.completeAsReturn)
					}
				»
			}
		'''
		
		val sourceCode = buildClass(packageName, content, classScope.getFileScope())
		val expressionCompiler = InMemoryJavacCompiler
				.newInstance()
				.useParentClassLoader(classLoader)
				.useOptions("--release", "8", "-Xlint:all", "-Xdiags:verbose");
		generateMetaFieldClasses(expr, expressionCompiler)
		var Class<?> evaluatorClass
		try {
			evaluatorClass = expressionCompiler.compile(packageName.child(className).withDots, sourceCode)
		} catch (CompilationException e) {
			throw new RuntimeException("Failed to compile expression.\n" + sourceCode, e)
		}
		val instance = injector.getInstance(evaluatorClass)
		
		evaluatorClass.getDeclaredMethod(methodName, RuneContext).invoke(instance, runtimeContext ?: contextFactory.createDefault)
	}
	
	private def void generateMetaFieldClasses(RosettaExpression expr, InMemoryJavacCompiler compiler) {
		metaFieldGenerator.streamObjects(expr)
			.forEach[object|
				val typeRepresentation = metaFieldGenerator.createTypeRepresentation(object);
				val classScope = JavaClassScope.createAndRegisterIdentifier(typeRepresentation);
				val classCode = metaFieldGenerator.generateClass(object, typeRepresentation, "0", classScope);
				val javaFileCode = buildClass(typeRepresentation.getPackageName(), classCode, classScope.getFileScope());
				compiler.addSource(typeRepresentation.canonicalName.withDots, javaFileCode)
			];
	}
	
	private def JavaLocalVariableDeclarationStatement declareDependency(JavaStatementScope scope, GeneratedIdentifier runeScopeVar, JavaClass<?> dependencyType) {
		new JavaLocalVariableDeclarationStatement(true, dependencyType, scope.getIdentifierOrThrow(dependencyType.toDependencyInstance), JavaExpression.from('''«runeScopeVar».getInstance(«dependencyType».class)''', dependencyType))
	}
}
