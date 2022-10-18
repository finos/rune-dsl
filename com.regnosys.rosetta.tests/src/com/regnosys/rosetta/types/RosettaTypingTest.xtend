package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.types.RBuiltinType.*
import com.regnosys.rosetta.tests.util.ModelHelper
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.expression.LogicalOperation
import com.regnosys.rosetta.rosetta.expression.MapOperation
import com.regnosys.rosetta.rosetta.expression.InlineFunction

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaTypingTest {	
	@Inject
	extension TypeFactory
	
	@Inject
	extension TypeTestUtil
	
	@Inject
	extension ExpressionValidationHelper
	
	@Inject
	extension ModelHelper
	
	@Test
	def void testLiteralTypeInference() {
		'False'.assertIsValidWithType(singleBoolean)
		'"Some string"'.assertIsValidWithType(singleString)
		'3.14'.assertIsValidWithType(singleNumber)
		'1'.assertIsValidWithType(singleInt)
		'empty'.assertIsValidWithType(emptyNothing)
	}
	
	@Test
	def void testVariableTypeInference() {
		val model = '''
		namespace test
		
		func TestVar:
			inputs:
				a int (2..4)
				b boolean (1..1)
			output: result number (1..4)
			alias c: if b then 42 else -1/12
			add result:
				if b then a else c
		
		func TestImplicitVar:
			output: result int (3..3)
			add result:
				[1, 2, 3] map [item + 1]
		'''.parseRosettaWithNoIssues
		model.elements.get(0) as Function => [operations.head.expression as RosettaConditionalExpression => [
			^if.assertHasType(singleBoolean)
			ifthen.assertHasType(createListType(INT, 2, 4))
			elsethen.assertHasType(singleNumber)
		]];
		
		model.elements.get(1) as Function => [operations.head.expression as MapOperation => [
			(functionRef as InlineFunction).body as ArithmeticOperation => [
				left.assertHasType(singleInt)
			]
		]];
	}
	
	// TODO: test auxiliary functions
	@Test
	def void testSubtyping() {
		val t1 = createListType(INT, 1, 3);
		val t2 = createListType(NUMBER, 1, 5);
		t1.assertListSubtype(t2)
		
		val t3 = createListType(BOOLEAN, 1, 3);
		t1.assertNotListSubtype(t3);
		
		val t4 = createListType(INT, 1, 2);
		t1.assertNotListSubtype(t4);
	}
	
	@Test
	def void testLogicalOperationTypeInference() {
		'True or False'.assertIsValidWithType(singleBoolean)
		'True and False'.assertIsValidWithType(singleBoolean)
		
		// Test loosened version
		'(if False then True else [True, False]) and False'.assertIsValidWithType(singleBoolean)
	}
	
	@Test
	def void testLogicalOperationTypeChecking() {
		'1 or False'
			.parseExpression
			.assertError(null, "Expected type `boolean`, but got `int` instead.")
		'True or 3.14'
			.parseExpression
			.assertError(null, "Expected type `boolean`, but got `number` instead.")
	}
	
	@Test
	def void testEqualityOperationTypeInference() {
		'(if True then [1] else [2, 3]) = (if False then [4.0, 5] else [6.0, 7, 8])'
			.assertIsValidWithType(singleBoolean)
		'(if True then [1] else [2, 3]) <> (if False then [4.0, 5] else [6.0, 7, 8])'
			.assertIsValidWithType(singleBoolean)
		'[1, 3] all = 5.0'.assertIsValidWithType(singleBoolean)
		'empty all <> 5.0'.assertIsValidWithType(singleBoolean)
		'[1, 3] any = 5.0'.assertIsValidWithType(singleBoolean)
		
		// Test loosened version
        '[1, 3] any = (if False then 1 else [2, 3])'.assertIsValidWithType(singleBoolean)
	}
	
	@Test
	def void testEqualityOperationTypeChecking() {
		'1 = True'
			.parseExpression
			.assertError(null, "Types `int` and `boolean` are not comparable.")
		'empty = True'
			.parseExpression
			.assertError(null, "Cannot compare an empty value to a single `boolean`, as they cannot be of the same length. Perhaps you forgot to write `all` or `any` in front of the operator?")
		'[1, 2] = [3, 4, 5]'
			.parseExpression
			.assertError(null, "Cannot compare a list of `int`s with 2 items to a list of `int`s with 3 items, as they cannot be of the same length.")
		'[1, 2] <> [True, False, False]'
			.parseExpression
			.assertError(null, "Types `int` and `boolean` are not comparable.")
		
		'[1, 2] all = empty'
			.parseExpression
			.assertError(null, "Expected a single value, but got an empty value instead.")
		'empty any = empty'
			.parseExpression
			.assertError(null, "Expected a single value, but got an empty value instead.")
		'[1, 2] all = [1, 2]'
			.parseExpression
			.assertError(null, "Expected a single value, but got a list with 2 items instead.")
		'5 any <> [1, 2]'
			.parseExpression
			.assertError(null, "Expected a single value, but got a list with 2 items instead. Perhaps you meant to swap the left and right operands?")
		'[3.0] any <> 5'
			.parseExpression
			.assertError(null, "The cardinality operator `any` is redundant when comparing two single values.")
	}
	
	// TODO: test arithmetic and comparisons with dates/times/etc
	@Test
	def void testArithmeticOperationTypeInference() {
		'3 + 4'.assertIsValidWithType(singleInt)
		'3.0 + 4'.assertIsValidWithType(singleNumber)
		'3 + 4.0'.assertIsValidWithType(singleNumber)
		'3.0 + 4.0'.assertIsValidWithType(singleNumber)
		'"ab" + "cd"'.assertIsValidWithType(singleString)
		
		'3 - 4'.assertIsValidWithType(singleInt)
		'3 - 4.0'.assertIsValidWithType(singleNumber)
		
		'3 * 4'.assertIsValidWithType(singleInt)
		'3.0 * 4'.assertIsValidWithType(singleNumber)
		
		'3 / 4'.assertIsValidWithType(singleNumber)
		
		// Test loosened version
        '(if False then 2 else [3, 4]) + 5'.assertIsValidWithType(singleInt)
	}
	
	@Test
	def void testArithemticOperationTypeChecking() {
		'[1, 2] + 3'
			.parseExpression
			.assertError(null, "Expected a single value, but got a list with 2 items instead.")
		'empty - 3'
			.parseExpression
			.assertError(null, "Expected a single value, but got an empty value instead.")
		'1.5 * False'
			.parseExpression
			.assertError(null, "Expected type `number`, but got `boolean` instead.")
		'"ab" + 3'
			.parseExpression
			.assertError(null, "Expected arguments to be either both a `string` or both a `number`, but got `string` and `int` instead.")
	}
	
	@Test
	def void testComparisonOperationTypeInference() {
		'1 < 2'.assertIsValidWithType(singleBoolean)
		'3 > 3.14'.assertIsValidWithType(singleBoolean)
		'-5.1 <= 42'.assertIsValidWithType(singleBoolean)
		'-3.14 >= 3.14'.assertIsValidWithType(singleBoolean)
		
		'[1, 2] any < 5'.assertIsValidWithType(singleBoolean)
		'empty all > 5'.assertIsValidWithType(singleBoolean)
		
		// Test loosened version
        '(if False then 2 else [3, 4]) < 5'.assertIsValidWithType(singleBoolean)
        '[1, 2] any < (if False then 5 else [3, 4])'.assertIsValidWithType(singleBoolean)
	}
	
	@Test
	def void testComparisonOperationTypeChecking() {
		// TODO: support date, zonedDateTime and `time`?
		'[1, 2] < 3'
			.parseExpression
			.assertError(null, "Expected a single value, but got a list with 2 items instead.")
		'empty > 3'
			.parseExpression
			.assertError(null, "Expected a single value, but got an empty value instead.")
		'1.5 <= False'
			.parseExpression
			.assertError(null, "Expected type `number`, but got `boolean` instead.")
			
		'[1, 2] all >= empty'
			.parseExpression
			.assertError(null, "Expected a single value, but got an empty value instead.")
		'empty any < empty'
			.parseExpression
			.assertError(null, "Expected a single value, but got an empty value instead.")
		'[1, 2] all > [1, 2]'
			.parseExpression
			.assertError(null, "Expected a single value, but got a list with 2 items instead.")
		'5 any <= [1, 2]'
			.parseExpression
			.assertError(null, "Expected a single value, but got a list with 2 items instead. Perhaps you meant to swap the left and right operands?")
		'5 all >= 1'
			.parseExpression
			.assertError(null, "The cardinality operator `all` is redundant when comparing two single values.")
	}
	
	@Test
	def void testConditionalExpressionTypeInference() {
		 'if True then [1, 2] else [3.0, 4.0, 5.0, 6.0]'.assertIsValidWithType(createListType(NUMBER, 2, 4));
	}
	
	@Test
	def void testConditionalExpressionTypeChecking() {
		'if [True, False] then 1 else 2'
			.parseExpression
			.assertError(null, "Expected a single value, but got a list with 2 items instead.")
		'if empty then 1 else 2'
			.parseExpression
			.assertError(null, "Expected a single `boolean`, but got an empty value instead.")
		'if True then 1 else False'
			.parseExpression
			.assertError(null, "Types `int` and `boolean` do not have a common supertype.")
		'if True then [1, 2, 3] else [False, True]'
			.parseExpression
			.assertError(null, "Types `int` and `boolean` do not have a common supertype.")
	}
	
	@Test
	def void testListLiteralTypeInference() {
		'[]'.assertIsValidWithType(emptyNothing);
		'[2, 4.5, 7, -3.14]'.assertIsValidWithType(createListType(NUMBER, 4, 4));
		'[2, [1, 2], -3.14]'.assertIsValidWithType(createListType(NUMBER, 4, 4));
	}
	
	@Test
	def void testListLiteralTypeChecking() {
		'[1, True]'
			.parseExpression
			.assertError(null, "Elements do not have a common supertype: `int`, `boolean`.")
	}
	
	@Test
	def void testFunctionCallTypeInference() {
		val model = '''
		namespace test
		
		func SomeFunc:
			inputs:
				a int (1..1)
				b boolean (2..4)
			output: result number (3..5)
			add result:
				[1.0, 2.0, 3.0]
		
		func Test:
			output: result number (3..5)
			add result:
				SomeFunc(42, [True, False, True])
		'''.parseRosettaWithNoIssues
		val expression = (model.elements.last as Function).operations.head.expression;
		expression.assertHasType(createListType(NUMBER, 3, 5));
	}
	
	@Test
	def void testFunctionCallTypeChecking() {
		val model = '''
		namespace test
		
		func SomeFunc:
			inputs:
			    a int (1..1)
			    b boolean (2..4)
			output: result int (1..1)
			set result:
				42
		
		func TestParamNumber:
			output: result int (1..1)
			set result:
				SomeFunc(1, [False, True], True)
		
		func TestParamType:
		    output: result int (1..1)
		    set result:
		        SomeFunc(1, [2, 3])
		
		func TestParamCardinality:
		    output: result int (1..1)
		    set result:
		        SomeFunc(1, [False, True, False, False, True])
		'''.parseRosetta
		
		val expr1 = (model.elements.get(1) as Function).operations.head.expression;
		expr1.assertError(null, "Expected 2 arguments, but got 3 instead.");
		
		val expr2 = (model.elements.get(2) as Function).operations.head.expression;
		expr2.assertError(null, "Expected type `boolean`, but got `int` instead.");
		
		val expr3 = (model.elements.get(3) as Function).operations.head.expression;
		expr3.assertError(null, "Expected a list with 2 to 4 items, but got a list with 5 items instead.");
	}
	
	@Test
	def void testProjectionTypeInference() {
		val model = '''
		namespace test
		
		type A:
			x int (1..1)
			y number (0..*)
			z boolean (3..7)
		
		func Test1:
			inputs:
			  a A (1..1)
			output: result int (1..1)
			set result:
				a -> x
		
		func Test2:
			inputs:
			  a A (1..1)
			output: result number (0..*)
			add result:
				a -> y
		
		func Test3:
			inputs:
			  a A (1..1)
			output: result boolean (3..7)
			add result:
				a -> z
		
		func Test4:
			inputs:
			  a A (2..5)
			output: result int (2..5)
			add result:
				a -> x
		
		func Test5:
			inputs:
			  a A (2..5)
			output: result number (0..*)
			add result:
				a -> y
		
		func Test6:
			inputs:
			  a A (2..5)
			output: result boolean (6..35)
			add result:
				a -> z
		'''.parseRosettaWithNoIssues		
		val expr1 = (model.elements.get(1) as Function).operations.head.expression;
		expr1.assertHasType(createListType(INT, 1, 1));
		
		val expr2 = (model.elements.get(2) as Function).operations.head.expression;
		expr2.assertHasType(createListType(NUMBER, 0));
		
		val expr3 = (model.elements.get(3) as Function).operations.head.expression;
		expr3.assertHasType(createListType(BOOLEAN, 3, 7));
		
		val expr4 = (model.elements.get(4) as Function).operations.head.expression;
		expr4.assertHasType(createListType(INT, 2, 5));
		
		val expr5 = (model.elements.get(5) as Function).operations.head.expression;
		expr5.assertHasType(createListType(NUMBER, 0));
		
		val expr6 = (model.elements.get(6) as Function).operations.head.expression;
		expr6.assertHasType(createListType(BOOLEAN, 6, 35));
	}
	
	@Test
	def void testEnumTypeInference() {
		val model = '''
		namespace test
		
		enum A:
			V1
			V2
		
		func Test:
			output: result A (1..1)
			set result:
				A -> V1
		'''.parseRosettaWithNoIssues
		val A = new REnumType(model.elements.get(0) as RosettaEnumeration);
		val expr1 = (model.elements.get(1) as Function).operations.head.expression;
		expr1.assertHasType(createListType(A, 1, 1));
	}
	
	@Test
	def void testExistsTypeInference() {
		'(if True then [] else 5) exists'.assertIsValidWithType(singleBoolean);
		'(if True then [] else [1, 2, 3]) exists'.assertIsValidWithType(singleBoolean);
	}
	
	@Test
	def void testExistsTypeChecking() {
		'empty exists'
			.parseExpression
			.assertError(null, "Expected an optional value, but got an empty value instead.")
		'42 exists'
			.parseExpression
			.assertError(null, "Expected an optional value, but got a single value instead.")
		'(if True then 42 else [1, 2, 3, 4, 5]) exists'
			.parseExpression
			.assertError(null, "Expected an optional value, but got a list with 1 to 5 items instead.")
	}

	@Test
	def void testAbsentTypeInference() {
		'(if True then [] else 5) is absent'.assertIsValidWithType(singleBoolean);
		'(if True then [] else [1, 2, 3]) is absent'.assertIsValidWithType(singleBoolean);
	}
	
	@Test
	def void testAbsentTypeChecking() {
		'empty is absent'
			.parseExpression
			.assertError(null, "Expected an optional value, but got an empty value instead.")
		'42 is absent'
			.parseExpression
			.assertError(null, "Expected an optional value, but got a single value instead.")
		'(if True then 42 else [1, 2, 3, 4, 5]) is absent'
			.parseExpression
			.assertError(null, "Expected an optional value, but got a list with 1 to 5 items instead.")
	}
	
	@Test
	def void testCountTypeInference() {
		'empty count'.assertIsValidWithType(singleInt);
		'42 count'.assertIsValidWithType(singleInt);
		'[1, 2, 3] count'.assertIsValidWithType(singleInt);
		'(if True then empty else [1, 2, 3]) count'.assertIsValidWithType(singleInt);
	}
	
	@Test
	def void testOnlyExistsTypeInference() {
		val model = '''
		namespace test
		
		type A:
			x int (0..1)
			y number (0..3)
			z boolean (0..*)
			
			condition C:
				x only exists and (x, y) only exists
		
		func Test:
			inputs:
			    a A (1..1)
			output: result boolean (1..1)
			set result:
				a -> x only exists and (a -> x, a -> y) only exists
		'''.parseRosettaWithNoIssues;
		
		(model.elements.get(0) as Data).conditions.head.expression as LogicalOperation => [
			left.assertHasType(singleBoolean)
			right.assertHasType(singleBoolean)
		]
		(model.elements.get(1) as Function).operations.head.expression as LogicalOperation => [
			left.assertHasType(singleBoolean)
			right.assertHasType(singleBoolean)
		]
	}
	
	@Test
	def void testOnlyExistsTypeChecking() {
		val model = '''
		namespace test
		
		type Foo:
			bar int (1..1)
			baz boolean (0..1)
			
			condition X:
				baz only exists
		
		type A:
			x Foo (0..1)
			y number (0..3)
			z boolean (0..*)
			
			condition C1:
				(x -> baz, y) only exists and (y, x -> baz) only exists
			condition C2:
				(x, x) only exists and (x -> baz, x -> baz) only exists
		
		func Test:
			inputs:
			    a A (1..1)
			    foo Foo (1..1)
			    b A (2..3)
			    c A (0..1)
			output: result boolean (0..*)
			add result:
				b -> x only exists
			add result:
				c only exists
			add result:
				(a -> x -> baz, a -> x) only exists
			add result:
				foo -> baz only exists
		'''.parseRosetta;
		
		(model.elements.get(0) as Data).conditions.head.expression
			.assertError(null, "The `only exists` operator is not applicable to instances of `Foo`.");
		
		(model.elements.get(1) as Data).conditions => [
			get(0).expression as LogicalOperation => [
				left.assertError(null, "All parent paths must be equal.")
				right.assertError(null, "All parent paths must be equal.")
			]
			get(1).expression as LogicalOperation => [
				left.assertError(null, "Duplicate attribute.")
				right.assertError(null, "Duplicate attribute.")
			]
		]
		
		(model.elements.get(2) as Function).operations => [
			get(0).expression.assertError(null, "Expected a single value, but got a list with 2 to 3 items instead.")
			get(1).expression.assertError(null, "Object must have a parent object.")
			get(2).expression.assertError(null, "All parent paths must be equal.")
			get(3).expression.assertError(null, "The `only exists` operator is not applicable to instances of `Foo`.")
		]
	}
	
	@Test
	def void testOnlyElementTypeInference() {
		'(if True then 0 else [1, 2]) only-element'.assertIsValidWithType(createListType(INT, 0, 1));
		'(if True then empty else [True, False]) only-element'.assertIsValidWithType(createListType(BOOLEAN, 0, 1));
		'(if True then 0 else [1, 2, 3, 42.0]) only-element'.assertIsValidWithType(createListType(NUMBER, 0, 1));
	}
	
	@Test
	def void testOnlyElementTypeChecking() {
		'empty only-element'
			.parseExpression
			.assertWarning(null, "Expected a list with 1 to 2 items, but got an empty value instead.")
		'42 only-element'
			.parseExpression
			.assertWarning(null, "Expected a list with 1 to 2 items, but got a single value instead.")
		'[1, 2] only-element'
			.parseExpression
			.assertWarning(null, "Expected a list with 1 to 2 items, but got a list with 2 items instead.")
		'(if True then empty else 42) only-element'
			.parseExpression
			.assertWarning(null, "Expected a list with 1 to 2 items, but got an optional value instead.")
	}
}
