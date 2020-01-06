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
class RosettaQualifyEventsComparisonTest {
	
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
			
			func FeatureCallEqualToLiteral:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before = 5
			
			func FeatureCallNotEqualToLiteral:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before <> 5
			
			func FeatureCallEqualToFeatureCall:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before = foo -> bar -> after
			
			func FeatureCallListEqualToFeatureCall:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before = foo -> baz -> other
			func FeatureCallNotEqualToFeatureCall:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before <> foo -> bar -> after
			
			func FeatureCallListNotEqualToFeatureCall:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before <> foo -> baz -> other
			
			func FeatureCallsEqualToLiteralOr:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before = 5 or foo -> baz -> other = 5
			
			func FeatureCallsEqualToLiteralAnd:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before = 5 and foo -> bar -> after = 5
						
«««			TODO tests compilation only, add unit test
			func MultipleOrFeatureCallsEqualToMultipleOrFeatureCalls:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				alias values : [foo -> bar -> before, foo -> baz -> other]
				assign-output is_event:
					values contains foo -> bar -> after
					or values contains foo -> baz -> bazValue
«««			TODO tests compilation only, add unit test
			func MultipleAndFeatureCallsEqualToMultipleOrFeatureCalls:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
				//	(foo -> bar -> before and foo -> baz -> other) = (foo -> bar -> after and foo -> baz -> bazValue)
				[foo -> bar -> before,  foo -> baz -> other] = [foo -> bar -> after, foo -> baz -> bazValue]
«««			TODO tests compilation only, add unit test
			func FeatureCallComparisonOr:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					(foo -> bar -> before = foo -> baz -> other) or (foo -> bar -> after = foo -> baz -> bazValue)
«««			TODO tests compilation only, add unit test
			func FeatureCallComparisonAnd:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					(foo -> bar -> before = foo -> baz -> other) and (foo -> bar -> after = foo -> baz -> bazValue)
«««			TODO tests compilation only, add unit test
			func MultipleOrFeatureCallEqualToLiteral:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					//		(foo -> bar -> before or foo -> bar -> after or foo -> baz -> other) = 5.0
					[foo -> bar -> before, foo -> bar -> after, foo -> baz -> other] contains 5.0
«««			TODO tests compilation only, add unit test
			func MultipleAndFeatureCallEqualToLiteral:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					// (foo -> bar -> before and foo -> bar -> after and foo -> baz -> other) = 5.0
					[foo -> bar -> before, foo -> bar -> after, foo -> baz -> other] = 5.0
			
«««			TODO tests compilation only, add unit test
			func AliasFeatureCallEqualToLiteral:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					AliasBefore(foo) -> numbers = 5
			
«««			TODO tests compilation only, add unit test
			func AliasFeatureCallEqualToFeatureCall:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					AliasBefore(foo) = AliasAfter(foo)
					
«««			TODO tests compilation only, add unit test
			func AliasFeatureCallsEqualToLiteralOr:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					AliasBefore(foo) -> numbers = 5 or  AliasOther(foo) -> numbers = 5
				
			
«««			TODO tests compilation only, add unit test
			func AliasFeatureCallsEqualToLiteralAnd:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					AliasBefore(foo) -> numbers = 5 and AliasOther(foo) -> numbers = 5
			
«««			TODO tests compilation only, add unit test
			func AliasMultipleOrFeatureCallsEqualToMultipleOrFeatureCalls:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					(AliasBefore(foo) -> numbers exists
					or AliasOther(foo) -> numbers exists) = 
					(AliasAfter(foo) -> numbers contains foo -> baz -> bazValue)
			
«««			TODO tests compilation only, add unit test
			func AliasMultipleOrs:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					AliasBeforeOrAfterOrOther(foo) -> numbers contains 5.0
			
«««			TODO tests compilation only, add unit test
			func MultipleGreaterThanComparisonsWithOrAnd:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before > 5 or ( foo -> baz -> other > 10 and foo -> bar -> after > 15 ) or foo -> baz -> bazValue > 20
			
			func FeatureCallGreatherThan:
				inputs: foo Foo(1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before > foo -> bar2 -> before
			
«««			Group By Deprecate it
			//isEvent GroupByFeatureCallGreaterThan
			//	(Foo -> bar group by after) -> before  > (Foo -> bar2 group by after) -> before 
			//
			//isEvent GroupByFeatureCallGreaterThanAlias
			//	aliasBeforeGroupByAfter > (Foo -> bar2 group by after) -> before
			
«««			Aliases
			
			func AliasBefore:
				inputs: foo Foo(1..1)
				output: result NumberList (1..1)
				assign-output result -> numbers : foo -> bar -> before
			
			func AliasAfter:
				inputs: foo Foo(1..1)
				output: result NumberList (1..1)
				assign-output result -> numbers : foo -> bar -> after
			
			func AliasOther:
				inputs: foo Foo(1..1)
				output: result NumberList (1..1)
				assign-output result -> numbers : foo -> baz -> other
			
			
			func AliasBeforeOrAfterOrOther:
				inputs: foo Foo(1..1)
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
		assertEvent(results1, "FeatureCallEqualToLiteral", true, 1)
		
		val results2 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results2, "FeatureCallEqualToFeatureCall", true, 1)
		
