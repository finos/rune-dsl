package com.regnosys.rosetta.generator.java.condition

import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.RosettaGrammarUtil
import com.regnosys.rosetta.rosetta.simple.Condition
import com.rosetta.model.lib.annotations.RosettaDataRule
import com.rosetta.model.lib.expression.ComparisonResult
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ValidationResult
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.CONDITION__EXPRESSION
import jakarta.inject.Inject
import com.google.inject.ImplementedBy
import com.rosetta.model.lib.validation.ValidationResult.ValidationType
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.expression.JavaDependencyProvider
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil
import java.util.Arrays
import java.util.Collections
import java.util.List
import com.regnosys.rosetta.generator.java.types.JavaConditionInterface
import com.regnosys.rosetta.rosetta.ParametrizedRosettaType

class ConditionGenerator {
	@Inject ExpressionGenerator expressionHandler
	@Inject extension ImportManagerExtension
	@Inject JavaDependencyProvider dependencies
	@Inject extension JavaIdentifierRepresentationService
	@Inject extension JavaTypeTranslator
	@Inject extension JavaTypeUtil
	@Inject extension ModelGeneratorUtil
	
	def generate(IFileSystemAccess2 fsa, Condition ele, String version) {
		val clazz = ele.toConditionJavaClass
		val topScope = new JavaScope(clazz.packageName)
		
		val classBody = ele.conditionClassBody(clazz, topScope, version)
		val content = buildClass(clazz.packageName, classBody, topScope)
		fsa.generateFile('''«clazz.canonicalName.withForwardSlashes».java''', content)
	}

