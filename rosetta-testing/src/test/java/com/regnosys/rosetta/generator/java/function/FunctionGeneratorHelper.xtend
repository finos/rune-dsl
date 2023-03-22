package com.regnosys.rosetta.generator.java.function

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Injector
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.rosetta.model.lib.functions.ConditionValidator
import com.rosetta.model.lib.functions.DefaultConditionValidator
import com.rosetta.model.lib.functions.ModelObjectValidator
import com.rosetta.model.lib.functions.NoOpModelObjectValidator
import com.rosetta.model.lib.functions.RosettaFunction
import java.util.Map
import java.util.function.Consumer
import org.eclipse.xtext.xbase.testing.RegisteringFileSystemAccess

import static org.junit.jupiter.api.Assertions.*

class FunctionGeneratorHelper {

	@Inject FunctionGenerator generator
	@Inject extension ModelHelper
	@Inject extension CodeGeneratorTestHelper
	@Inject RegisteringFileSystemAccess fsa
	@Inject JavaNames.Factory factory

	final Injector injector
	
	new() {
		injector = Guice.createInjector(new AbstractModule() {
			override protected configure() {
				bind(ConditionValidator).toInstance(new DefaultConditionValidator)
				bind(ModelObjectValidator).toInstance(new NoOpModelObjectValidator)
			}
		})
	}
	
	def createFunc(Map<String, Class<?>> classes, String funcName) {
		val func = classes.get(rootPackage.functions + '.' + funcName) // get abstract func class
				.declaredClasses.get(0) // get default func implementation (e.g. inner class) 
				.declaredConstructor.newInstance
				as RosettaFunction 
		injector.injectMembers(func)
		return func
	}
	
	def <T> invokeFunc(RosettaFunction func, Class<T> resultClass, Object... inputs) {
		val evaluateMethod = func.class.getMatchingMethod("evaluate", inputs.map[class])
		evaluateMethod.invoke(func, inputs) as T
	}

	def void assertToGeneratedFunction(CharSequence actualModel, CharSequence expected) throws AssertionError {
		actualModel.assertToGenerated(expected, [
			generator.generate(factory.create(it), fsa, it.elements.filter(Function).filter[operations.nullOrEmpty].head, "test")
		])
	}

	def void assertToGeneratedCalculation(CharSequence actualModel, CharSequence expected) throws AssertionError {
		actualModel.assertToGenerated(expected, [
			generator.generate(factory.create(it), fsa, it.elements.filter(Function).filter[!operations.nullOrEmpty].head, "test")
		])
	}

	def protected void assertToGenerated(CharSequence actualModel, CharSequence expected,
		Consumer<RosettaModel> genCall) throws AssertionError {
		val model = actualModel.parseRosettaWithNoErrors
		genCall.accept(model)
		assertEquals(expected.toString, fsa.textFiles.entrySet.head.value)
	}
}