		val results3 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results3, "FeatureCallsEqualToLiteralOr", true, 1)
		
		val results4 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results4, "FeatureCallsEqualToLiteralAnd", true, 1)
				
		val results5 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results5, "FeatureCallNotEqualToLiteral", false, 1)
						
		val results6 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results6, "FeatureCallNotEqualToFeatureCall", false, 1)
		
		val results7 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results7, "FeatureCallListEqualToFeatureCall", true, 1)
		
		val results8 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results8, "FeatureCallListNotEqualToFeatureCall", false, 1)
	}

	@Test
	def shouldCompareEqualObjectsWithMultipleCardinality() {
		val barInstance1 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val barInstance2 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(5)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance1, barInstance2))))
		
		val results1 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results1, "FeatureCallEqualToLiteral", true, 1)
		
		val results2 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results2, "FeatureCallEqualToFeatureCall", true, 1)
		
		val results3 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results3, "FeatureCallsEqualToLiteralOr", true, 1)
		
		val results4 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results4, "FeatureCallsEqualToLiteralAnd", true, 1)
		
		val results5 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results5, "FeatureCallNotEqualToLiteral", false, 1)
				
		val results6 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results6, "FeatureCallNotEqualToFeatureCall", false, 1)
				
		val results7 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results7, "FeatureCallListEqualToFeatureCall", true, 1)
		
		val results8 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results8, "FeatureCallListNotEqualToFeatureCall", false, 1)
	}
	
	@Test
	def shouldCompareObjectsWithZeroCardinality() {
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(5)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of()))
		
		val results1 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results1, "FeatureCallEqualToLiteral", false, 1)
		
		val results2 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results2, "FeatureCallEqualToFeatureCall", false, 1)
		
		val results3 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results3, "FeatureCallNotEqualToLiteral", true, 1)
		
		val results4 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results4, "FeatureCallNotEqualToFeatureCall", true, 1)
						
		val results5 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results5, "FeatureCallListEqualToFeatureCall", false, 1)
		
		val results6 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results6, "FeatureCallListNotEqualToFeatureCall", true, 1)
	}
	
	@Test
	def shouldCompareObjectsWithUnsetValues() {
		val barInstance1 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val barInstance2 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(5)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance1, barInstance2))))
		
		val results1 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results1, "FeatureCallListEqualToFeatureCall", false, 1)
		
		val results2 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results2, "FeatureCallListNotEqualToFeatureCall", true, 1)
	}
	
	@Test
	def shouldCompareUnequalObjects() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(4), 'after', BigDecimal.valueOf(5)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(10)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance))))

		val results1 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results1, "FeatureCallEqualToLiteral", false, 1)
		
		val results2 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results2, "FeatureCallEqualToFeatureCall", false, 1)
		
		val results3 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results3, "FeatureCallsEqualToLiteralOr", false, 1)
		
		val results4 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results4, "FeatureCallsEqualToLiteralAnd", false, 1)
				
		val results5 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results5, "FeatureCallNotEqualToLiteral", true, 1)
								
		val results7 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results7, "FeatureCallListEqualToFeatureCall", false, 1)
		
		val results8 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results8, "FeatureCallListNotEqualToFeatureCall", true, 1)
	}
	
	@Test
	def shouldCompareUnequalObjectsWithMultipleCardinality() {
		val barInstance1 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(4), 'after', BigDecimal.valueOf(5)), of())
		val barInstance2 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(4), 'after', BigDecimal.valueOf(5)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(10)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance1, barInstance2))))
		
		val results1 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results1, "FeatureCallEqualToLiteral", false, 1)
		
		val results2 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results2, "FeatureCallEqualToFeatureCall", false, 1)
		
		val results3 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results3, "FeatureCallsEqualToLiteralOr", false, 1)
		
		val results4 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results4, "FeatureCallsEqualToLiteralAnd", false, 1)
		
		val results5 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results5, "FeatureCallNotEqualToLiteral", true, 1)
		
		val results7 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results7, "FeatureCallListEqualToFeatureCall", false, 1)
		
		val results8 = createUtilAndGetAllResults(fooInstance)
		assertEvent(results8, "FeatureCallListNotEqualToFeatureCall", true, 1)
	}

	// Util methods
		
	def assertEvent(List<QualifyResult> results, String isEventName, boolean expectedSuccess, int expressionDataRuleResultCount) {
		val result = getQualifyResult(results, isEventName)
		assertThat('Expected result (' + expectedSuccess + ') but got ' + result, result.success, is(expectedSuccess))
		assertThat('Unexpected number of expressionDataRule results', result.expressionDataRuleResults.size, is(expressionDataRuleResultCount))
	}
}