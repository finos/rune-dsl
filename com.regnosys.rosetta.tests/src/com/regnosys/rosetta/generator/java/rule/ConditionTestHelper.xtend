package com.regnosys.rosetta.generator.java.rule

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.rosetta.model.lib.path.RosettaPath
import java.util.Map
import java.util.Optional
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.google.inject.Injector
import com.google.inject.Guice
import com.google.inject.AbstractModule
import com.rosetta.model.lib.functions.ConditionValidator
import com.rosetta.model.lib.functions.DefaultConditionValidator
import com.rosetta.model.lib.functions.ModelObjectValidator
import com.rosetta.model.lib.functions.NoOpModelObjectValidator

class ConditionTestHelper {
	
	@Inject extension ModelHelper
	@Inject extension RosettaExtensions
	@Inject extension CodeGeneratorTestHelper
	
	final Injector injector
	
	new() {
		injector = Guice.createInjector(new AbstractModule() {
			override protected configure() {
				bind(ConditionValidator).toInstance(new DefaultConditionValidator)
				bind(ModelObjectValidator).toInstance(new NoOpModelObjectValidator)
			}
		})
	}
	
	def createConditionInstance(Map<String, Class<?>> classes, RootPackage rootNamespace, String conditionName) {
		val conditionClass = classes.get(rootNamespace.dataRule.name + '.' + conditionName.toConditionJavaType)
		val cond = conditionClass
				.declaredConstructor.newInstance
		injector.injectMembers(cond)
		return cond
	}
	
	def runDataRule(Map<String, Class<?>> classes, Object instance, String conditionName) {
		runDataRule(classes, rootPackage, instance, conditionName)
	}
	
	def runDataRule(Map<String, Class<?>> classes, RootPackage rootNamespace, Object instance, String conditionName) {
		val conditionClass = classes.get(rootNamespace.dataRule.name + '.' + conditionName.toConditionJavaType)
		val conditionInstance = createConditionInstance(classes, rootNamespace, conditionName)
		return conditionClass.getMatchingMethod("validate", #[RosettaPath, instance.class]).invoke(conditionInstance, null, instance)
	}
	
	def runChoiceRule(Map<String, Class<?>> classes, Object instance, String conditionName) {
		runChoiceRule(classes, rootPackage, instance, conditionName)
	}
	
	def runChoiceRule(Map<String, Class<?>> classes, RootPackage rootNamespace, Object instance, String conditionName) {
		val dataRuleClass = classes.get(rootNamespace.choiceRule.name + '.' + conditionName.toConditionJavaType)
		return dataRuleClass.getMatchingMethod("validate", #[RosettaPath, instance.class]).invoke(dataRuleClass.constructor.newInstance, null, instance)
	}

	def isSuccess(Object validationResult) {
		validationResult.class.getMatchingMethod("isSuccess", null).invoke(validationResult) as Boolean
	}

	def definition(Object validationResult) {
		validationResult.class.getMatchingMethod("getDefinition", null).invoke(validationResult) as String
	}

	def failureReason(Object validationResult) {
		validationResult.class.getMatchingMethod("getFailureReason", null).invoke(validationResult) as Optional<String>
	}
	
	
}
