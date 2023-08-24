package com.regnosys.rosetta.generator.java.condition

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.functions.ConditionValidator
import com.rosetta.model.lib.functions.DefaultConditionValidator
import com.rosetta.model.lib.functions.ModelObjectValidator
import com.rosetta.model.lib.functions.NoOpModelObjectValidator
import com.rosetta.model.lib.validation.Validator
import java.util.Map
import java.util.Optional
import javax.inject.Inject

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
	
	def createConditionInstance(Map<String, Class<?>> classes, String conditionName) {
		createConditionInstance(classes, rootPackage, conditionName)
	}
	
	def createConditionInstance(Map<String, Class<?>> classes, RootPackage rootNamespace, String conditionName) {
		val conditionClass = classes.get(rootNamespace.condition + '.' + conditionName.toConditionJavaType)
		val cond = injector.getInstance(conditionClass)
		return cond as Validator<RosettaModelObject>
	}
	
	def runCondition(Map<String, Class<?>> classes, Object instance, String conditionName) {
		runCondition(classes, rootPackage, instance, conditionName)
	}
	
	def runCondition(Map<String, Class<?>> classes, RootPackage rootNamespace, Object instance, String conditionName) {
		val conditionInstance = createConditionInstance(classes, rootNamespace, conditionName)
		return conditionInstance.validate(null, instance as RosettaModelObject)
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
