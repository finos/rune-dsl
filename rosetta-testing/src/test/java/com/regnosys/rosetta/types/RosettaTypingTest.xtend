package com.regnosys.rosetta.types

import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import com.regnosys.rosetta.tests.util.ModelHelper
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.expression.LogicalOperation
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import java.util.Optional
import java.math.BigDecimal
import com.regnosys.rosetta.tests.util.ExpressionValidationHelper
import com.regnosys.rosetta.tests.util.ExpressionParser
import javax.inject.Inject
import java.math.BigInteger

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaTypingTest {	
	@Inject
	extension TypeFactory
	
	@Inject
	extension TypeSystem
	
	@Inject
	extension TypeTestUtil
	
	@Inject
	extension ExpressionParser
	
	@Inject
	extension ExpressionValidationHelper
	
	@Inject
	extension ModelHelper
	
	@Inject
	extension RBuiltinTypeService
	
	@Inject
	extension RObjectFactory
	
	@Test
	def void testLiteralTypeInference() {
		'False'.assertIsValidWithType(singleBoolean)
		'"Some string"'.assertIsValidWithType(singleString(11, 11))
		'3.14'.assertIsValidWithType(singleNumber(3, 2, "3.14", "3.14"))
		'1'.assertIsValidWithType(singleInt(1, "1", "1"))
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
				[1, 2, 3] extract item + 1
		'''.parseRosettaWithNoIssues
		model.elements.get(0) as Function => [operations.head.expression as RosettaConditionalExpression => [
			^if.assertHasType(singleBoolean)
			ifthen.assertHasType(createListType(UNCONSTRAINED_INT, 2, 4))
			elsethen.assertHasType(singleUnconstrainedNumber)
		]];
	}
	
	// TODO: test auxiliary functions
	@Test
	def void testSubtyping() {
		val t1 = createListType(UNCONSTRAINED_INT, 1, 3);
		val t2 = createListType(UNCONSTRAINED_NUMBER, 1, 5);
		t1.assertListSubtype(t2)
		
		val t3 = createListType(BOOLEAN, 1, 3);
		t1.assertNotListSubtype(t3);
		
		val t4 = createListType(UNCONSTRAINED_INT, 1, 2);
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
		
		'a = 1'
			.parseExpression(#['a int (0..1)'])
			.assertNoIssues
			
		val model = '''
		namespace test
		
		type Foo:
			packageTransactionPriceNotation int (0..1)
			
			condition C:
				packageTransactionPriceNotation = 1
		'''.parseRosettaWithNoIssues
		val expression = (model.elements.last as Data).conditions.head.expression;
		expression.assertHasType(singleBoolean);
		
		// Test loosened version
        '[1, 3] any = (if False then 1 else [2, 3])'.assertIsValidWithType(singleBoolean)
	}
	
	@Test
	def void testEqualityOperationTypeChecking() {
		'1 = True'
			.parseExpression
			.assertError(null, "Types `int` and `boolean` are not comparable.")
	}
	
	// TODO: test arithmetic and comparisons with dates/times/etc
	@Test
	def void testArithmeticOperationTypeInference() {
		'3 + 4'.assertIsValidWithType(singleInt(Optional.empty, Optional.of(BigInteger.valueOf(7)), Optional.of(BigInteger.valueOf(7))))
		'3.0 + 4'.assertIsValidWithType(singleNumber(Optional.empty, Optional.of(1), Optional.of(new BigDecimal("7")), Optional.of(new BigDecimal("7")), Optional.empty))
		'3 + 4.0'.assertIsValidWithType(singleNumber(Optional.empty, Optional.of(1), Optional.of(new BigDecimal("7")), Optional.of(new BigDecimal("7")), Optional.empty))
		'3.0 + 4.0'.assertIsValidWithType(singleNumber(Optional.empty, Optional.of(1), Optional.of(new BigDecimal("7")), Optional.of(new BigDecimal("7")), Optional.empty))
		'"ab" + "cd"'.assertIsValidWithType(singleString(4, 4))
		
		'3 - 4'.assertIsValidWithType(singleInt(Optional.empty, Optional.of(BigInteger.valueOf(-1)), Optional.of(BigInteger.valueOf(-1))))
		'3 - 4.0'.assertIsValidWithType(singleNumber(Optional.empty, Optional.of(1), Optional.of(new BigDecimal("-1")), Optional.of(new BigDecimal("-1")), Optional.empty))
		
		'3 * 4'.assertIsValidWithType(singleInt(Optional.empty, Optional.of(BigInteger.valueOf(12)), Optional.of(BigInteger.valueOf(12))))
		'3.0 * 4'.assertIsValidWithType(singleNumber(Optional.empty, Optional.of(1), Optional.of(new BigDecimal("12")), Optional.of(new BigDecimal("12")), Optional.empty))
		
		'3 / 4'.assertIsValidWithType(singleUnconstrainedNumber)
	}
	
	@Test
	def void testArithemticOperationTypeChecking() {
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
		'1.5 <= False'
			.parseExpression
			.assertError(null, "Expected type `number`, but got `boolean` instead.")

		'5 all >= 1'
			.parseExpression
			.assertError(null, "The cardinality operator `all` is redundant when comparing two single values.")
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
		expression.assertHasType(createListType(UNCONSTRAINED_NUMBER, 3, 5));
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
		'''.parseRosetta
		
		val expr1 = (model.elements.get(1) as Function).operations.head.expression;
		expr1.assertError(null, "Expected 2 arguments, but got 3 instead.");
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
		expr1.assertHasType(createListType(UNCONSTRAINED_INT, 1, 1));
		
		val expr2 = (model.elements.get(2) as Function).operations.head.expression;
		expr2.assertHasType(createListType(UNCONSTRAINED_NUMBER, 0));
		
		val expr3 = (model.elements.get(3) as Function).operations.head.expression;
		expr3.assertHasType(createListType(BOOLEAN, 3, 7));
		
		val expr4 = (model.elements.get(4) as Function).operations.head.expression;
		expr4.assertHasType(createListType(UNCONSTRAINED_INT, 2, 5));
		
		val expr5 = (model.elements.get(5) as Function).operations.head.expression;
		expr5.assertHasType(createListType(UNCONSTRAINED_NUMBER, 0));
		
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
		val A = (model.elements.get(0) as RosettaEnumeration).buildREnumType;
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
		'42 exists'
			.parseExpression
			.assertError(null, "Expected an optional value, but got a single value instead.")
	}

	@Test
	def void testAbsentTypeInference() {
		'(if True then [] else 5) is absent'.assertIsValidWithType(singleBoolean);
		'(if True then [] else [1, 2, 3]) is absent'.assertIsValidWithType(singleBoolean);
	}
	
	@Test
	def void testAbsentTypeChecking() {
		'42 is absent'
			.parseExpression
			.assertError(null, "Expected an optional value, but got a single value instead.")
	}
	
	@Test
	def void testCountTypeInference() {
		val singlePositiveInt = singleInt(Optional.empty, Optional.of(BigInteger.ZERO), Optional.empty)
		'empty count'.assertIsValidWithType(singlePositiveInt);
		'42 count'.assertIsValidWithType(singlePositiveInt);
		'[1, 2, 3] count'.assertIsValidWithType(singlePositiveInt);
		'(if True then empty else [1, 2, 3]) count'.assertIsValidWithType(singlePositiveInt);
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
	def void testOnlyElementTypeChecking() {
		'42 only-element'
			.parseExpression
			.assertWarning(null, "Expected a list with 1 to 2 items, but got a single value instead.")
	}
	
	@Test
	def void testTypeAliasJoin() {
		val model = '''
		namespace test
		
		typeAlias maxNString(n int): string(minLength: 1, maxLength: n)
		typeAlias max3String: maxNString(n: 3)
		typeAlias max4String: maxNString(n: 4)
		
		func Test:
			inputs:
				s1 max3String (1..1)
				s2 max4String (1..1)
				s3 maxNString(n: 4) (1..1)
			output: result string (0..*)
			add result: if True then s1 else s2
			add result: if True then s2 else s2
			add result: if True then s2 else s3
		'''.parseRosettaWithNoIssues
		model.elements.last as Function => [
			val max4String = createListType(inputs.get(1).typeCall.typeCallToRType, single)
			val maxNString = createListType(inputs.get(2).typeCall.typeCallToRType, single)
			
			operations => [
				get(0).expression.assertHasType(maxNString)
				get(1).expression.assertHasType(max4String)
				get(2).expression.assertHasType(maxNString)
			]
		]
	}

	@Test
	def void shouldCoerceStringToParameterizedString() {
		'''
		namespace test
		
		func Test:
			inputs: str string (1..1)
			output: max3String string(minLength: 1, maxLength: 3) (1..1)
			set max3String: str
		'''.parseRosettaWithNoIssues
	}

	@Test
	def void shouldCoerceParameterizedStringToString() {
		'''
		namespace test
		
		func Test:
			inputs: max3String string(minLength: 1, maxLength: 3) (1..1)
			output: str string (1..1)
			set str: max3String
		'''.parseRosettaWithNoIssues
	}

	@Test
	def void shouldCoerceStringToStringTypeAlias() {
		'''
		namespace test
		
		typeAlias Max3String: string(minLength: 1, maxLength: 3)
		
		func Test:
			inputs: str string (1..1)
			output: max3String Max3String (1..1)
			set max3String: str
		'''.parseRosettaWithNoIssues
	}

	@Test
	def void shouldCoerceStringTypeAliasToString() {
		'''
		namespace test
		
		typeAlias Max3String: string(minLength: 1, maxLength: 3)
		
		func Test:
			inputs: max3String Max3String (1..1)
			output: str string (1..1)
			set str: max3String
		'''.parseRosettaWithNoIssues
	}
	
	@Test
	def void shouldCoerceDifferentParameterizedStrings() {
		'''
		namespace test
		
		func Test:
			inputs: max10String string(minLength: 1, maxLength: 10) (1..1)
			output: max3String string(minLength: 1, maxLength: 3) (1..1)
			set max3String: max10String
		'''.parseRosettaWithNoIssues
	}
	
	@Test
	def void shouldCoerceDifferentTypeAliases() {
		'''
		namespace test
		
		typeAlias Max10String: string(minLength: 1, maxLength: 10)
		typeAlias Max3String: string(minLength: 1, maxLength: 3)
		
		func Test:
			inputs: max10String Max10String (1..1)
			output: max3String Max3String (1..1)
			set max3String: max10String
		'''.parseRosettaWithNoIssues
	}

}
