package com.regnosys.rosetta.generator.java.calculation

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.simple.SimplePackage
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.regnosys.rosetta.validation.RosettaIssueCodes
import java.util.Arrays
import java.util.List
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.IsCollectionContaining.hasItems
import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class FuncGeneratorTest {

	@Inject extension FuncGeneratorHelper
	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper
	@Inject extension ValidationTestHelper

	@Test
	def void testSimpleFunctionGeneration() {
		val code = '''
			func FuncFoo:
			 	inputs:
			 		name string  (0..1)
			 		name2 string (0..1)
				output:
					result string (0..1)
		'''
		code.assertToGeneratedFunction(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import java.util.Arrays;
				
				
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
					
					private String assignOutput(String result, String name, String name2) {
						return result;
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
		code.generateCode.compileToClasses
	}

	@Test
	def void shouldGenerateFunctionWithStringListOutput() {
		val code = '''
			func FuncFoo:
			 	inputs:
			 		name string  (0..1)
			 		name2 string (0..1)
				output:
					result string (0..*)
		'''
		code.assertToGeneratedFunction(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import java.util.Arrays;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param name 
					* @param name2 
					* @return result 
					*/
					public List<String> evaluate(String name, String name2) {
						
						List<String> resultHolder = doEvaluate(name, name2);
						List<String> result = assignOutput(resultHolder, name, name2);
						
						return result;
					}
					
					private List<String> assignOutput(List<String> result, String name, String name2) {
						return result;
					}
				
					protected abstract List<String> doEvaluate(String name, String name2);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<String> doEvaluate(String name, String name2) {
							return Arrays.asList();
						}
					}
				}
			'''
		)
		code.generateCode.compileToClasses
	}

	@Test
	def void shouldGenerateFunctionWithNumberListOutput() {
		val code = '''
			func FuncFoo:
			 	inputs:
			 		name string  (0..1)
			 		name2 string (0..1)
				output:
					result number (0..*)
		'''
		code.assertToGeneratedFunction(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import java.math.BigDecimal;
				import java.util.Arrays;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param name 
					* @param name2 
					* @return result 
					*/
					public List<BigDecimal> evaluate(String name, String name2) {
						
						List<BigDecimal> resultHolder = doEvaluate(name, name2);
						List<BigDecimal> result = assignOutput(resultHolder, name, name2);
						
						return result;
					}
					
					private List<BigDecimal> assignOutput(List<BigDecimal> result, String name, String name2) {
						return result;
					}
				
					protected abstract List<BigDecimal> doEvaluate(String name, String name2);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<BigDecimal> doEvaluate(String name, String name2) {
							return Arrays.asList();
						}
					}
				}
			'''
		)
		code.generateCode.compileToClasses
	}

	@Test
	def void shouldGenerateFunctionWithIntListOutput() {
		val code = '''
			func FuncFoo:
			 	inputs:
			 		name string  (0..1)
			 		name2 string (0..1)
				output:
					result int (0..*)
		'''
		code.assertToGeneratedFunction(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import java.util.Arrays;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param name 
					* @param name2 
					* @return result 
					*/
					public List<Integer> evaluate(String name, String name2) {
						
						List<Integer> resultHolder = doEvaluate(name, name2);
						List<Integer> result = assignOutput(resultHolder, name, name2);
						
						return result;
					}
					
					private List<Integer> assignOutput(List<Integer> result, String name, String name2) {
						return result;
					}
				
					protected abstract List<Integer> doEvaluate(String name, String name2);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Integer> doEvaluate(String name, String name2) {
							return Arrays.asList();
						}
					}
				}
			'''
		)
		code.generateCode.compileToClasses
	}

	@Test
	def void shouldGenerateFunctionWithDateListOutput() {
		val code = '''
			func FuncFoo:
			 	inputs:
			 		name string  (0..1)
			 		name2 string (0..1)
				output:
					result date (0..*)
		'''
		code.assertToGeneratedFunction(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.records.Date;
				import java.util.Arrays;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param name 
					* @param name2 
					* @return result 
					*/
					public List<Date> evaluate(String name, String name2) {
						
						List<Date> resultHolder = doEvaluate(name, name2);
						List<Date> result = assignOutput(resultHolder, name, name2);
						
						return result;
					}
					
					private List<Date> assignOutput(List<Date> result, String name, String name2) {
						return result;
					}
				
					protected abstract List<Date> doEvaluate(String name, String name2);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Date> doEvaluate(String name, String name2) {
							return Arrays.asList();
						}
					}
				}
			'''
		)
		code.generateCode.compileToClasses
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
		// .writeClasses("shouldGenerateFuncWithAssignOutputDoIfBooleanLiteralsAndNoElse")
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
		// .writeClasses("shouldGenerateFuncWithAssignOutputDoIfComparisonResultAndNoElse")
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
				namespace com.rosetta.test.model.party
				version "test"
				
				type Party:
					[metadata key]
					id number (1..1)
					name string (1..1)
			''',
			'''
				namespace com.rosetta.test.model.agreement
				version "test"
				
				import com.rosetta.test.model.party.*
				
				type Agreement:
					id number (1..1)
					party Party (1..1)
						[metadata reference]
			''',
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
				namespace com.rosetta.test.model.party
				version "test"
				
				type Party:
					id number (1..1)
					name MyData (1..1)
				
				type MyData:
					val string (1..1)
			''',
			'''
				namespace com.rosetta.test.model.agreement
				version "test"
				
				import com.rosetta.test.model.party.*
				
				type Agreement:
					id number (1..1)
					party Party (1..1)
				
					condition AgreementValid:
					if Get_Party_Id() exists
						then id is absent
				
				func Get_Party_Id:
				 	inputs:
				 		agreement Agreement (1..1)
					output:
						result MyData (1..1)
				
					assign-output result : agreement -> party -> name
				
				
			'''
		].generateCode // .writeClasses("shouldGenerateFunctionWithAssignemtnAsReference")
		.compileToClasses
	}

	@Test
	def void shouldGenerateFunctionWithAssignmentAsMeta() {

		#[
			'''
				namespace com.rosetta.test.model.party
				version "test"
				
				type Party:
					id number (1..1)
					name string (1..1)
				
				type MyData:
					val Party (1..1)
						[metadata id]
			''',
			'''
				namespace com.rosetta.test.model.agreement
				version "test"
				
				import com.rosetta.test.model.party.*
				
				type Agreement:
					id number (1..1)
					party Party (1..1)
						[metadata id]
				
					condition AgreementValid:
						if Get_Party_Id() exists
							then id is absent
				
				func Get_Party_Id:
				 	inputs:
				 		agreement Agreement (1..1)
					output:
						result MyData (1..1)
				
					assign-output result-> val : agreement -> party
				
				
			'''
		].generateCode // .writeClasses("shouldGenerateFunctionWithAssignmentAsMeta")
		.compileToClasses
	}

	@Test
	def void shouldGenerateFunctionWithConditionalAssignment() {
		#[
			'''
				namespace com.rosetta.test.model.agreement
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
		].generateCode // .writeClasses("shouldGenerateFunctionWithConditionalAssignment")
		.compileToClasses
	}

	@Test
	def void shouldGenerateFunctionWithCreationLHSUsingAlias() {
		val code = #[
			'''
				namespace com.rosetta.test.model.agreement
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
		// .writeClasses("shouldGenerateFunctionWithCreationLHS")
		code.compileToClasses
	}

	@Test
	def void shouldGenerateFunctionWithAliasAssignOutput() {
		val code = #[
			'''
				namespace com.rosetta.test.model.agreement
				version "test"
				
				type Top:
					foo Foo (1..1)
				
				type Foo:
					bar Bar (0..1)
				
				type Bar:
					id number (1..1)
				
				func UpdateBarId: <"Updates Bar.id by assign-output on an alias">
					inputs:
						top Top (1..1)
						newId number (1..1)
				
					output:
						topOut Top (1..1)
				
					alias barAlias :
						topOut -> foo -> bar
				
					assign-output barAlias -> id:
						newId
			'''
		].generateCode
		// .writeClasses("shouldGenerateFunctionWithAliasAssignOutput")
		code.compileToClasses
	}

	@Test
	def void shouldGenerateDisjoint() {
		val code = #[
			'''
				namespace com.rosetta.test.model.agreement
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
		// .writeClasses("shouldGenerateDisjoint")
		code.compileToClasses
	}

	@Test
	def void shouldNotGenerateDisjointDifferentTypes() {
		val model = '''
			namespace com.rosetta.test.model.agreement
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

		model.assertError(ROSETTA_DISJOINT_EXPRESSION, null, "Disjoint must operate on lists of the same type")
	}

	@Test
	def void shouldNotAndInts() {
		val model = '''
			namespace com.rosetta.test.model.agreement
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

		model.assertError(SimplePackage.Literals.OPERATION, RosettaIssueCodes.TYPE_ERROR,
			"left hand side of and expression must be boolean")
	}

	@Test
	def void shouldReturnMultiple() {
		val model = '''
			namespace com.rosetta.test.model.agreement
			version "test"
			
			type Top:
				foo Foo (1..*)
				foob Foo (1..1)
			
			type Foo:
				bar1 number (1..*)
				
			func ExtractFoo: <"tries returning list of complex">
				inputs: 
					top1 Top (1..1)
				
				output: result Foo (1..*)
				assign-output result:
					top1-> foo
					
			func ExtractFoowithAlias: <"tries returning list of complex">
				inputs: 
					top1 Top (1..1)
				output: result Foo (1..1)
				alias foos: top1->foo 
				assign-output result:
					foos
			
			func ExtractBar: <"tries returning list of basic">
				inputs: 
					top1 Top (1..1)
				
				output: result number (1..*)
				assign-output result:
					top1-> foo -> bar1
		'''.parseRosettaWithNoErrors
		model.generateCode // .writeClasses("shouldReturnMultiple")
		.compileToClasses
	}

	@Test
	def void funcCallingMultipleFunc() {
		val model = '''
			func F1:
				inputs: f1Input date (1..1)
				output: f1OutputList date (1..*)
					
			func F2:
				inputs: f2InputList date (1..*)
				output: f2Output date (1..1)
				
			func F3:
				inputs: f3Input date (1..1)
				output: f3Output date (1..1)
				assign-output f3Output: F2(F1(f3Input))
		'''
		val code = model.generateCode
		val f3 = code.get("com.rosetta.test.model.functions.F3")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.records.Date;
				import com.rosetta.test.model.functions.F1;
				import com.rosetta.test.model.functions.F2;
				import java.util.Arrays;
				
				
				@ImplementedBy(F3.F3Default.class)
				public abstract class F3 implements RosettaFunction {
					
					// RosettaFunction dependencies
					//
					@Inject protected F1 f1;
					@Inject protected F2 f2;
				
					/**
					* @param f3Input 
					* @return f3Output 
					*/
					public Date evaluate(Date f3Input) {
						
						Date f3OutputHolder = doEvaluate(f3Input);
						Date f3Output = assignOutput(f3OutputHolder, f3Input);
						
						return f3Output;
					}
					
					private Date assignOutput(Date f3Output, Date f3Input) {
						f3Output = MapperS.of(f2.evaluate(MapperC.of(f1.evaluate(MapperS.of(f3Input).get())).getMulti())).get();
						return f3Output;
					}
				
					protected abstract Date doEvaluate(Date f3Input);
					
					public static final class F3Default extends F3 {
						@Override
						protected  Date doEvaluate(Date f3Input) {
							return null;
						}
					}
				}
			'''.toString,
			f3
		)
		code.compileToClasses

	}

	@Test
	def void funcCallingMultipleFunc2() {
		val model = '''
			func F1:
				inputs: f1Input date (1..1)
				output: f1OutputList date (1..*)
					
			func F2:
				inputs: f2InputList date (1..*)
				output: f2Output date (1..1)
				
			func F3:
				inputs: f3Input date (1..1)
				output: f3Output date (1..1)
				alias f1OutList: F1(f3Input)
				assign-output f3Output: F2(f1OutList)
		'''
		val code = model.generateCode
		val f1 = code.get("com.rosetta.test.model.functions.F1")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.records.Date;
				import java.util.Arrays;
				import java.util.List;
				
				
				@ImplementedBy(F1.F1Default.class)
				public abstract class F1 implements RosettaFunction {
				
					/**
					* @param f1Input 
					* @return f1OutputList 
					*/
					public List<Date> evaluate(Date f1Input) {
						
						List<Date> f1OutputListHolder = doEvaluate(f1Input);
						List<Date> f1OutputList = assignOutput(f1OutputListHolder, f1Input);
						
						return f1OutputList;
					}
					
					private List<Date> assignOutput(List<Date> f1OutputList, Date f1Input) {
						return f1OutputList;
					}
				
					protected abstract List<Date> doEvaluate(Date f1Input);
					
					public static final class F1Default extends F1 {
						@Override
						protected  List<Date> doEvaluate(Date f1Input) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f1
		)
		val f2 = code.get("com.rosetta.test.model.functions.F2")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.records.Date;
				import java.util.Arrays;
				import java.util.List;
				
				
				@ImplementedBy(F2.F2Default.class)
				public abstract class F2 implements RosettaFunction {
				
					/**
					* @param f2InputList 
					* @return f2Output 
					*/
					public Date evaluate(List<Date> f2InputList) {
						
						Date f2OutputHolder = doEvaluate(f2InputList);
						Date f2Output = assignOutput(f2OutputHolder, f2InputList);
						
						return f2Output;
					}
					
					private Date assignOutput(Date f2Output, List<Date> f2InputList) {
						return f2Output;
					}
				
					protected abstract Date doEvaluate(List<Date> f2InputList);
					
					public static final class F2Default extends F2 {
						@Override
						protected  Date doEvaluate(List<Date> f2InputList) {
							return null;
						}
					}
				}
			'''.toString,
			f2
		)
		val f3 = code.get("com.rosetta.test.model.functions.F3")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.Mapper;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.records.Date;
				import com.rosetta.test.model.functions.F1;
				import com.rosetta.test.model.functions.F2;
				import java.util.Arrays;
				
				
				@ImplementedBy(F3.F3Default.class)
				public abstract class F3 implements RosettaFunction {
					
					// RosettaFunction dependencies
					//
					@Inject protected F1 f1;
					@Inject protected F2 f2;
				
					/**
					* @param f3Input 
					* @return f3Output 
					*/
					public Date evaluate(Date f3Input) {
						
						Date f3OutputHolder = doEvaluate(f3Input);
						Date f3Output = assignOutput(f3OutputHolder, f3Input);
						
						return f3Output;
					}
					
					private Date assignOutput(Date f3Output, Date f3Input) {
						f3Output = MapperS.of(f2.evaluate(MapperC.of(f1OutList(f3Input).getMulti()).getMulti())).get();
						return f3Output;
					}
				
					protected abstract Date doEvaluate(Date f3Input);
					
					
					protected Mapper<Date> f1OutList(Date f3Input) {
						return MapperC.of(f1.evaluate(MapperS.of(f3Input).get()));
					}
					public static final class F3Default extends F3 {
						@Override
						protected  Date doEvaluate(Date f3Input) {
							return null;
						}
					}
				}
			'''.toString,
			f3
		)
		code.compileToClasses

	}

	@Test
	def void funcCallingMultipleFuncWithAlias() {
		val model = '''
			namespace "demo"
			version "${project.version}"
			
			type Number:
				num number (1..1)
			
			func F1:
				inputs: num number (1..1)
				output: numbers Number (1..*)
			
			func F2:
				inputs: nums number(1..*)
				output: str string (1..1)
			
			func F3:
				inputs: num number (1..1)
				output: str string (1..1)
			
				alias f1: F1(num)
				assign-output str: F2(f1 -> num)
			
			func F4:
				inputs: num number (1..*)
				output: str string (1..1)
			
				alias f2: F2(num)
			
				assign-output str: f2
			
		'''.parseRosettaWithNoErrors
		model.generateCode// .writeClasses("funcCallingMultipleFuncWithAlias")
		.compileToClasses
	}

	@Test
	def void typeWithCondition() {
		val model = '''
			namespace "demo"
			version "${project.version}"
			
			type Foo:
				bar Bar (1..1)
			
				condition XXX:
				if bar -> num exists
				then bar -> zap contains Zap -> A
				and if bar -> zap contains Zap -> A
				then bar -> num exists
			
			type Bar:
				num number (1..1)
				zap Zap (1..1)
			
			enum Zap:
				A B C
			
		'''.parseRosettaWithNoErrors
		model.generateCode // .writeClasses("typeWithCondition").compileToClasses
	}

	@Test
	def void funcUsingListEquals() {
		val model = '''
			namespace "demo"
			version "${project.version}"
			
			type T1:
					num number (1..1)
					nums number (1..*)
			
			func F1:
				inputs: t1 T1(1..1)
						t2 T1(1..1)
				output: res boolean (1..1)
				assign-output res: t1->num = t2->nums
			
		'''.parseRosetta
		model.assertWarning(ROSETTA_BINARY_OPERATION, null,
			"Comparison operator = should specify 'all' or 'any' when comparing a list to a single value")
	}

	@Test
	def void funcUsingListEqualsAll() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func F1:
				inputs:
					s1 string (1..1)
					s2 string (1..*)
				output:
					res boolean (1..1)
				assign-output res: s1 all = s2
			
		'''.generateCode
		val classes = code.compileToClasses

		val func = classes.createFunc("F1");
		assertTrue(func.invokeFunc(Boolean, "a", Arrays.asList("a", "a")))
		assertFalse(func.invokeFunc(Boolean, "a", Arrays.asList("a", "b")))
		assertFalse(func.invokeFunc(Boolean, "b", Arrays.asList("a", "a")))
	}

	@Test
	def void funcUsingListEqualsAny() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func F1:
				inputs:
					s1 string (1..1)
					s2 string (1..*)
				output:
					res boolean (1..1)
				assign-output res: s1 any = s2
			
		'''.generateCode
		val classes = code.compileToClasses

		val func = classes.createFunc("F1");
		assertTrue(func.invokeFunc(Boolean, "a", Arrays.asList("a", "a")))
		assertTrue(func.invokeFunc(Boolean, "a", Arrays.asList("a", "b")))
		assertFalse(func.invokeFunc(Boolean, "b", Arrays.asList("a", "a")))
	}

	@Test
	def void funcUsingListComparableEqualsAll() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func F1:
				inputs:
					n1 int (1..1)
					n2 int (1..*)
				output:
					res boolean (1..1)
				assign-output res: n1 all = n2
			
		'''.generateCode
		val classes = code.compileToClasses

		val func = classes.createFunc("F1");
		assertTrue(func.invokeFunc(Boolean, 1, Arrays.asList(1, 1)))
		assertFalse(func.invokeFunc(Boolean, 1, Arrays.asList(1, 2)))
		assertFalse(func.invokeFunc(Boolean, 2, Arrays.asList(1, 1)))
	}

	@Test
	def void funcUsingListComparableEqualsAny() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func F1:
				inputs:
					n1 int (1..1)
					n2 int (1..*)
				output:
					res boolean (1..1)
				assign-output res: n1 any = n2
			
		'''.generateCode
		val classes = code.compileToClasses

		val func = classes.createFunc("F1");
		assertTrue(func.invokeFunc(Boolean, 1, Arrays.asList(1, 1)))
		assertTrue(func.invokeFunc(Boolean, 1, Arrays.asList(1, 2)))
		assertFalse(func.invokeFunc(Boolean, 2, Arrays.asList(1, 1)))
	}

	@Test
	def void funcUsingListNotEqualsAll() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func F1:
				inputs:
					s1 string (1..1)
					s2 string (1..*)
				output:
					res boolean (1..1)
				assign-output res: s1 all <> s2
			
		'''.generateCode
		val classes = code.compileToClasses

		val func = classes.createFunc("F1");
		assertFalse(func.invokeFunc(Boolean, "a", Arrays.asList("a", "a")))
		assertFalse(func.invokeFunc(Boolean, "a", Arrays.asList("a", "b")))
		assertTrue(func.invokeFunc(Boolean, "b", Arrays.asList("a", "a")))
	}

	@Test
	def void funcUsingListNotEqualsAny() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func F1:
				inputs:
					s1 string (1..1)
					s2 string (1..*)
				output:
					res boolean (1..1)
				assign-output res: s1 any <> s2
			
		'''.generateCode
		val classes = code.compileToClasses

		val func = classes.createFunc("F1");
		assertFalse(func.invokeFunc(Boolean, "a", Arrays.asList("a", "a")))
		assertTrue(func.invokeFunc(Boolean, "a", Arrays.asList("a", "b")))
		assertTrue(func.invokeFunc(Boolean, "b", Arrays.asList("a", "a")))
	}

	@Test
	def void funcUsingListComparableNotEqualsAll() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func F1:
				inputs:
					n1 int (1..1)
					n2 int (1..*)
				output:
					res boolean (1..1)
				assign-output res: n1 all <> n2
			
		'''.generateCode
		val classes = code.compileToClasses

		val func = classes.createFunc("F1");
		assertFalse(func.invokeFunc(Boolean, 1, Arrays.asList(1, 1)))
		assertFalse(func.invokeFunc(Boolean, 1, Arrays.asList(1, 2)))
		assertTrue(func.invokeFunc(Boolean, 2, Arrays.asList(1, 1)))
	}

	@Test
	def void funcUsingListComparableNotEqualsAny() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func F1:
				inputs:
					n1 int (1..1)
					n2 int (1..*)
				output:
					res boolean (1..1)
				assign-output res: n1 any <> n2
			
		'''.generateCode
		val classes = code.compileToClasses

		val func = classes.createFunc("F1");
		assertFalse(func.invokeFunc(Boolean, 1, Arrays.asList(1, 1)))
		assertTrue(func.invokeFunc(Boolean, 1, Arrays.asList(1, 2)))
		assertTrue(func.invokeFunc(Boolean, 2, Arrays.asList(1, 1)))
	}

	@Test
	def void funcUsingListComparableGreaterThanAll() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func F1:
				inputs:
					n1 int (1..1)
					n2 int (1..*)
				output:
					res boolean (1..1)
				assign-output res: n1 all > n2
			
		'''.generateCode
		val classes = code.compileToClasses

		val func = classes.createFunc("F1");
		assertTrue(func.invokeFunc(Boolean, 2, Arrays.asList(1, 1)))
		assertFalse(func.invokeFunc(Boolean, 2, Arrays.asList(1, 2)))
		assertFalse(func.invokeFunc(Boolean, 1, Arrays.asList(2, 2)))
	}

	@Test
	def void funcUsingListComparableGreaterThanAny() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func F1:
				inputs:
					n1 int (1..1)
					n2 int (1..*)
				output:
					res boolean (1..1)
				assign-output res: n1 any > n2
			
		'''.generateCode
		val classes = code.compileToClasses

		val func = classes.createFunc("F1");
		// assertTrue(func.invokeFunc(Boolean, 2, Arrays.asList(1, 1)))
		assertTrue(func.invokeFunc(Boolean, 2, Arrays.asList(1, 2)))
	// assertFalse(func.invokeFunc(Boolean, 1, Arrays.asList(2, 2)))
	}

	@Test
	def void funcWithListOfIntDistinct() {
		val model = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			type Foo:
				n int (0..*)
			
			func DistinctFunc:
				inputs:
					foo Foo (0..1)
				output:
					res int (0..*)
				assign-output res: foo -> n distinct
			
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.DistinctFunc")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Foo;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(DistinctFunc.DistinctFuncDefault.class)
				public abstract class DistinctFunc implements RosettaFunction {
				
					/**
					* @param foo 
					* @return res 
					*/
					public List<Integer> evaluate(Foo foo) {
						
						List<Integer> resHolder = doEvaluate(foo);
						List<Integer> res = assignOutput(resHolder, foo);
						
						return res;
					}
					
					private List<Integer> assignOutput(List<Integer> res, Foo foo) {
						res = distinct(MapperS.of(foo).<Integer>mapC("getN", _foo -> _foo.getN())).getMulti();
						return res;
					}
				
					protected abstract List<Integer> doEvaluate(Foo foo);
					
					public static final class DistinctFuncDefault extends DistinctFunc {
						@Override
						protected  List<Integer> doEvaluate(Foo foo) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("DistinctFunc");
		val foo = classes.createInstanceUsingBuilder('Foo', of(), of('n', ImmutableList.of(1,1,1,2,2,3)))
		val res = func.invokeFunc(List, foo)
		assertEquals(3, res.size);
		assertThat(res, hasItems(1, 2, 3));
		
	}
	
	@Test
	def void funcWithListOfIntDistinct2() {
		val model = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func DistinctFunc:
				inputs:
					n int (0..*)
				output:
					res int (0..*)
				assign-output res: n distinct
			
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.DistinctFunc")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(DistinctFunc.DistinctFuncDefault.class)
				public abstract class DistinctFunc implements RosettaFunction {
				
					/**
					* @param n 
					* @return res 
					*/
					public List<Integer> evaluate(List<Integer> n) {
						
						List<Integer> resHolder = doEvaluate(n);
						List<Integer> res = assignOutput(resHolder, n);
						
						return res;
					}
					
					private List<Integer> assignOutput(List<Integer> res, List<Integer> n) {
						res = distinct(MapperC.of(n)).getMulti();
						return res;
					}
				
					protected abstract List<Integer> doEvaluate(List<Integer> n);
					
					public static final class DistinctFuncDefault extends DistinctFunc {
						@Override
						protected  List<Integer> doEvaluate(List<Integer> n) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("DistinctFunc");
		val res = func.invokeFunc(List, ImmutableList.of(1,1,1,2,2,3))
		assertEquals(3, res.size);
		assertThat(res, hasItems(1, 2, 3));
	}
	
	@Test
	def void funcWithListOfStringDistinct() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			type Foo:
				n string (0..*)
			
			func DistinctFunc:
				inputs:
					foo Foo (0..1)
				output:
					res string (0..*)
				assign-output res: foo -> n distinct
			
		'''.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("DistinctFunc");
		val foo = classes.createInstanceUsingBuilder('Foo', of(), of('n', ImmutableList.of("1","1","1","2","2","3")))
		val res = func.invokeFunc(List, foo)
		assertEquals(3, res.size);
		assertThat(res, hasItems("1", "2", "3"));
	}

	@Test
	def void funcWithListOfStringDistinct2() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func DistinctFunc:
				inputs:
					n string (0..*)
				output:
					res string (0..*)
				assign-output res: n distinct
			
		'''.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("DistinctFunc");
		val res = func.invokeFunc(List, ImmutableList.of("1","1","1","2","2","3"))
		assertEquals(3, res.size);
		assertThat(res, hasItems("1", "2", "3"));
	}

	@Test
	def void funcWithListOfComplexTypeDistinct() {
		val model = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			type Foo:
				barList Bar (0..*)
			
			type Bar:
				n int (0..1)
			
			func DistinctFunc:
				inputs:
					foo Foo (0..1)
				output:
					res Bar (0..*)
				assign-output res: foo -> barList distinct
			
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.DistinctFunc")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Bar.BarBuilder;
				import com.rosetta.test.model.Foo;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(DistinctFunc.DistinctFuncDefault.class)
				public abstract class DistinctFunc implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param foo 
					* @return res 
					*/
					public List<? extends Bar> evaluate(Foo foo) {
						
						List<Bar.BarBuilder> resHolder = doEvaluate(foo);
						List<Bar.BarBuilder> res = assignOutput(resHolder, foo);
						
						if (res!=null) objectValidator.validateAndFailOnErorr(Bar.class, res);
						return res;
					}
					
					private List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> res, Foo foo) {
						res = toBuilder(distinct(MapperS.of(foo).<Bar>mapC("getBarList", _foo -> _foo.getBarList())).getMulti())
						;
						return res;
					}
				
					protected abstract List<Bar.BarBuilder> doEvaluate(Foo foo);
					
					public static final class DistinctFuncDefault extends DistinctFunc {
						@Override
						protected  List<Bar.BarBuilder> doEvaluate(Foo foo) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("DistinctFunc");
		
		val bar1 = classes.createInstanceUsingBuilder('Bar', of('n', 1), of())
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('n', 2), of())
		val bar3 = classes.createInstanceUsingBuilder('Bar', of('n', 3), of())
		
		val barList = newArrayList
		barList.add(bar1)
		barList.add(bar1)
		barList.add(bar1)
		barList.add(bar2)
		barList.add(bar2)
		barList.add(bar3)
		
		val foo = classes.createInstanceUsingBuilder('Foo', of(), of('barList', barList))
		
		val res = func.invokeFunc(List, foo)
		assertEquals(3, res.size);
		assertThat(res, hasItems(bar1, bar2, bar3));
	}
	
	@Test
	def void funcWithListOfComplexTypeDistinct2() {
		val model = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			type Bar:
				n int (0..1)
			
			func DistinctFunc:
				inputs:
					barList Bar (0..*)
				output:
					res Bar (0..*)
				assign-output res: barList distinct
			
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.DistinctFunc")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Bar.BarBuilder;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(DistinctFunc.DistinctFuncDefault.class)
				public abstract class DistinctFunc implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param barList 
					* @return res 
					*/
					public List<? extends Bar> evaluate(List<? extends Bar> barList) {
						
						List<Bar.BarBuilder> resHolder = doEvaluate(barList);
						List<Bar.BarBuilder> res = assignOutput(resHolder, barList);
						
						if (res!=null) objectValidator.validateAndFailOnErorr(Bar.class, res);
						return res;
					}
					
					private List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> res, List<? extends Bar> barList) {
						res = toBuilder(distinct(MapperC.of(barList)).getMulti())
						;
						return res;
					}
				
					protected abstract List<Bar.BarBuilder> doEvaluate(List<? extends Bar> barList);
					
					public static final class DistinctFuncDefault extends DistinctFunc {
						@Override
						protected  List<Bar.BarBuilder> doEvaluate(List<? extends Bar> barList) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("DistinctFunc");
		
		val bar1 = classes.createInstanceUsingBuilder('Bar', of('n', 1), of())
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('n', 2), of())
		val bar3 = classes.createInstanceUsingBuilder('Bar', of('n', 3), of())
		
		val barList = newArrayList
		barList.add(bar1)
		barList.add(bar1)
		barList.add(bar1)
		barList.add(bar2)
		barList.add(bar2)
		barList.add(bar3)
		
		val res = func.invokeFunc(List, barList)
		assertEquals(3, res.size);
		assertThat(res, hasItems(bar1, bar2, bar3));
	}
	
	@Test
	def void funcWithListOfStringDistinctThenOnlyElement() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			type Foo:
				n string (0..*)
			
			func DistinctFunc:
				inputs:
					foo Foo (0..1)
				output:
					res string (0..1)
				assign-output res: foo -> n distinct only-element
			
		'''.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("DistinctFunc");
		val foo = classes.createInstanceUsingBuilder('Foo', of(), of('n', ImmutableList.of("1","1","1")))
		val res = func.invokeFunc(String, foo)
		assertEquals("1", res);
	}

	@Test
	def void funcWithListOfStringDistinctThenOnlyElement2() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func DistinctFunc:
				inputs:
					n string (0..*)
				output:
					res string (0..1)
				assign-output res: n distinct only-element
			
		'''.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("DistinctFunc");
		val res = func.invokeFunc(String,  ImmutableList.of("1","1","1"))
		assertEquals("1", res);
	}
	
	@Test
	def void funcWithListOfStringDistinctThenOnlyElement3() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func DistinctFunc:
				inputs:
					n string (0..*)
				output:
					res string (0..1)
				alias x:
					n distinct only-element
				assign-output res: 
					x
			
		'''.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("DistinctFunc");
		val res = func.invokeFunc(String,  ImmutableList.of("1","1","1"))
		assertEquals("1", res);
	}
	
	@Test
	def void funcWithListOfStringDistinctThenOnlyElement4() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			func DistinctFunc:
				inputs:
					n string (0..*)
				output:
					res string (0..1)
				alias x:
					n
				assign-output res: 
					x distinct only-element
			
		'''.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("DistinctFunc");
		val res = func.invokeFunc(String,  ImmutableList.of("1","1","1"))
		assertEquals("1", res);
	}

	@Test
	def void funcOnlyElementAnyMultiple() {
		'''
			namespace "demo"
			version "${project.version}"
			
			type Type1:
					t Type2 (1..1)
					ts Type2 (1..*)
			type Type2:
					num number (1..1)
					nums number (1..*)
			
			func Func1:
				inputs: t1 Type1(1..1)
				output: res number (1..1)
				assign-output res: t1->ts->num only-element
			
		'''.parseRosettaWithNoErrors.generateCode// .writeClasses("funcCallingMultipleFunc")
		.compileToClasses
	}

	@Test
	def void funcOnlyElementOnlySingle() {
		val model = '''
			namespace "demo"
			version "${project.version}"
			
			type T1:
					t T2 (1..1)
					ts T2 (1..*)
			type T2:
					num number (1..1)
					nums number (1..*)
			
			func F1:
				inputs: t1 T1(1..1)
				output: res number (1..1)
				assign-output res: t1->t->num only-element
			
		'''.parseRosetta
		model.assertError(ROSETTA_FEATURE_CALL, null,
			"'only-element' can not be used for single cardinality expressions.")
	}

	@Test
	def void nestedIfElse() {
		val model = '''
			namespace "demo"
			version "${project.version}"
			
			func IfElseTest:
			inputs:
				s1 string (1..1)
				s2 string (1..1)
			output: result string (1..1)
			
			assign-output result:
				if s1 = "1"
					then if s2 = "a"
						then "result1a"
					else
						if s2 = "b"
							then "result1b"
				else
					"result1"
				else if s1 = "2" then
					if s2 = "a"
					then "result2a"
					else if s2 = "b"
					then "result2b"
					else "result2"
					  else
			"result"
		  '''.parseRosettaWithNoErrors
		model.generateCode// .writeClasses("nestedIfElse")
		.compileToClasses

	}

	@Test
	def void mathOperationInsideIfStatement() {
		val model = '''
			namespace "demo"
			version "${project.version}"
			
			func AddInsideIf:
				inputs:
					i1 int (1..1)
					i2 int (1..1)
					b boolean (1..1)
				output: result int (1..1)
				
				assign-output result:
					if b = True
					then i1 + i2
					else 0
		'''.parseRosettaWithNoErrors
		model.generateCode// .writeClasses("nestedIfElse")
		.compileToClasses

	}

	@Test
	def void assignOutputOnResolvedQuantity() {
		val model = '''
			namespace "demo"
			version "${project.version}"
			
			type Quantity:
				amount number (1..1)
				
			type PriceQuantity:
				[metadata key]
				quantity Quantity (0..*)
				    [metadata location]
				    
			type ResolvablePayoutQuantity:
				resolvedQuantity Quantity (0..1)
				[metadata address "pointsTo"=PriceQuantity->quantity]
			
			type Cashflow:
				payoutQuantity ResolvablePayoutQuantity (1..1)
			
			func InterestCashSettlementAmount:
				inputs:
					x number (1..1)
				output:
					cashflow Cashflow (1..1)

			assign-output cashflow -> payoutQuantity -> resolvedQuantity -> amount:
				 x

		'''.parseRosettaWithNoErrors
		model.generateCode
		//.writeClasses("assignOutputOnResolvedQuantity")
		.compileToClasses

	}
	
	@Test
	def void ifWithSingleStringType() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		test boolean (1..1)
			 		t1 string  (1..1)
			 		t2 string (1..1)
				output:
					result string (1..1)
				
				assign-output result:
					if test = True
					then t1
					else t2
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import java.util.Arrays;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param test 
					* @param t1 
					* @param t2 
					* @return result 
					*/
					public String evaluate(Boolean test, String t1, String t2) {
						
						String resultHolder = doEvaluate(test, t1, t2);
						String result = assignOutput(resultHolder, test, t1, t2);
						
						return result;
					}
					
					private String assignOutput(String result, Boolean test, String t1, String t2) {
						result = com.rosetta.model.lib.mapper.MapperUtils.fromBuiltInType(() -> {
						if (areEqual(MapperS.of(test), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()) {
							return MapperS.of(t1);
						}
						else {
							return MapperS.of(t2);
						}
						}).get();
						return result;
					}
				
					protected abstract String doEvaluate(Boolean test, String t1, String t2);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  String doEvaluate(Boolean test, String t1, String t2) {
							return null;
						}
					}
				}
			'''.toString,
			f
		)
		code.compileToClasses
	}

	@Test
	def void ifWithMultipleStringType() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		test boolean (1..1)
			 		t1 string  (1..*)
			 		t2 string (1..*)
				output:
					result string (1..*)
				
				assign-output result:
					if test = True
					then t1
					else t2
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param test 
					* @param t1 
					* @param t2 
					* @return result 
					*/
					public List<String> evaluate(Boolean test, List<String> t1, List<String> t2) {
						
						List<String> resultHolder = doEvaluate(test, t1, t2);
						List<String> result = assignOutput(resultHolder, test, t1, t2);
						
						return result;
					}
					
					private List<String> assignOutput(List<String> result, Boolean test, List<String> t1, List<String> t2) {
						result = com.rosetta.model.lib.mapper.MapperUtils.fromBuiltInType(() -> {
						if (areEqual(MapperS.of(test), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()) {
							return MapperC.of(t1);
						}
						else {
							return MapperC.of(t2);
						}
						}).getMulti();
						return result;
					}
				
					protected abstract List<String> doEvaluate(Boolean test, List<String> t1, List<String> t2);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<String> doEvaluate(Boolean test, List<String> t1, List<String> t2) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		code.compileToClasses
	}

	@Test
	def void ifWithSingleNumberType() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		test boolean (1..1)
			 		t1 number  (1..1)
			 		t2 number (1..1)
				output:
					result number (1..1)
				
				assign-output result:
					if test = True
					then t1
					else t2
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import java.math.BigDecimal;
				import java.util.Arrays;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param test 
					* @param t1 
					* @param t2 
					* @return result 
					*/
					public BigDecimal evaluate(Boolean test, BigDecimal t1, BigDecimal t2) {
						
						BigDecimal resultHolder = doEvaluate(test, t1, t2);
						BigDecimal result = assignOutput(resultHolder, test, t1, t2);
						
						return result;
					}
					
					private BigDecimal assignOutput(BigDecimal result, Boolean test, BigDecimal t1, BigDecimal t2) {
						result = com.rosetta.model.lib.mapper.MapperUtils.fromBuiltInType(() -> {
						if (areEqual(MapperS.of(test), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()) {
							return MapperS.of(t1);
						}
						else {
							return MapperS.of(t2);
						}
						}).get();
						return result;
					}
				
					protected abstract BigDecimal doEvaluate(Boolean test, BigDecimal t1, BigDecimal t2);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  BigDecimal doEvaluate(Boolean test, BigDecimal t1, BigDecimal t2) {
							return null;
						}
					}
				}
			'''.toString,
			f
		)
		code.compileToClasses
	}

	@Test
	def void ifWithMultipleNumberType() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		test boolean (1..1)
			 		t1 number  (1..*)
			 		t2 number (1..*)
				output:
					result number (1..*)
				
				assign-output result:
					if test = True
					then t1
					else t2
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import java.math.BigDecimal;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param test 
					* @param t1 
					* @param t2 
					* @return result 
					*/
					public List<BigDecimal> evaluate(Boolean test, List<BigDecimal> t1, List<BigDecimal> t2) {
						
						List<BigDecimal> resultHolder = doEvaluate(test, t1, t2);
						List<BigDecimal> result = assignOutput(resultHolder, test, t1, t2);
						
						return result;
					}
					
					private List<BigDecimal> assignOutput(List<BigDecimal> result, Boolean test, List<BigDecimal> t1, List<BigDecimal> t2) {
						result = com.rosetta.model.lib.mapper.MapperUtils.fromBuiltInType(() -> {
						if (areEqual(MapperS.of(test), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()) {
							return MapperC.of(t1);
						}
						else {
							return MapperC.of(t2);
						}
						}).getMulti();
						return result;
					}
				
					protected abstract List<BigDecimal> doEvaluate(Boolean test, List<BigDecimal> t1, List<BigDecimal> t2);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<BigDecimal> doEvaluate(Boolean test, List<BigDecimal> t1, List<BigDecimal> t2) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		code.compileToClasses
	}
	
	@Test
	def void ifWithSingleDataType() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		test boolean (1..1)
			 		b1 Bar (1..1)
			 		b2 Bar (1..1)
				output:
					result Bar (1..1)
				
				assign-output result:
					if test = True
					then b1
					else b2
			
			type Bar:
				s1 string (1..1)
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Bar.BarBuilder;
				import java.util.Arrays;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param test 
					* @param b1 
					* @param b2 
					* @return result 
					*/
					public Bar evaluate(Boolean test, Bar b1, Bar b2) {
						
						Bar.BarBuilder resultHolder = doEvaluate(test, b1, b2);
						Bar.BarBuilder result = assignOutput(resultHolder, test, b1, b2);
						
						if (result!=null) objectValidator.validateAndFailOnErorr(Bar.class, result);
						return result;
					}
					
					private Bar.BarBuilder assignOutput(Bar.BarBuilder result, Boolean test, Bar b1, Bar b2) {
						result = toBuilder(com.rosetta.model.lib.mapper.MapperUtils.fromDataType(() -> {
						if (areEqual(MapperS.of(test), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()) {
							return MapperS.of(b1);
						}
						else {
							return MapperS.of(b2);
						}
						}).get())
						;
						return result;
					}
				
					protected abstract Bar.BarBuilder doEvaluate(Boolean test, Bar b1, Bar b2);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  Bar.BarBuilder doEvaluate(Boolean test, Bar b1, Bar b2) {
							return Bar.builder();
						}
					}
				}
			'''.toString,
			f
		)
		code.compileToClasses
	}

	@Test
	def void ifWithMultipleDataTypes() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		test boolean (1..1)
			 		b1 Bar (1..*)
			 		b2 Bar (1..*)
				output:
					result Bar (1..*)
				
				assign-output result:
					if test = True
					then b1
					else b2
			
			type Bar:
				s1 string (1..1)
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Bar.BarBuilder;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param test 
					* @param b1 
					* @param b2 
					* @return result 
					*/
					public List<? extends Bar> evaluate(Boolean test, List<? extends Bar> b1, List<? extends Bar> b2) {
						
						List<Bar.BarBuilder> resultHolder = doEvaluate(test, b1, b2);
						List<Bar.BarBuilder> result = assignOutput(resultHolder, test, b1, b2);
						
						if (result!=null) objectValidator.validateAndFailOnErorr(Bar.class, result);
						return result;
					}
					
					private List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> result, Boolean test, List<? extends Bar> b1, List<? extends Bar> b2) {
						result = toBuilder(com.rosetta.model.lib.mapper.MapperUtils.fromDataType(() -> {
						if (areEqual(MapperS.of(test), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()) {
							return MapperC.of(b1);
						}
						else {
							return MapperC.of(b2);
						}
						}).getMulti())
						;
						return result;
					}
				
					protected abstract List<Bar.BarBuilder> doEvaluate(Boolean test, List<? extends Bar> b1, List<? extends Bar> b2);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Bar.BarBuilder> doEvaluate(Boolean test, List<? extends Bar> b1, List<? extends Bar> b2) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		code.compileToClasses
	}
	
	@Test
	def void shouldSetList() {
		val model = '''
			type Foo:
				outList string (0..*)
			
			func FuncFoo:
			 	inputs:
			 		inList string (0..*)
				output:
					foo Foo (1..1)
				
				set foo -> outList:
					inList
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.Foo.FooBuilder;
				import java.util.Arrays;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param inList 
					* @return foo 
					*/
					public Foo evaluate(List<String> inList) {
						
						Foo.FooBuilder fooHolder = doEvaluate(inList);
						Foo.FooBuilder foo = assignOutput(fooHolder, inList);
						
						if (foo!=null) objectValidator.validateAndFailOnErorr(Foo.class, foo);
						return foo;
					}
					
					private Foo.FooBuilder assignOutput(Foo.FooBuilder foo, List<String> inList) {
						foo
							.setOutList(MapperC.of(inList).getMulti())
						;
						return foo;
					}
				
					protected abstract Foo.FooBuilder doEvaluate(List<String> inList);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  Foo.FooBuilder doEvaluate(List<String> inList) {
							return Foo.builder();
						}
					}
				}
			'''.toString,
			f
		)
		code.compileToClasses
	}
	
	@Test
	def void shouldSetList2() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		n1 number (0..1)
			 		n2 number (0..1)
			 		n3 number (0..1)
				output:
					result boolean (0..1)
				
				assign-output result:
					if n1 exists and n2 exists and n3 exists
					then n1 + n2 = n3
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.Foo.FooBuilder;
				import java.util.Arrays;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param inList 
					* @return foo 
					*/
					public Foo evaluate(List<String> inList) {
						
						Foo.FooBuilder fooHolder = doEvaluate(inList);
						Foo.FooBuilder foo = assignOutput(fooHolder, inList);
						
						if (foo!=null) objectValidator.validateAndFailOnErorr(Foo.class, foo);
						return foo;
					}
					
					private Foo.FooBuilder assignOutput(Foo.FooBuilder foo, List<String> inList) {
						foo
							.setOutList(MapperC.of(inList).getMulti())
						;
						return foo;
					}
				
					protected abstract Foo.FooBuilder doEvaluate(List<String> inList);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  Foo.FooBuilder doEvaluate(List<String> inList) {
							return Foo.builder();
						}
					}
				}
			'''.toString,
			f
		)
		code.compileToClasses
	}
	
}
