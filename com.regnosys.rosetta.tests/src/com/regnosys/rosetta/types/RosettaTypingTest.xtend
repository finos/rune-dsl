package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.types.RBuiltinType.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaTypingTest {	
	@Inject
	extension TypeFactory
	
	@Inject
	extension TypeTestUtil
	
	@Inject
	extension ExpressionValidationHelper
	
	@Test
	def void testLiteralTypeInference() {
		'False'.assertIsValidWithType(singleBoolean)
		'"Some string"'.assertIsValidWithType(singleString)
		'3.14'.assertIsValidWithType(singleNumber)
		'1'.assertIsValidWithType(singleInt)
		'empty'.assertIsValidWithType(emptyNothing)
	}
	
	@Test
	def void testSubtyping() {
		val t1 = createListType(INT, 1, 3);
		val t2 = createListType(NUMBER, 1, 5);
		t1.assertListSubtype(t2)
	}
	
	@Test
	def void testLogicalOperationTypeInference() {
		'True or False'.assertIsValidWithType(singleBoolean)
		'True and False'.assertIsValidWithType(singleBoolean)
	}
	
	@Test
	def void testLogicalOperationTypeChecking() {
		'1 or False'
			.parseExpression
			.assertError(null, "Expected list type `boolean (1..1)`, but was `int (1..1)`.")
		'True or 3.14'
			.parseExpression
			.assertError(null, "Expected list type `boolean (1..1)`, but was `number (1..1)`.")
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
		'[3.0] any <> 5'.assertIsValidWithType(singleBoolean)
	}
	
	@Test
	def void testEqualityOperationTypeChecking() {
		'1 = True'
			.parseExpression
			.assertError(null, "List types `int (1..1)` and `boolean (1..1)` are not comparable.")
		// TODO: write tests for list comparability + comparability with `all`/`any`
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
	}
	
	@Test
	def void testArithemticOperationTypeChecking() {
//		'[1, 2] + 3' TODO
//			.parseExpression
//			.assertError(null, "")
		'empty - 3'
			.parseExpression
			.assertError(null, "Expected constraint `(1..1)`, but was `(0..0)`.")
		'1.5 * False'
			.parseExpression
			.assertError(null, "Expected a subtype of `number`, but was `boolean`.")
		'"ab" + 3'
			.parseExpression
			.assertError(null, "Expected argument types to be either both `string` or both a subtype of `number`, but got `string` and `int` instead.")
	}
	
	@Test
	def void testComparisonOperationTypeInference() {
		'1 < 2'.assertIsValidWithType(singleBoolean)
		'3 > 3.14'.assertIsValidWithType(singleBoolean)
		'-5.1 <= 42'.assertIsValidWithType(singleBoolean)
		'-3.14 >= 3.14'.assertIsValidWithType(singleBoolean)
		
		// TODO: test `any` and `all`
	}
	
	@Test
	def void testComparisonOperationTypeChecking() {
		
	}
}
