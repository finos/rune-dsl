package com.regnosys.rosetta.tests.util

import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.rule.DataRuleGenerator
import java.util.Map
import java.util.Optional
import com.rosetta.model.lib.path.RosettaPath

class DataRuleHelper {
	
	@Inject extension ModelHelper

	def runDataRule(Map<String, Class<?>> classes, Object instance, String dataRuleName) {
		val dataRuleClass = classes.get(javaPackages.dataRule.packageName + '.' + DataRuleGenerator.dataRuleClassName(dataRuleName))
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
	
	def printDataRule(Map<String, String> code, String name) {
		println(code.get(javaPackages.dataRule.packageName + '.' + DataRuleGenerator.dataRuleClassName(name)))
	}
	
}
