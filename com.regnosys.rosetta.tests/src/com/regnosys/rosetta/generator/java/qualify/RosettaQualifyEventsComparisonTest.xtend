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
			class Foo {
				bar Bar (0..*);
				bar2 Bar (0..*);
				baz Baz (0..1);
			}
			
			class Bar {
				before number (0..1);
				after number (0..1);
			}
			
			class Baz {
				bazValue number (0..1);
				other number (0..1);
			}
			
			isEvent FeatureCallEqualToLiteral
				Foo -> bar -> before = 5
			isEvent FeatureCallNotEqualToLiteral
				Foo -> bar -> before <> 5
			
			isEvent FeatureCallEqualToFeatureCall
				Foo -> bar -> before = Foo -> bar -> after
			isEvent FeatureCallListEqualToFeatureCall
				Foo -> bar -> before = Foo -> baz -> other
			isEvent FeatureCallNotEqualToFeatureCall
				Foo -> bar -> before <> Foo -> bar -> after
			isEvent FeatureCallListNotEqualToFeatureCall
				Foo -> bar -> before <> Foo -> baz -> other
			isEvent FeatureCallsEqualToLiteralOr
				Foo -> bar -> before = 5 or Foo -> baz -> other = 5
			
			isEvent FeatureCallsEqualToLiteralAnd
				Foo -> bar -> before = 5 and Foo -> bar -> after = 5
			
«««			TODO tests compilation only, add unit test
			isEvent MultipleOrFeatureCallsEqualToMultipleOrFeatureCalls
				( Foo -> bar -> before or Foo -> baz -> other ) = ( Foo -> bar -> after or Foo -> baz -> bazValue )
«««			TODO tests compilation only, add unit test
			isEvent MultipleAndFeatureCallsEqualToMultipleOrFeatureCalls
				( Foo -> bar -> before and Foo -> baz -> other ) = ( Foo -> bar -> after and Foo -> baz -> bazValue )
«««			TODO tests compilation only, add unit test
			isEvent FeatureCallComparisonOr
				( Foo -> bar -> before = Foo -> baz -> other ) or ( Foo -> bar -> after = Foo -> baz -> bazValue )
«««			TODO tests compilation only, add unit test
			isEvent FeatureCallComparisonAnd
				( Foo -> bar -> before = Foo -> baz -> other ) and ( Foo -> bar -> after = Foo -> baz -> bazValue )
«««			TODO tests compilation only, add unit test
			isEvent MultipleOrFeatureCallEqualToLiteral
				( Foo -> bar -> before or Foo -> bar -> after or Foo -> baz -> other) = 5.0
«««			TODO tests compilation only, add unit test
			isEvent MultipleAndFeatureCallEqualToLiteral
				( Foo -> bar -> before and Foo -> bar -> after and Foo -> baz -> other) = 5.0
			
«««			TODO tests compilation only, add unit test
			isEvent AliasFeatureCallEqualToLiteral
				aliasBefore = 5
			
«««			TODO tests compilation only, add unit test
			isEvent AliasFeatureCallEqualToFeatureCall
				aliasBefore = aliasAfter
«««			TODO tests compilation only, add unit test
			isEvent AliasFeatureCallsEqualToLiteralOr
				aliasBefore = 5 or aliasOther = 5
			
«««			TODO tests compilation only, add unit test
			isEvent AliasFeatureCallsEqualToLiteralAnd
				aliasBefore = 5 and aliasOther = 5
			
«««			TODO tests compilation only, add unit test
			isEvent AliasMultipleOrFeatureCallsEqualToMultipleOrFeatureCalls
				( aliasBefore or aliasOther ) = ( aliasAfter or Foo -> baz -> bazValue )
«««			TODO tests compilation only, add unit test
			isEvent AliasMultipleOrs
				aliasBeforeOrAfterOrOther = 5.0
			
«««			TODO tests compilation only, add unit test
			isEvent MultipleGreaterThanComparisonsWithOrAnd
				Foo -> bar -> before > 5 or ( Foo -> baz -> other > 10 and Foo -> bar -> after > 15 ) or Foo -> baz -> bazValue > 20
			
			isEvent FeatureCallGreatherThan
				Foo -> bar -> before > Foo -> bar2 -> before
			
«««			Group By
			
			isEvent GroupByFeatureCallGreaterThan
				(Foo -> bar group by after) -> before  > (Foo -> bar2 group by after) -> before 
			
			isEvent GroupByFeatureCallGreaterThanAlias
				aliasBeforeGroupByAfter > (Foo -> bar2 group by after) -> before
			
«««			Aliases
			
			alias aliasBefore
				Foo -> bar -> before
			
			alias aliasAfter
				Foo -> bar -> after
			
			alias aliasOther
				Foo -> baz -> other
			
			alias aliasBeforeOrAfterOrOther
				Foo -> bar -> before or Foo -> bar -> after or Foo -> baz -> other
			
			alias aliasBeforeGroupByAfter
				( Foo -> bar group by after ) -> before
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