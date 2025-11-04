package com.regnosys.rosetta.generator.java.condition

import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.RosettaGrammarUtil
import com.regnosys.rosetta.rosetta.simple.Condition
import com.rosetta.model.lib.annotations.RosettaDataRule
import com.rosetta.model.lib.expression.ComparisonResult
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ValidationResult

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
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.EcoreBasedJavaClassGenerator
import com.regnosys.rosetta.rosetta.RosettaTypeWithConditions

class ConditionGenerator extends EcoreBasedJavaClassGenerator<Condition, JavaConditionInterface> {
	@Inject ExpressionGenerator expressionHandler
	@Inject JavaDependencyProvider dependencies
	@Inject extension JavaIdentifierRepresentationService
	@Inject extension JavaTypeTranslator
	@Inject extension JavaTypeUtil
	@Inject extension ModelGeneratorUtil
	
	
	override protected streamObjects(RosettaModel model) {
		model.elements.stream
			.filter[it instanceof RosettaTypeWithConditions]
			.map[it as RosettaTypeWithConditions]
			.flatMap[it.conditions.stream]
	}
	override protected createTypeRepresentation(Condition object) {
		object.toConditionJavaClass
	}
	override protected generateClass(Condition condition, JavaConditionInterface conditionClass, String version, JavaClassScope classScope) {
		val definition = RosettaGrammarUtil.quote(RosettaGrammarUtil.extractNodeText(condition, CONDITION__EXPRESSION))
		val deps = dependencies.javaDependencies(condition.expression)
		val implicitVarRepr = condition.expression.implicitVarInContext
		val params = conditionClass.parameters.entrySet.toMap(
			[entry|(condition.enclosingType as ParametrizedRosettaType).parameters.findFirst[name == entry.key]],
			[entry|entry.value]
		)
		
		val getValidationResultsScope = classScope.createMethodScope("getValidationResults")
		val pathId = getValidationResultsScope.createUniqueIdentifier("path")
		val instanceId = getValidationResultsScope.createIdentifier(implicitVarRepr, conditionClass.instanceType.name.toFirstLower)
		params.forEach[param,type|getValidationResultsScope.createIdentifier(param)]
		
		val defaultClass = conditionClass.createNestedClassImplementingInterface("Default", conditionClass)
		val defaultClassScope = classScope.createNestedClassScopeAndRegisterIdentifier(defaultClass)
		
		deps.forEach[defaultClassScope.createIdentifier(it.toDependencyInstance, it.simpleName.toFirstLower)]
		
		val defaultClassGetValidationResultsScope = defaultClassScope.createMethodScope("getValidationResults")
		val defaultClassInstanceId = defaultClassGetValidationResultsScope.createIdentifier(implicitVarRepr, conditionClass.instanceType.name.toFirstLower)
		params.forEach[param,type|defaultClassGetValidationResultsScope.createIdentifier(param)]
		val defaultClassGetValidationResultsBodyScope = defaultClassGetValidationResultsScope.bodyScope
		val defaultClassResultId = defaultClassGetValidationResultsBodyScope.createUniqueIdentifier("result")
		val defaultClassFailureMessageId = defaultClassGetValidationResultsBodyScope.createUniqueIdentifier("failureMessage")
		
		val defaultClassExecuteScope = defaultClassScope.createMethodScope("execute")
		val defaultClassExecuteInstanceId = defaultClassExecuteScope.createIdentifier(implicitVarRepr, conditionClass.instanceType.name.toFirstLower)
		params.forEach[param,type|defaultClassExecuteScope.createIdentifier(param)]
		val defaultClassExecuteBodyScope = defaultClassExecuteScope.bodyScope
		val defaultClassExecuteExceptionId = defaultClassExecuteBodyScope.createUniqueIdentifier("ex")
		
		val noOpClass = conditionClass.createNestedClassImplementingInterface("NoOp", conditionClass)
		classScope.createNestedClassScopeAndRegisterIdentifier(noOpClass)
		'''
			«emptyJavadocWithVersion(version)»
			@«RosettaDataRule»("«conditionClass.simpleName»")
			@«ImplementedBy»(«conditionClass».Default.class)
			public «conditionClass.asInterfaceDeclaration» {
				
				String NAME = "«conditionClass.simpleName»";
				String DEFINITION = «definition»;
				«IF !conditionClass.implementsValidatorInterface»
				«List»<«ValidationResult»<?>> getValidationResults(«RosettaPath» «pathId», «conditionClass.instanceClass» «instanceId»«FOR param : params.keySet», «params.get(param)» «getValidationResultsScope.getIdentifierOrThrow(param)»«ENDFOR»);
				«ENDIF»
				
				«defaultClass.asClassDeclaration» {
				
					«FOR dep : deps»
						@«javax.inject.Inject» protected «dep» «defaultClassScope.getIdentifierOrThrow(dep.toDependencyInstance)»;
						
					«ENDFOR»
					@Override
					public «List»<«ValidationResult»<?>> getValidationResults(«RosettaPath» «pathId», «conditionClass.instanceClass» «defaultClassInstanceId»«FOR param : params.keySet», «params.get(param)» «getValidationResultsScope.getIdentifierOrThrow(param)»«ENDFOR») {
						«ComparisonResult» «defaultClassResultId» = executeDataRule(«defaultClassGetValidationResultsBodyScope.getIdentifierOrThrow(implicitVarRepr)»«FOR param: params.keySet», «defaultClassGetValidationResultsBodyScope.getIdentifierOrThrow(param)»«ENDFOR»);
						if (result.get()) {
							return «Arrays».asList(«ValidationResult».success(NAME, ValidationResult.ValidationType.DATA_RULE, "«conditionClass.instanceType.name»", «pathId», DEFINITION));
						}
						
						String «defaultClassFailureMessageId» = «defaultClassResultId».getError();
						if («defaultClassFailureMessageId» == null || «defaultClassFailureMessageId».contains("Null") || «defaultClassFailureMessageId» == "") {
							«defaultClassFailureMessageId» = "Condition has failed.";
						}
						return «Arrays».asList(«ValidationResult».failure(NAME, «ValidationType».DATA_RULE, "«conditionClass.instanceType.name»", «pathId», DEFINITION, «defaultClassFailureMessageId»));
					}
					
					private «ComparisonResult» executeDataRule(«conditionClass.instanceClass» «defaultClassExecuteInstanceId»«FOR param : params.keySet», «params.get(param)» «defaultClassExecuteScope.getIdentifierOrThrow(param)»«ENDFOR») {
						try «expressionHandler.javaCode(condition.expression, COMPARISON_RESULT, defaultClassExecuteBodyScope)
								.completeAsReturn.toBlock»
						catch («Exception» «defaultClassExecuteExceptionId») {
							return «ComparisonResult».failure(«defaultClassExecuteExceptionId».getMessage());
						}
					}
				}
				
				@SuppressWarnings("unused")
				«noOpClass.asClassDeclaration» {
				
					@Override
					public «List»<«ValidationResult»<?>> getValidationResults(«RosettaPath» «pathId», «conditionClass.instanceClass» «instanceId»«FOR param : params.keySet», «params.get(param)» «getValidationResultsScope.getIdentifierOrThrow(param)»«ENDFOR») {
						return «Collections».emptyList();
					}
				}
			}
		'''
	}
}

