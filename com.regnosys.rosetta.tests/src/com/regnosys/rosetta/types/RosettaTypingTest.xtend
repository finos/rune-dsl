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
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper

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
}
