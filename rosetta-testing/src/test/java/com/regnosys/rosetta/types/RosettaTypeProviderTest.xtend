package com.regnosys.rosetta.types

import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.ExpressionParser
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import java.util.List
import com.regnosys.rosetta.rosetta.expression.MapOperation
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation
import static extension com.regnosys.rosetta.types.RMetaAnnotatedType.*
import java.util.Optional
import java.math.BigInteger
import java.math.BigDecimal
import org.eclipse.xtext.serializer.ISerializer
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.expression.LogicalOperation
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
@TestInstance(Lifecycle.PER_CLASS)
class RosettaTypeProviderTest {

	@Inject extension RosettaTypeProvider
	@Inject extension CardinalityProvider
	@Inject extension ModelHelper modelHelper
	@Inject extension ValidationTestHelper
	@Inject extension ExpressionParser
	@Inject extension TypeFactory
	@Inject extension ISerializer
	@Inject extension RObjectFactory
	@Inject extension TypeSystem
	@Inject extension RBuiltinTypeService builtins
	
	RMetaAttribute SCHEME
	@BeforeAll
	def void setup() {
		SCHEME = new RMetaAttribute("scheme", UNCONSTRAINED_STRING, null)
	}
	
	private def void assertIsValidWithType(CharSequence expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti, List<RosettaModel> context, String... attributes) {
		assertIsValidWithType(expr.parseExpression(context, attributes), expectedType, expectedIsMulti)
	}
	private def void assertIsValidWithType(CharSequence expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti, List<RosettaModel> context) {
		assertIsValidWithType(expr.parseExpression(context), expectedType, expectedIsMulti)
	}
	private def void assertIsValidWithType(CharSequence expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti, String... attributes) {
		assertIsValidWithType(expr.parseExpression(attributes), expectedType, expectedIsMulti)
	}
	private def void assertIsValidWithType(CharSequence expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti) {
		assertIsValidWithType(expr, expectedType, expectedIsMulti)
	}
	private def void assertIsValidWithType(RosettaExpression expr, RMetaAnnotatedType expectedType, boolean expectedIsMulti) {
		expr.assertNoIssues
		val actual = expr.RMetaAnnotatedType
		
		assertEquals(expectedType, actual, "Expression: " + expr.serialize)
		if (expectedIsMulti) {
			assertTrue(expr.isMulti, "Expected multi cardinality. Expression: " + expr.serialize)
		} else {
			assertFalse(expr.isMulti, "Expected single cardinality. Expression: " + expr.serialize)
		}
	}
	
	@Test
	def void testLiteralTypeInference() {
		'False'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		'"Some string"'.assertIsValidWithType(stringWithNoMeta(11, 11), false)
		'3.14'.assertIsValidWithType(numberWithNoMeta(3, 2, "3.14", "3.14"), false)
		'1'.assertIsValidWithType(intWithNoMeta(1, "1", "1"), false)
		'empty'.assertIsValidWithType(NOTHING_WITH_NO_META, false)
	}
	