	private def StringConcatenationClient conditionClassBody(Condition condition, JavaConditionInterface conditionClass, JavaScope scope, String version) {
		val definition = RosettaGrammarUtil.quote(RosettaGrammarUtil.extractNodeText(condition, CONDITION__EXPRESSION))
		val deps = dependencies.javaDependencies(condition.expression)
		val implicitVarRepr = condition.expression.implicitVarInContext
		val params = conditionClass.parameters.entrySet.toMap(
			[entry|(condition.enclosingType as ParametrizedRosettaType).parameters.findFirst[name == entry.key]],
			[entry|entry.value]
		)
		
		val classScope = scope.classScope(conditionClass.simpleName)
		
		val validateScope = classScope.methodScope("validate")
		val pathId = validateScope.createUniqueIdentifier("path")
		val instanceId = validateScope.createIdentifier(implicitVarRepr, conditionClass.instanceType.name.toFirstLower)
		params.forEach[param,type|validateScope.createIdentifier(param)]
		
		val defaultClassScope = classScope.classScope("Default")
		val defaultClassName = defaultClassScope.createUniqueIdentifier("Default")
		
		deps.forEach[defaultClassScope.createIdentifier(it.toDependencyInstance, it.simpleName.toFirstLower)]
		
		val defaultClassValidateScope = defaultClassScope.methodScope("validate")
		val defaultClassPathId = defaultClassValidateScope.createUniqueIdentifier("path")
		val defaultClassInstanceId = defaultClassValidateScope.createIdentifier(implicitVarRepr, conditionClass.instanceType.name.toFirstLower)
		params.forEach[param,type|defaultClassValidateScope.createIdentifier(param)]
		val defaultClassResultId = defaultClassValidateScope.createUniqueIdentifier("result")
		val defaultClassFailureMessageId = defaultClassValidateScope.createUniqueIdentifier("failureMessage")
		
		val defaultClassExecuteScope = defaultClassScope.methodScope("execute")
		val defaultClassExecuteInstanceId = defaultClassExecuteScope.createIdentifier(implicitVarRepr, conditionClass.instanceType.name.toFirstLower)
		params.forEach[param,type|defaultClassExecuteScope.createIdentifier(param)]
		val defaultClassExecuteExceptionId = defaultClassExecuteScope.createUniqueIdentifier("ex")
		
		val noOpClassScope = classScope.classScope("NoOp")
		val noOpClassName = noOpClassScope.createUniqueIdentifier("NoOp")
		
		val noOpClassValidateScope = noOpClassScope.methodScope("validate")
		val noOpClassPathId = noOpClassValidateScope.createUniqueIdentifier("path")
		val noOpInstanceId = noOpClassValidateScope.createIdentifier(implicitVarRepr, conditionClass.instanceType.name.toFirstLower)
		params.forEach[param,type|noOpClassValidateScope.createIdentifier(param)]
		
		'''
			«emptyJavadocWithVersion(version)»
			@«RosettaDataRule»("«conditionClass.simpleName»")
			@«ImplementedBy»(«conditionClass».Default.class)
			public interface «conditionClass»«IF conditionClass.implementsValidatorInterface» extends «conditionClass.validatorInterface»«ENDIF» {
				
				String NAME = "«conditionClass.simpleName»";
				String DEFINITION = «definition»;
				«IF !conditionClass.implementsValidatorInterface»
				«List»<«ValidationResult»<?>> getValidationResults(«RosettaPath» «pathId», «conditionClass.instanceClass» «instanceId»«FOR param : params.keySet», «params.get(param)» «validateScope.getIdentifierOrThrow(param)»«ENDFOR»);
				«ENDIF»
				
				class «defaultClassName» implements «conditionClass» {
				
					«FOR dep : deps»
						@«javax.inject.Inject» protected «dep» «defaultClassScope.getIdentifierOrThrow(dep.toDependencyInstance)»;
						
					«ENDFOR»
					@Override
					public «List»<«ValidationResult»<?>> getValidationResults(«RosettaPath» «defaultClassPathId», «conditionClass.instanceClass» «defaultClassInstanceId»«FOR param : params.keySet», «params.get(param)» «defaultClassValidateScope.getIdentifierOrThrow(param)»«ENDFOR») {
						«ComparisonResult» «defaultClassResultId» = executeDataRule(«defaultClassValidateScope.getIdentifierOrThrow(implicitVarRepr)»«FOR param: params.keySet», «defaultClassValidateScope.getIdentifierOrThrow(param)»«ENDFOR»);
						if (result.get()) {
							return «Arrays».asList(«ValidationResult».success(NAME, ValidationResult.ValidationType.DATA_RULE, "«conditionClass.instanceType.name»", «defaultClassPathId», DEFINITION));
						}
						
						String «defaultClassFailureMessageId» = «defaultClassResultId».getError();
						if («defaultClassFailureMessageId» == null || «defaultClassFailureMessageId».contains("Null") || «defaultClassFailureMessageId» == "") {
							«defaultClassFailureMessageId» = "Condition has failed.";
						}
						return «Arrays».asList(«ValidationResult».failure(NAME, «ValidationType».DATA_RULE, "«conditionClass.instanceType.name»", «defaultClassPathId», DEFINITION, «defaultClassFailureMessageId»));
					}
					
					private «ComparisonResult» executeDataRule(«conditionClass.instanceClass» «defaultClassExecuteInstanceId»«FOR param : params.keySet», «params.get(param)» «defaultClassExecuteScope.getIdentifierOrThrow(param)»«ENDFOR») {
						try «expressionHandler.javaCode(condition.expression, COMPARISON_RESULT, defaultClassExecuteScope)
								.completeAsReturn.toBlock»
						catch («Exception» «defaultClassExecuteExceptionId») {
							return «ComparisonResult».failure(«defaultClassExecuteExceptionId».getMessage());
						}
					}
				}
				
				@SuppressWarnings("unused")
				class «noOpClassName» implements «conditionClass» {
				
					@Override
					public «List»<«ValidationResult»<?>> getValidationResults(«RosettaPath» «noOpClassPathId», «conditionClass.instanceClass» «noOpInstanceId»«FOR param : params.keySet», «params.get(param)» «noOpClassValidateScope.getIdentifierOrThrow(param)»«ENDFOR») {
						return «Collections».emptyList();
					}
				}
			}
		'''
	}
}

