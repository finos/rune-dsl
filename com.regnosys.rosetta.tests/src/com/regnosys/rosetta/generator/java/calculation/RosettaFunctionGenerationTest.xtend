package com.regnosys.rosetta.generator.java.calculation

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaFunctionGenerationTest {

	@Inject extension FuncGeneratorHelper
	@Inject extension CodeGeneratorTestHelper

	@Test
	def void testSimpleFunctionGeneration() {
		'''
			func FuncFoo:
			 	inputs:
			 		name string  (0..1)
					name2 string (0..1)
				output:
					result string (0..1)
		'''.assertToGeneratedFunction(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.functions.RosettaFunction;
			
			
			@ImplementedBy(FuncFoo.FuncFooDefault.class)
			public abstract class FuncFoo implements RosettaFunction {
			
				/**
				* @param name 
				* @param name2 
				* @return result 
				*/
				public String evaluate(String name, String name2) {
					
					String resultHolder = doEvaluate(name, name2);
					String result = assignOutput(resultHolder, name, name2);
					
					return result;
				}
				
				private String assignOutput(String resultHolder, String name, String name2) {
					return resultHolder;
				}
			
				protected abstract String doEvaluate(String name, String name2);
				
				public static final class FuncFooDefault extends FuncFoo {
					@Override
					protected  String doEvaluate(String name, String name2) {
						return null;
					}
				}
			}
			'''
		)
	}

	
	@Test
	def void shouldGenerateFuncWithAssignOutputDoIfBooleanLiterals() {
		val code = '''
			func Foo:
				inputs:
					foo int (0..1)
				output: 
					result boolean (1..1)
					
				assign-output result: 
					if foo exists
					then False
					else True
		'''.generateCode
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateFuncWithAssignOutputDoIfBooleanLiteralsAndNoElse() {
		val code = '''
			func Foo:
				inputs:
					foo int (0..1)
				output: 
					result boolean (1..1)
					
				assign-output result: 
					if foo exists
					then False
		'''.generateCode
		code.compileToClasses
	}

	@Test
	def void shouldGenerateFuncWithAssignOutputDoIfFuncCall() {
		val code = '''
			func Bar:
				inputs:
					bar number (0..1)
				output: 
					result number (1..1)
			
			func Foo:
				inputs:
					foo number (0..1)
				output: 
					result number (1..1)
				
				assign-output result: 
					if foo exists
					then Bar( foo )
					else 0.0
		'''.generateCode
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateFuncWithAssignOutputDoIfFuncCallAndElseBoolean() {
		val code = '''
			func Bar:
				inputs:
					bar number (0..1)
				output: 
					result boolean (1..1)
			
			func Foo:
				inputs:
					foo number (0..1)
				output: 
					result boolean (1..1)
				
				assign-output result: 
					if foo exists
					then Bar( foo )
					else True
		'''.generateCode
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateFuncWithAssignOutputDoIfFuncCallAndNoElse() {
		val code = '''
			func Bar:
				inputs:
					bar number (0..1)
				output: 
					result boolean (1..1)
			
			func Foo:
				inputs:
					foo number (0..1)
				output: 
					result boolean (1..1)
				
				assign-output result: 
					if foo exists
					then Bar( foo )
		'''.generateCode
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateFuncWithAssignOutputDoIfBigDecimalAndFeatureCall() {
		val code = '''
			type Bar:
				baz number (1..1)
			
			func Foo:
				inputs:
					bar Bar (0..1)
				output: 
					result number (1..1)
				
				assign-output result: 
					if bar exists
					then 30.0
					else bar -> baz
		'''.generateCode
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateFuncWithAssignOutputDoIfComparisonResultAndElseBoolean() {
		val code = '''
			type Bar:
				baz number (1..1)
			
			func Foo:
				inputs:
					bar Bar (0..1)
				output: 
					result boolean (1..1)
				
				assign-output result: 
					if bar -> baz exists
					then bar -> baz > 5
					else True
		'''.generateCode
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateFuncWithAssignOutputDoIfComparisonResultAndNoElse() {
		val code = '''
			type Bar:
				baz number (1..1)
			
			func Foo:
				inputs:
					bar Bar (0..1)
				output: 
					result boolean (1..1)
				
				assign-output result: 
					if bar -> baz exists
					then bar -> baz > 5
		'''.generateCode
		code.compileToClasses
	}
}