	@Test
	def void testVariableTypeInference() {
		val context = '''
		func TestVar:
			output: result number (1..4)
			alias c: if True then 42 else -1/12
			add result:
				c
		
		func TestImplicitVar:
			output: result int (3..3)
			add result:
				[1, 2, 3] extract item + 1
		'''.parseRosettaWithNoIssues
		'a'.assertIsValidWithType(UNCONSTRAINED_INT.withMeta(#[SCHEME]), true, #["a int (2..4) [metadata scheme]"])
		'b'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false, #["b boolean (1..1)"])
		context.elements.get(0) as Function => [operations.head.expression.assertIsValidWithType(UNCONSTRAINED_NUMBER_WITH_NO_META, false)]
		
		context.elements.get(1) as Function => [operations.head.expression as MapOperation => [
			function.body as ArithmeticOperation => [
				left.assertIsValidWithType(intWithNoMeta(1, "1", "3"), false)
			]
		]];
	}
	
	@Test
	def void testLogicalOperationTypeInference() {
		'True or False'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		'True and False'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
	}
	
	@Test
	def void testLogicalOperationTypeChecking() {
		'1 or False'
			.parseExpression
			.assertError(null, "Expected type `boolean`, but got `int` instead.")
		'True or 3.14'
			.parseExpression
			.assertError(null, "Expected type `boolean`, but got `number` instead.")
		'a or False'
			.parseExpression(#['a boolean (1..2)'])
			.assertError(null, "aaaaa")
	}
	
	@Test
	def void testEqualityOperationTypeInference() {
		'[2, 3] = [6.0, 7, 8]'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		'[2, 3] <> [6.0, 7, 8]'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		'[1, 3] all = 5.0'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		'empty all <> 5.0'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		'[1, 3] any = 5.0'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		
		'a = 1'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false, #['a int (0..1)'])
	}
	
	@Test
	def void testEqualityOperationTypeChecking() {
		'1 = True'
			.parseExpression
			.assertError(null, "Types `int` and `boolean` are not comparable.")
		'empty = True'
			.parseExpression
			.assertError(null, "Cannot compare an empty value to a single value, as they cannot be of the same length. Perhaps you forgot to write `all` or `any` in front of the operator?")
		'[1, 2] = [3, 4, 5]'
			.parseExpression
			.assertError(null, "Cannot compare a list with 2 items to a list with 3 items, as they cannot be of the same length.")
		'[1, 2] <> [True, False, False]'
			.parseExpression
			.assertError(null, "Types `int` and `boolean` are not comparable.")
		
		'1 = True'
			.parseExpression
			.assertError(null, "Types `int` and `boolean` are not comparable.")
		'[1, 3] any <> a'
			.parseExpression(#['a int (1..2)'])
			.assertError(null, "aaaaaaaaaa")
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
		'3 + 4'.assertIsValidWithType(intWithNoMeta(Optional.empty, Optional.of(BigInteger.valueOf(7)), Optional.of(BigInteger.valueOf(7))), false)
		'3.0 + 4'.assertIsValidWithType(numberWithNoMeta(Optional.empty, Optional.of(1), Optional.of(new BigDecimal("7")), Optional.of(new BigDecimal("7")), Optional.empty), false)
		'3 + 4.0'.assertIsValidWithType(numberWithNoMeta(Optional.empty, Optional.of(1), Optional.of(new BigDecimal("7")), Optional.of(new BigDecimal("7")), Optional.empty), false)
		'3.0 + 4.0'.assertIsValidWithType(numberWithNoMeta(Optional.empty, Optional.of(1), Optional.of(new BigDecimal("7")), Optional.of(new BigDecimal("7")), Optional.empty), false)
		'"ab" + "cd"'.assertIsValidWithType(stringWithNoMeta(4, 4), false)
		
		'3 - 4'.assertIsValidWithType(intWithNoMeta(Optional.empty, Optional.of(BigInteger.valueOf(-1)), Optional.of(BigInteger.valueOf(-1))), false)
		'3 - 4.0'.assertIsValidWithType(numberWithNoMeta(Optional.empty, Optional.of(1), Optional.of(new BigDecimal("-1")), Optional.of(new BigDecimal("-1")), Optional.empty), false)
		
		'3 * 4'.assertIsValidWithType(intWithNoMeta(Optional.empty, Optional.of(BigInteger.valueOf(12)), Optional.of(BigInteger.valueOf(12))), false)
		'3.0 * 4'.assertIsValidWithType(numberWithNoMeta(Optional.empty, Optional.of(1), Optional.of(new BigDecimal("12")), Optional.of(new BigDecimal("12")), Optional.empty), false)
		
		'3 / 4'.assertIsValidWithType(UNCONSTRAINED_NUMBER_WITH_NO_META, false)
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
		'a + 5'
			.parseExpression(#['a int (1..2)'])
			.assertError(null, "aaaaaaaaaaa")
	}
	
	@Test
	def void testComparisonOperationTypeInference() {
		'1 < 2'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		'3 > 3.14'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		'-5.1 <= 42'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		'-3.14 >= 3.14'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		
		'[1, 2] any < 5'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		'empty all > 5'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
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
		
		'a < 5'
			.parseExpression(#['a int (1..2)'])
			.assertError(null, "aaaaa")
		'[1, 2] any < a'
			.parseExpression(#['a int (1..2)'])
			.assertError(null, "aaaaa")
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
		 'if True then [1, 2] else [3.0, 4.0, 5.0, 6.0]'.assertIsValidWithType(numberWithNoMeta(2, 1, "1", "6"), true);
	}
	
	@Test
	def void testConditionalExpressionTypeChecking() {
		'if [True, False] then 1 else 2'
			.parseExpression
			.assertError(null, "Expected a single value, but got a list with 2 items instead.")
		'if empty then 1 else 2'
			.parseExpression
			.assertError(null, "Expected a single value, but got an empty value instead.")
		'if True then 1 else False'
			.parseExpression
			.assertError(null, "Types `int` and `boolean` do not have a common supertype.")
		'if True then [1, 2, 3] else [False, True]'
			.parseExpression
			.assertError(null, "Types `int` and `boolean` do not have a common supertype.")
	}
	
	@Test
	def void testListLiteralTypeInference() {
		'[]'.assertIsValidWithType(NOTHING_WITH_NO_META, false);
		'[2, 4.5, 7, -3.14]'.assertIsValidWithType(numberWithNoMeta(3, 2, "-3.14", "7"), true);
		'[2, [1, 2], -3.14]'.assertIsValidWithType(numberWithNoMeta(3, 2, "-3.14", "2"), true);
	}
	
	@Test
	def void testListLiteralTypeChecking() {
		'[1, True]'
			.parseExpression
			.assertError(null, "Elements do not have a common supertype: `int`, `boolean`.")
	}
	
	@Test
	def void testFunctionCallTypeInference() {
		val context = '''
		func SomeFunc:
			inputs:
				a int (1..1)
				b boolean (2..4)
			output: result number (3..5)
			add result:
				[1.0, 2.0, 3.0]
		'''.parseRosettaWithNoIssues
		'SomeFunc(42, [True, False, True])'.assertIsValidWithType(UNCONSTRAINED_NUMBER_WITH_NO_META, true, #[context])
	}
	
	@Test
	def void testFunctionCallTypeChecking() {
		val context = '''
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
		'''.parseRosettaWithNoIssues
		
		'SomeFunc(1, [False, True], True)'
			.parseExpression(#[context])
			.assertError(null, "Expected 2 arguments, but got 3 instead.");
		'SomeFunc(1, [2, 3])'
			.parseExpression(#[context])
			.assertError(null, "Expected type `boolean`, but got `int` instead.");
		'SomeFunc(1, [False, True, False, False, True])'
			.parseExpression(#[context])
			.assertError(null, "Expected a list with 2 to 4 items, but got a list with 5 items instead.");
	}
	
	@Test
	def void testProjectionTypeInference() {
		val context = '''
		namespace test
		
		type A:
			x int (1..1)
			y number (0..*)
			z boolean (3..7)
		'''.parseRosettaWithNoIssues
		'a -> x'.assertIsValidWithType(UNCONSTRAINED_INT_WITH_NO_META, false, #[context], #['a A (1..1)'])
		'a -> y'.assertIsValidWithType(UNCONSTRAINED_NUMBER_WITH_NO_META, true, #[context], #['a A (1..1)'])
		'a -> z'.assertIsValidWithType(BOOLEAN_WITH_NO_META, true, #[context], #['a A (1..1)'])
		'a -> x'.assertIsValidWithType(UNCONSTRAINED_INT_WITH_NO_META, true, #[context], #['a A (2..5)'])
		'a -> y'.assertIsValidWithType(UNCONSTRAINED_NUMBER_WITH_NO_META, true, #[context], #['a A (1..1)'])
		'a -> z'.assertIsValidWithType(BOOLEAN_WITH_NO_META, true, #[context], #['a A (1..1)'])
	}
	
	@Test
	def void testEnumTypeInference() {
		val context = '''
		namespace test
		
		enum A:
			V1
			V2
		
		func Test:
			output: result A (1..1)
			set result:
				A -> V1
		'''.parseRosettaWithNoIssues
		val A = (context.elements.get(0) as RosettaEnumeration).buildREnumType;
		'A -> V1'.assertIsValidWithType(A.withEmptyMeta, false, #[context])
	}
	
	@Test
	def void testExistsTypeInference() {
		'a exists'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false, #['a int (0..1)']);
		'a exists'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false, #['a int (0..3)']);
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
		'a is absent'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false, #['a int (0..1)']);
		'a is absent'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false, #['a int (0..3)']);
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
		val positiveInt = intWithNoMeta(Optional.empty, Optional.of(BigInteger.ZERO), Optional.empty)
		'empty count'.assertIsValidWithType(positiveInt, false);
		'42 count'.assertIsValidWithType(positiveInt, false);
		'[1, 2, 3] count'.assertIsValidWithType(positiveInt, false);
		'(if True then empty else [1, 2, 3]) count'.assertIsValidWithType(positiveInt, false);
	}
	
	@Test
	def void testOnlyExistsTypeInference() {
		val context = '''
		namespace test
		
		type A:
			x int (0..1)
			y number (0..3)
			z boolean (0..*)
			
			condition C:
				x only exists and (x, y) only exists
		'''.parseRosettaWithNoIssues;
		
		(context.elements.get(0) as Data).conditions.head.expression as LogicalOperation => [
			left.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
			right.assertIsValidWithType(BOOLEAN_WITH_NO_META, false)
		]
		'a -> x only exists'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false, #[context], #['a A (1..1)'])
		'(a -> x, a -> y) only exists'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false, #[context], #['a A (1..1)'])
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
		'(if True then 0 else [1, 2]) only-element'.assertIsValidWithType(intWithNoMeta(1, "0", "2"), false);
		'(if True then empty else [True, False]) only-element'.assertIsValidWithType(BOOLEAN_WITH_NO_META, false);
		'(if True then 0 else [1, 2, 3, 42.0]) only-element'.assertIsValidWithType(numberWithNoMeta(3, 1, "0", "42.0"), false);
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
			val max4String = inputs.get(1).typeCall.typeCallToRType.withEmptyMeta
			val maxNString = inputs.get(2).typeCall.typeCallToRType.withEmptyMeta
			
			operations => [
				get(0).expression.assertIsValidWithType(maxNString, false)
				get(1).expression.assertIsValidWithType(max4String, false)
				get(2).expression.assertIsValidWithType(maxNString, false)
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
	
	@Test
	def void testAttributeSameNameAsAnnotationTest() {
		val model =
		'''		
		type A:
			[rootType]
			rootType string (0..1)
		
			condition C:
				rootType exists
		'''.parseRosetta
		
		model.assertNoIssues
	}

	@Test
	def void testBinaryExpressionCommonType() {
		val funcs = '''
			isEvent root Foo;
			
			type Foo:
				iBar int (0..*)
				nBar number (0..*)
				nBuz number (0..*)
			
			func Qualify_AllNumber:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				set is_event:
					[foo -> nBar, foo -> nBuz] contains 4.0
			
			func Qualify_MixedNumber:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				set is_event:
					[foo -> nBar, foo -> iBar] contains 4.0
			
			func Qualify_IntOnly:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				set is_event:
					foo -> iBar = 4.0
		'''.parseRosettaWithNoErrors.elements.filter(Function)
		
		val allNumber = funcs.filter[name == "Qualify_AllNumber"].head
		assertEquals('number', (allNumber.operations.head.expression as RosettaContainsExpression).left.RMetaAnnotatedType.RType.name)
		val mixed = funcs.filter[name == "Qualify_MixedNumber"].head
		assertEquals('number', (mixed.operations.head.expression as RosettaContainsExpression).left.RMetaAnnotatedType.RType.name)
		val intOnly = funcs.filter[name == "Qualify_IntOnly"].head
		assertEquals('int', (intOnly.operations.head.expression as RosettaBinaryOperation).left.RMetaAnnotatedType.RType.name)
	}
}
