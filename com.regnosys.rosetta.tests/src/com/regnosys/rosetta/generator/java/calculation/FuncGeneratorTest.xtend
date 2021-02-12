package com.regnosys.rosetta.generator.java.calculation

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import static com.regnosys.rosetta.validation.RosettaIssueCodes.*


import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import com.regnosys.rosetta.rosetta.simple.SimplePackage

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class FuncGeneratorTest {

	@Inject extension FuncGeneratorHelper
	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper
	@Inject extension ValidationTestHelper

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
					final String result = assignOutput(resultHolder, name, name2);
					
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
	
	@Test
	def void shouldGenerateFuncWithNestedBooleanExpressionCondition() {
		val code = '''
			type Money:
				amount number (1..1)
				currency string (1..1)
			
			func Foo:
			 	inputs:
			 		m1 Money  (0..1)
					m2 Money (0..1)
					currency string (0..1)
				output:
					result string (0..1)
				
				condition:
					[ m1 -> currency , m2 -> currency ] = currency
		'''.generateCode
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateFuncWithKeyReferenceFromAnotherNamespace() {

		val code = #[
		'''
			namespace "com.rosetta.test.model.party"
			version "test"

			type Party:
				[metadata key]
				id number (1..1)
				name string (1..1)
		''',
		'''
			namespace "com.rosetta.test.model.agreement"
			version "test"
			
			import com.rosetta.test.model.party.*

			type Agreement:
				id number (1..1)
				party Party (1..1)
					[metadata reference]
		'''
		,
		'''
			namespace "com.rosetta.test.model.func"
			version "test"

			import com.rosetta.test.model.party.*
			import com.rosetta.test.model.agreement.*

			func Create_Agreement:
			 	inputs:
			 		party Party (1..1)
					id number (1..1)
				output:
					agreement Agreement (1..1)
					
				assign-output agreement -> id: id
				assign-output agreement -> party: party as-key

		'''
		].generateCode
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateFunctionWithAssignemtnAsReference() {

		#[
		'''
			namespace "com.rosetta.test.model.party"
			version "test"

			type Party:
				id number (1..1)
				name MyData (1..1)
			
			type MyData:
				val string (1..1)
		''',
		'''
			namespace "com.rosetta.test.model.agreement"
			version "test"
			
			import com.rosetta.test.model.party.*

			type Agreement:
				id number (1..1)
				party Party (1..1)
				
				condition AgreementValid:
					if get_Party_Id() exists
						then id is absent

			func get_Party_Id:
			 	inputs:
			 		agreement Agreement (1..1)
				output:
					result MyData (1..1)
					
				assign-output result : agreement -> party -> name
				

		'''
		].generateCode
		//.writeClasses("shouldGenerateFunctionWithAssignemtnAsReference")
		.compileToClasses
	}
	
	@Test
	def void shouldGenerateFunctionWithConditionalAssignment() {
		#[
		'''
			namespace "com.rosetta.test.model.agreement"
					version "test"
			
			type Top:
				foo Foo (1..*)
			
			type Foo:
				bar1 Bar (0..1)
				bar2 Bar (0..1)
				
			type Bar:
				id number (1..1)
			
			func ExtractBar: <"Extracts a bar">
				inputs: top Top (1..1)
				output: bar Bar (1..1)
				alias foo: top -> foo  only-element
				assign-output bar:
					if foo -> bar1 exists then foo -> bar1
					//else if foo -> bar2 exists then foo -> bar2
		'''
		].generateCode
		//.writeClasses("shouldGenerateFunctionWithConditionalAssignment")
		.compileToClasses
	}
	
	@Test
	def void shouldGenerateFunctionWithCreationLHSUsingAlias() {
		val code = #[
		'''
			namespace "com.rosetta.test.model.agreement"
					version "test"
			
			type Top:
				foo Foo (1..1)
			
			type Foo:
				bar1 Bar (0..1)
				bar2 Bar (0..1)
				
			type Bar:
				id number (1..1)
			
			func ExtractBar: <"Extracts a bar">
				inputs: top Top (1..1)
				output: topOut Top (1..1)
				alias fooAlias : topOut -> foo
				assign-output fooAlias -> bar1:
					top -> foo -> bar1
				assign-output topOut -> foo -> bar2:
					top -> foo -> bar2
		'''
		].generateCode
		//.writeClasses("shouldGenerateFunctionWithCreationLHS")
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateDisjoint() {
		val code = #[
		'''
			namespace "com.rosetta.test.model.agreement"
					version "test"
			
			type Top:
				foo Foo (1..*)
			
			type Foo:
				bar1 number (0..1)
			
			func Disjoint: <"checks disjoint">
				inputs: 
					top1 Top (1..1)
					top2 Top (1..1)
				
				output: result boolean (1..1)
				assign-output result:
					top1-> foo disjoint top2 -> foo
		'''
		].generateCode
		//.writeClasses("shouldGenerateDisjoint")
		code.compileToClasses
	}
	
	@Test
	def void shouldNotGenerateDisjointDifferentTypes() {
		val model = 
		'''
			namespace "com.rosetta.test.model.agreement"
					version "test"
			
			type Top:
				foo Foo (1..*)
				bar string (1..*)
			
			type Foo:
				bar1 number (0..1)
			
			func ExtractBar: <"tries disjoint differnt types">
				inputs: 
					top1 Top (1..1)
					top2 Top (1..1)
				
				output: result boolean (1..1)
				assign-output result:
					top1-> foo disjoint top2 -> bar
		'''.parseRosetta
		
		
		model.assertError(ROSETTA_DISJOINT_EXPRESSION, null, 
			"Disjoint must operate on lists of the same type")
	}
	
	@Test
	def void shouldNotAndInts() {
		val model = 
		'''
			namespace "com.rosetta.test.model.agreement"
					version "test"
			
			type Top:
				foo Foo (1..1)
			
			type Foo:
				bar1 number (1..1)
			
			func ExtractBar: <"tries anding integers">
				inputs: 
					top1 Top (1..1)
					top2 Top (1..1)
				
				output: result int (1..1)
				assign-output result:
					top1-> foo and top2 -> foo
		'''.parseRosetta
		
		
		model.assertError(SimplePackage.Literals.OPERATION, TYPE_ERROR, 
			"left hand side of and expression must be boolean")
	}
		
	
}
