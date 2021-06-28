package com.regnosys.rosetta.generator.java.qualify

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.qualify.QualifyResult
import java.math.BigDecimal
import java.util.List
import java.util.Map
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.core.Is.is

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaQualifyProductComparisonTest {
	
	@Inject extension CodeGeneratorTestHelper
	@Inject extension QualifyTestHelper
	
	Map<String, Class<?>> classes
	
	@BeforeEach
	def void setUp() {
		val code = '''
			isProduct root Foo;
			
			type NumberList:
				numbers number (1..*)
			
			type Foo:
				bar Bar (0..*)
				bar2 Bar (0..*)
				baz Baz (0..1)
			
			type Bar:
				before number (0..1)
				after number (0..1)
			
			type Baz:
				bazValue number (0..1)
				other number (0..1)
			
			func Qualify_FeatureCallEqualToLiteral:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> before = 5
			
			func Qualify_FeatureCallNotEqualToLiteral:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> before <> 5
			
			func Qualify_FeatureCallEqualToFeatureCall:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> before = foo -> bar -> after
			
			func Qualify_FeatureCallListEqualToFeatureCall:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> before = foo -> baz -> other

			func Qualify_FeatureCallNotEqualToFeatureCall:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> before <> foo -> bar -> after
			
			func Qualify_FeatureCallListNotEqualToFeatureCall:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> before <> foo -> baz -> other
			
			func Qualify_FeatureCallsEqualToLiteralOr:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> before = 5 or foo -> baz -> other = 5
			
			func Qualify_FeatureCallsEqualToLiteralAnd:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> before = 5 and foo -> bar -> after = 5
			
«««			TODO tests compilation only, add unit test
			func Qualify_MultipleOrFeatureCallsEqualToMultipleOrFeatureCalls:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				alias values : [foo -> bar -> before, foo -> baz -> other]
				assign-output is_product:
					values contains foo -> bar -> after
						or values contains foo -> baz -> bazValue
«««			TODO tests compilation only, add unit test
			func Qualify_MultipleAndFeatureCallsEqualToMultipleOrFeatureCalls:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
				//	(foo -> bar -> before and foo -> baz -> other) = (foo -> bar -> after and foo -> baz -> bazValue)
				[foo -> bar -> before,  foo -> baz -> other] = [foo -> bar -> after, foo -> baz -> bazValue]
«««			TODO tests compilation only, add unit test
			func Qualify_FeatureCallComparisonOr:
				[qualification Product]
				inputs: foo Foo(1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					(foo -> bar -> before = foo -> baz -> other) or (foo -> bar -> after = foo -> baz -> bazValue)
«««			TODO tests compilation only, add unit test
			func Qualify_FeatureCallComparisonAnd:
				[qualification Product]
				inputs: foo Foo(1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					(foo -> bar -> before = foo -> baz -> other) and (foo -> bar -> after = foo -> baz -> bazValue)
«««			TODO tests compilation only, add unit test
			func Qualify_MultipleOrFeatureCallEqualToLiteral:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					//		(foo -> bar -> before or foo -> bar -> after or foo -> baz -> other) = 5.0
					[foo -> bar -> before, foo -> bar -> after, foo -> baz -> other] contains 5.0
«««			TODO tests compilation only, add unit test
			func Qualify_MultipleAndFeatureCallEqualToLiteral:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					// (foo -> bar -> before and foo -> bar -> after and foo -> baz -> other) = 5.0
					[foo -> bar -> before, foo -> bar -> after, foo -> baz -> other] = 5.0
			
«««			TODO tests compilation only, add unit test
			func Qualify_AliasFeatureCallEqualToLiteral:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					AliasBefore(foo) -> numbers = 5
			
«««			TODO tests compilation only, add unit test
			func Qualify_AliasFeatureCallEqualToFeatureCall:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					AliasBefore(foo) = AliasAfter(foo)
			
«««			TODO tests compilation only, add unit test
			func Qualify_AliasFeatureCallsEqualToLiteralOr:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					AliasBefore(foo) -> numbers = 5 or  AliasOther(foo) -> numbers = 5
			
«««			TODO tests compilation only, add unit test
			func Qualify_AliasFeatureCallsEqualToLiteralAnd:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					AliasBefore(foo) -> numbers = 5 and AliasOther(foo) -> numbers = 5
			
«««			TODO tests compilation only, add unit test
			func Qualify_AliasMultipleOrFeatureCallsEqualToMultipleOrFeatureCalls:
				[qualification Product]
				inputs: foo Foo(1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					(AliasBefore(foo) -> numbers exists
					or AliasOther(foo) -> numbers exists) =
					(AliasAfter(foo) -> numbers contains foo -> baz -> bazValue)
			
«««			TODO tests compilation only, add unit test
			func Qualify_AliasMultipleOrs:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					AliasBeforeOrAfterOrOther(foo) -> numbers contains 5.0
			
«««			TODO tests compilation only, add unit test
			func Qualify_MultipleGreaterThanComparisonsWithOrAnd:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> before > 5 
						or ( foo -> baz -> other > 10 and foo -> bar -> after > 15 ) 
						or foo -> baz -> bazValue > 20
			
			func Qualify_FeatureCallGreatherThan:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> before > foo -> bar2 -> before
			
«««			Aliases
			
			func AliasBefore:
				inputs: foo Foo (1..1)
				output: result NumberList (1..1)
				assign-output result -> numbers: foo -> bar -> before
			
			func AliasAfter:
				inputs: foo Foo (1..1)
				output: result NumberList (1..1)
				assign-output result -> numbers : foo -> bar -> after
			
			func AliasOther:
				inputs: foo Foo (1..1)
				output: result NumberList (1..1)
				assign-output result -> numbers : foo -> baz -> other
			
			
			func AliasBeforeOrAfterOrOther:
				inputs: foo Foo (1..1)
				output: result NumberList (1..1)
				assign-output result -> numbers : [
					foo -> bar -> before,
					foo -> bar -> after,
					foo -> baz -> other
				]
			
			func AliasBeforeGroupByAfter:
				inputs: foo Foo(1..1)
				output: result NumberList (1..1)
				assign-output result -> numbers: [
						(foo -> bar group by after) -> before
				]

			'''.generateCode
		//code.writeClasses("QualifyEventsComparisonTest")
		classes = code.compileToClasses
	}
	
	@Test
	def shouldCompareEqualObjects() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(5)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance))))

		val results1 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results1, "FeatureCallEqualToLiteral", true)
		
		val results2 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results2, "FeatureCallEqualToFeatureCall", true)
		
		val results3 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results3, "FeatureCallsEqualToLiteralOr", true)
		
		val results4 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results4, "FeatureCallsEqualToLiteralAnd", true)
				
		val results5 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results5, "FeatureCallNotEqualToLiteral", false)
						
		val results6 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results6, "FeatureCallNotEqualToFeatureCall", false)
		
		val results7 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results7, "FeatureCallListEqualToFeatureCall", true)
		
		val results8 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results8, "FeatureCallListNotEqualToFeatureCall", false)
	}

	@Test
	def shouldCompareEqualObjectsWithMultipleCardinality() {
		val barInstance1 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val barInstance2 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(5)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance1, barInstance2))))
		
		val results1 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results1, "FeatureCallEqualToLiteral", true)
		
		val results2 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results2, "FeatureCallEqualToFeatureCall", true)
		
		val results3 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results3, "FeatureCallsEqualToLiteralOr", true)
		
		val results4 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results4, "FeatureCallsEqualToLiteralAnd", true)
		
		val results5 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results5, "FeatureCallNotEqualToLiteral", false)
				
		val results6 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results6, "FeatureCallNotEqualToFeatureCall", false)
				
		val results7 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results7, "FeatureCallListEqualToFeatureCall", true)
		
		val results8 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results8, "FeatureCallListNotEqualToFeatureCall", false)
	}
	
	@Test
	def shouldCompareObjectsWithZeroCardinality() {
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(5)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of()))
		
		val results1 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results1, "FeatureCallEqualToLiteral", false)
		
		val results2 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results2, "FeatureCallEqualToFeatureCall", false)
		
		val results3 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results3, "FeatureCallNotEqualToLiteral", true)
		
		val results4 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results4, "FeatureCallNotEqualToFeatureCall", true)
						
		val results5 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results5, "FeatureCallListEqualToFeatureCall", false)
		
		val results6 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results6, "FeatureCallListNotEqualToFeatureCall", true)
	}
	
	@Test
	def shouldCompareObjectsWithUnsetValues() {
		val barInstance1 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val barInstance2 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(5)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance1, barInstance2))))
		
		val results1 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results1, "FeatureCallListEqualToFeatureCall", false)
		
		val results2 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results2, "FeatureCallListNotEqualToFeatureCall", true)
	}
	
	@Test
	def shouldCompareUnequalObjects() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(4), 'after', BigDecimal.valueOf(5)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(10)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance))))

		val results1 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results1, "FeatureCallEqualToLiteral", false)
		
		val results2 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results2, "FeatureCallEqualToFeatureCall", false)
		
		val results3 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results3, "FeatureCallsEqualToLiteralOr", false)
		
		val results4 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results4, "FeatureCallsEqualToLiteralAnd", false)
				
		val results5 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results5, "FeatureCallNotEqualToLiteral", true)
								
		val results7 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results7, "FeatureCallListEqualToFeatureCall", false)
		
		val results8 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results8, "FeatureCallListNotEqualToFeatureCall", true)
	}
	
	@Test
	def shouldCompareUnequalObjectsWithMultipleCardinality() {
		val barInstance1 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(4), 'after', BigDecimal.valueOf(5)), of())
		val barInstance2 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(4), 'after', BigDecimal.valueOf(5)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(10)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance1, barInstance2))))
		
		val results1 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results1, "FeatureCallEqualToLiteral", false)
		
		val results2 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results2, "FeatureCallEqualToFeatureCall", false)
		
		val results3 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results3, "FeatureCallsEqualToLiteralOr", false)
		
		val results4 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results4, "FeatureCallsEqualToLiteralAnd", false)
		
		val results5 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results5, "FeatureCallNotEqualToLiteral", true)
		
		val results7 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results7, "FeatureCallListEqualToFeatureCall", false)
		
		val results8 = createUtilAndGetAllResults(fooInstance)
		assertQualify(results8, "FeatureCallListNotEqualToFeatureCall", true)
	}

	// Util methods
		
	def assertQualify(List<QualifyResult> results, String isQualifyName, boolean expectedSuccess) {
		val result = getQualifyResult(results, isQualifyName)
		assertThat('Expected result (' + expectedSuccess + ') but got ' + result, result.success, is(expectedSuccess))
		assertThat('Unexpected number of expressionDataRule results', result.expressionDataRuleResults.size, is(1))
	}
}