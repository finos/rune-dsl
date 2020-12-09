package com.regnosys.rosetta.generator.java.rule

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.rosetta.model.lib.path.RosettaPath
import java.util.Map
import java.util.Optional

class ConditionHelper {
	
	@Inject extension com.regnosys.rosetta.tests.util.ModelHelper
	@Inject extension RosettaExtensions
	
	def runDataRule(Map<String, Class<?>> classes, Object instance, String conditionName) {
		val dataRuleClass = classes.get(rootPackage.dataRule.name + '.' + conditionName.toConditionJavaType)
		return dataRuleClass.getMethod("validate", #[RosettaPath, instance.class]).invoke(dataRuleClass.newInstance, null, instance)
	}
	
	def runChoiceRule(Map<String, Class<?>> classes, Object instance, String conditionName) {
		val dataRuleClass = classes.get(rootPackage.choiceRule.name + '.' + conditionName.toConditionJavaType)
		return dataRuleClass.getMethod("validate", #[RosettaPath, instance.class]).invoke(dataRuleClass.newInstance, null, instance)
	}
	

	def isSuccess(Object validationResult) {
		validationResult.class.getMethod("isSuccess", null).invoke(validationResult) as Boolean
	}

	def definition(Object validationResult) {
		validationResult.class.getMethod("getDefinition", null).invoke(validationResult) as String
	}

	def failureReason(Object validationResult) {
		validationResult.class.getMethod("getFailureReason", null).invoke(validationResult) as Optional<String>
	}
	
	
}
