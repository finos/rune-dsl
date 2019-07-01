package com.regnosys.rosetta.generator.java.qualify

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.DataRuleHelper
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.validation.ValidationResult
import java.math.BigDecimal
import java.util.Map
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.core.Is.is
import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaQualifyEventsDataRuleTest {
	
	@Inject extension CodeGeneratorTestHelper
	@Inject extension DataRuleHelper
	@Inject extension QualifyTestHelper
	
	Map<String, Class<?>> classes
	
	@BeforeEach
	def void setUp() {
		val code = '''
			class Foo {
				bar Bar (0..*);
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
			
			data rule FeatureCallComparisonDecreasing
			when Foo -> bar exists
			then Foo -> bar -> before > Foo -> bar -> after
			
			data rule BarFeatureCallGreaterThanLiteralZero
			when Foo -> bar exists
			then Foo -> bar -> after > 0
			
			data rule BazFeatureCallGreaterThanLiteralFive
			when Foo -> baz exists
			then Foo -> baz -> other > 5
			
			data rule BazFeatureCallGreaterThanLiteralZero
			when Foo -> baz exists
			then Foo -> baz -> other > 0
			
			isEvent ExprAndDataRulesOrDataRules
				Foo -> baz -> other when present = 10
				and Foo -> bar -> before exists
				and Foo -> bar -> after exists
				and FeatureCallComparisonDecreasing, BarFeatureCallGreaterThanLiteralZero or BazFeatureCallGreaterThanLiteralFive, BazFeatureCallGreaterThanLiteralZero apply
			'''.generateCode
//		println(code)
		classes = code.compileToClasses
	}

	
	@Test
	def exprSuccess_allDataRulesSucccess_should_qualify() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(3)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(10)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance))))

		// Assert DataRules

		// FeatureCallComparisonDecreasing (success)
		assertDataRule(fooInstance, 'FeatureCallComparisonDecreasing', true, "when Foo -> bar exists\nthen Foo -> bar -> before > Foo -> bar -> after")		
		
		// BarFeatureCallGreaterThanLiteralZero (success)
		assertDataRule(fooInstance, 'BarFeatureCallGreaterThanLiteralZero', true, "when Foo -> bar exists\nthen Foo -> bar -> after > 0")		
		
		// BazFeatureCallGreaterThanLiteralZero (success)
		assertDataRule(fooInstance, 'BazFeatureCallGreaterThanLiteralZero', true, "when Foo -> baz exists\nthen Foo -> baz -> other > 0")
		
		// BazFeatureCallGreaterThanLiteralFive (success)
		assertDataRule(fooInstance, 'BazFeatureCallGreaterThanLiteralFive', true, "when Foo -> baz exists\nthen Foo -> baz -> other > 5")	
		
		// Assert Event
		
		val results = createUtilAndGetAllResults(fooInstance)
		val result = getQualifyResult(results, "ExprAndDataRulesOrDataRules")
		assertTrue(result.success, 'Unexpected success result')
		assertThat('Unexpected number of expressionDataRule results', result.expressionDataRuleResults.size, is(5))
	}

	@Test
	def exprSuccess_andDataRuleFail_orDataRulesSuccess_should_qualify() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(10), 'after', BigDecimal.valueOf(0)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(10)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance))))

		// Assert DataRules
		
		// FeatureCallComparisonDecreasing (success)
		assertDataRule(fooInstance, 'FeatureCallComparisonDecreasing', true, "when Foo -> bar exists\nthen Foo -> bar -> before > Foo -> bar -> after")		
		
		// BarFeatureCallGreaterThanLiteralZero (fail)
		assertDataRule(fooInstance, 'BarFeatureCallGreaterThanLiteralZero', false, "when Foo -> bar exists\nthen Foo -> bar -> after > 0")		
		
		// BazFeatureCallGreaterThanLiteralZero (success)
		assertDataRule(fooInstance, 'BazFeatureCallGreaterThanLiteralZero', true, "when Foo -> baz exists\nthen Foo -> baz -> other > 0")
				
		// BazFeatureCallGreaterThanLiteralFive (success)
		assertDataRule(fooInstance, 'BazFeatureCallGreaterThanLiteralFive', true, "when Foo -> baz exists\nthen Foo -> baz -> other > 5")	
						
		// Assert Event
		
		val results = createUtilAndGetAllResults(fooInstance)
		val result = getQualifyResult(results, "ExprAndDataRulesOrDataRules")
		assertTrue(result.success, 'Expected success result')
		assertThat('Unexpected number of expressionDataRule results', result.expressionDataRuleResults.size, is(5))
	}
	
	@Test
	def exprSuccess_andDataRuleFail_orDataRulesFail_should_not_qualify() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(-10), 'after', BigDecimal.valueOf(0)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(0)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance))))

		// Assert DataRules

		// FeatureCallComparisonDecreasing (fail)
		val dataRuleBarDescreasing = ValidationResult.cast(classes.runDataRule(fooInstance, 'FeatureCallComparisonDecreasing'))
		assertFalse(dataRuleBarDescreasing.success)
		assertThat(dataRuleBarDescreasing.definition, is("when Foo -> bar exists\nthen Foo -> bar -> before > Foo -> bar -> after"))
		assertThat(dataRuleBarDescreasing.failureReason.orElse(""), is("all elements of paths [Foo->getBar[0]->getBefore] values [-10] are not > than all elements of paths [Foo->getBar[0]->getAfter] values [0]"))

		// BarFeatureCallGreaterThanLiteralZero (fail)
		val dataRuleBarGreaterThanZero = ValidationResult.cast(classes.runDataRule(fooInstance, 'BarFeatureCallGreaterThanLiteralZero'))
		assertFalse(dataRuleBarGreaterThanZero.success)
		assertThat(dataRuleBarGreaterThanZero.getDefinition(), is("when Foo -> bar exists\nthen Foo -> bar -> after > 0"))
		assertThat(dataRuleBarGreaterThanZero.failureReason.orElse(""), is("all elements of paths [Foo->getBar[0]->getAfter] values [0] are not > than all elements of paths [Integer] values [0]"))
		
		// BazFeatureCallGreaterThanLiteralZero (fail)
		val dataRuleBazGreaterThanZero = ValidationResult.cast(classes.runDataRule(fooInstance, 'BazFeatureCallGreaterThanLiteralZero'))
		assertFalse(dataRuleBazGreaterThanZero.success)
		assertThat(dataRuleBazGreaterThanZero.definition, is("when Foo -> baz exists\nthen Foo -> baz -> other > 0"))
		assertThat(dataRuleBazGreaterThanZero.failureReason.orElse(""), is("all elements of paths [Foo->getBaz->getOther] values [0] are not > than all elements of paths [Integer] values [0]"))
		
		// BazFeatureCallGreaterThanLiteralFive (fail)
		val dataRuleBazGreaterThanFive = ValidationResult.cast(classes.runDataRule(fooInstance, 'BazFeatureCallGreaterThanLiteralFive'))
		assertFalse(dataRuleBazGreaterThanFive.success)
		assertThat(dataRuleBazGreaterThanFive.definition, is("when Foo -> baz exists\nthen Foo -> baz -> other > 5"))
		assertThat(dataRuleBazGreaterThanFive.failureReason.orElse(""), is("all elements of paths [Foo->getBaz->getOther] values [0] are not > than all elements of paths [Integer] values [5]"))
		
		// Assert Event
		
		val results = createUtilAndGetAllResults(fooInstance)
		val result = getQualifyResult(results, "ExprAndDataRulesOrDataRules")
		assertFalse(result.success, 'Unexpected success result')
		assertThat('Unexpected number of expressionDataRule results', result.expressionDataRuleResults.size, is(5))
	}

	// Util methods
		
	def assertDataRule(RosettaModelObject model, String dataRuleName, boolean expectedSuccess, String expectedDefinition) {
		val dataRuleResult = ValidationResult.cast(classes.runDataRule(model, dataRuleName))
		assertThat(dataRuleResult.success, is(expectedSuccess))	
		assertThat(dataRuleResult.definition, is(expectedDefinition))
	}
}