package com.regnosys.rosetta.generator.java.calculation

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.simple.SimplePackage
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.regnosys.rosetta.validation.RosettaIssueCodes
import java.util.List
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
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
		].generateCode// .writeClasses("shouldGenerateFunctionWithAssignemtnAsReference")
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
		].generateCode// .writeClasses("shouldGenerateFunctionWithAssignmentAsMeta")
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
		].generateCode// .writeClasses("shouldGenerateFunctionWithConditionalAssignment")
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
		model.generateCode// .writeClasses("shouldReturnMultiple")
		.compileToClasses
	}

	@Test
	def void funcCallingMultipleFunc() {
		val model = '''
			namespace "demo"
			version "${project.version}"
			
			func F1:
				inputs: num number (1..1)
				output: nums number (1..*)
					
			func F2:
				inputs: nums number(1..*)
				output: str string (1..1)
				
			func F3:
				inputs: num number (1..1)
				output: str string (1..1)
				assign-output str: F2(F1(num))

			func F4:
					inputs: num number (1..*)
					output: str string (1..1)
					assign-output str: F2(num)
			'''.parseRosettaWithNoErrors
		model.generateCode
		//.writeClasses("funcCallingMultipleFunc")
		.compileToClasses

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
		model.generateCode
		//.writeClasses("funcCallingMultipleFuncWithAlias")
		.compileToClasses

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
		model.assertWarning(ROSETTA_BINARY_OPERATION, null, "Comparison operator = should specify 'all' or 'any' when comparing a list to a single value")
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
		assertTrue(func.invokeFunc(Boolean, "a", List.of("a", "a")))
		assertFalse(func.invokeFunc(Boolean, "a", List.of("a", "b")))
		assertFalse(func.invokeFunc(Boolean, "b", List.of("a", "a")))
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
		assertTrue(func.invokeFunc(Boolean, "a", List.of("a", "a")))
		assertTrue(func.invokeFunc(Boolean, "a", List.of("a", "b")))
		assertFalse(func.invokeFunc(Boolean, "b", List.of("a", "a")))
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
		assertTrue(func.invokeFunc(Boolean, 1, List.of(1, 1)))
		assertFalse(func.invokeFunc(Boolean, 1, List.of(1, 2)))
		assertFalse(func.invokeFunc(Boolean, 2, List.of(1, 1)))
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
		assertTrue(func.invokeFunc(Boolean, 1, List.of(1, 1)))
		assertTrue(func.invokeFunc(Boolean, 1, List.of(1, 2)))
		assertFalse(func.invokeFunc(Boolean, 2, List.of(1, 1)))
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
		assertFalse(func.invokeFunc(Boolean, "a", List.of("a", "a")))
		assertFalse(func.invokeFunc(Boolean, "a", List.of("a", "b")))
		assertTrue(func.invokeFunc(Boolean, "b", List.of("a", "a")))
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
		assertFalse(func.invokeFunc(Boolean, "a", List.of("a", "a")))
		assertTrue(func.invokeFunc(Boolean, "a", List.of("a", "b")))
		assertTrue(func.invokeFunc(Boolean, "b", List.of("a", "a")))
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
		assertFalse(func.invokeFunc(Boolean, 1, List.of(1, 1)))
		assertFalse(func.invokeFunc(Boolean, 1, List.of(1, 2)))
		assertTrue(func.invokeFunc(Boolean, 2, List.of(1, 1)))
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
		assertFalse(func.invokeFunc(Boolean, 1, List.of(1, 1)))
		assertTrue(func.invokeFunc(Boolean, 1, List.of(1, 2)))
		assertTrue(func.invokeFunc(Boolean, 2, List.of(1, 1)))
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
		assertTrue(func.invokeFunc(Boolean, 2, List.of(1, 1)))
		assertFalse(func.invokeFunc(Boolean, 2, List.of(1, 2)))
		assertFalse(func.invokeFunc(Boolean, 1, List.of(2, 2)))
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
		//assertTrue(func.invokeFunc(Boolean, 2, List.of(1, 1)))
		assertTrue(func.invokeFunc(Boolean, 2, List.of(1, 2)))
		//assertFalse(func.invokeFunc(Boolean, 1, List.of(2, 2)))
	}

	@Test
	def void funcOnlyElementAnyMultiple() {
		'''
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
				assign-output res: t1->ts->num only-element

			'''.parseRosettaWithNoErrors.generateCode
		//.writeClasses("funcCallingMultipleFunc")
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
		model.assertError(ROSETTA_FEATURE_CALL, null, "'only-element' can not be used for single cardinality expressions.")
	}
}
