package com.regnosys.rosetta.interpreternew

import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.junit.jupiter.api.Test
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper

import static com.google.common.collect.ImmutableMap.*
import static org.junit.jupiter.api.Assertions.*
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
import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue
import com.regnosys.rosetta.tests.util.ExpressionParser
import static org.junit.jupiter.api.Assertions.*;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.impl.ExpressionFactoryImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaInterpreterCompilerComparisonTest2 {
	
	@Inject extension CodeGeneratorTestHelper
	@Inject extension FunctionGeneratorHelper
	
	@Inject
	ExpressionParser parser;
	@Inject
	RosettaInterpreterNew interpreter;
	
	@Test
	def void simpleTest() {
		val model = '''
			func Foo:
				output: b int (1..1)
				set b: 5
		'''.generateCode
		
		// Biggest problem I see is that there's no just way to run a piece of code
		// from the code text itself
		// You need to tell it specifically what function its supposed to be 
		// creating, running and getting output from
		// Though I guess it could be standardised by calling all functions
		// the same and passing some arguments?
		// This would require writing a lot of code manually and testing it for
		// both runtime options
		val classes = model.compileToClasses
		val foo = classes.createFunc('Foo')
		val output = foo.invokeFunc(Integer)
		
		val expr = parser.parseExpression("5")
		val value = interpreter.interp(expr)
		
		// If actually doing this then better approach would be
		// To have like a helper method that converts a primitive type
		// Into some value domain type to avoid having to manually set it
		assertEquals(value, new RosettaInterpreterNumberValue(output))
	}
}

// I couldn't get a way to import this so I just put it here??????
class FunctionGeneratorHelper {

	@Inject FunctionGenerator generator
	@Inject extension ModelHelper
	@Inject extension CodeGeneratorTestHelper
	@Inject RegisteringFileSystemAccess fsa

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
		injector.getInstance(classes.get(rootPackage.functions + '.' + funcName)) as RosettaFunction
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
			generator.generate(new RootPackage(it), fsa, it.elements.filter(Function).filter[operations.nullOrEmpty].head, "test")
		])
	}

	def void assertToGeneratedCalculation(CharSequence actualModel, CharSequence expected) throws AssertionError {
		actualModel.assertToGenerated(expected, [
			generator.generate(new RootPackage(it), fsa, it.elements.filter(Function).filter[!operations.nullOrEmpty].head, "test")
		])
	}

	def protected void assertToGenerated(CharSequence actualModel, CharSequence expected,
		Consumer<RosettaModel> genCall) throws AssertionError {
		val model = actualModel.parseRosettaWithNoErrors
		genCall.accept(model)
		assertEquals(expected.toString, fsa.textFiles.entrySet.head.value)
	}
}