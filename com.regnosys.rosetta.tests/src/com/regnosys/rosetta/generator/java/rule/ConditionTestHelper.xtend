package com.regnosys.rosetta.generator.java.rule

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.rosetta.model.lib.path.RosettaPath
import java.util.Map
import java.util.Optional
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage

class ConditionTestHelper {
	
	@Inject extension ModelHelper
	@Inject extension RosettaExtensions
	@Inject extension CodeGeneratorTestHelper
	
	def runDataRule(Map<String, Class<?>> classes, Object instance, String conditionName) {
		runDataRule(classes, rootPackage, instance, conditionName)
	}
	
	def runDataRule(Map<String, Class<?>> classes, RootPackage rootNamespace, Object instance, String conditionName) {
		val dataRuleClass = classes.get(rootNamespace.dataRule.name + '.' + conditionName.toConditionJavaType)
		return dataRuleClass.getMatchingMethod("validate", #[RosettaPath, instance.class]).invoke(dataRuleClass.constructor.newInstance, null, instance)
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
