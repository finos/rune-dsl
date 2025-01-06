package com.regnosys.rosetta.generator.java.function

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
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
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import java.lang.reflect.InvocationTargetException
import javax.inject.Inject
import com.regnosys.rosetta.utils.ModelIdProvider
import com.rosetta.util.DottedPath

class FunctionGeneratorHelper {

	@Inject FunctionGenerator generator
	@Inject extension ModelHelper
	@Inject extension CodeGeneratorTestHelper
	@Inject RegisteringFileSystemAccess fsa
	@Inject extension ModelIdProvider

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
		createFunc(classes, funcName, rootPackage.functions)
	}
	def createFunc(Map<String, Class<?>> classes, String funcName, DottedPath packageName) {
		injector.getInstance(classes.get(packageName + '.' + funcName)) as RosettaFunction
	}
	
	def <T> invokeFunc(RosettaFunction func, Class<T> resultClass, Object... inputs) {
		val evaluateMethod = func.class.getMatchingMethod("evaluate", inputs.map[it?.class])
		try {
			evaluateMethod.invoke(func, inputs) as T
		} catch (InvocationTargetException e) {
			throw e.cause
		}
	}

	def void assertToGeneratedFunction(CharSequence actualModel, CharSequence expected) throws AssertionError {
		actualModel.assertToGenerated(expected, [
			generator.generate(new RootPackage(it.toDottedPath), fsa, it.elements.filter(Function).filter[operations.nullOrEmpty].head, "test")
		])
	}

	def void assertToGeneratedCalculation(CharSequence actualModel, CharSequence expected) throws AssertionError {
		actualModel.assertToGenerated(expected, [
			generator.generate(new RootPackage(it.toDottedPath), fsa, it.elements.filter(Function).filter[!operations.nullOrEmpty].head, "test")
		])
	}

	def protected void assertToGenerated(CharSequence actualModel, CharSequence expected,
		Consumer<RosettaModel> genCall) throws AssertionError {
		val model = actualModel.parseRosettaWithNoErrors
		genCall.accept(model)
		assertEquals(expected.toString, fsa.textFiles.entrySet.head.value)
	}
}
