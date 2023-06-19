package com.regnosys.rosetta.generator.java.condition

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
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
class RosettaConditionTest {
	
	@Inject extension CodeGeneratorTestHelper
	@Inject extension ConditionTestHelper
	
	Map<String, Class<?>> classes
	
	@BeforeEach
	def void setUp() {
		val code = '''
			type Foo:
				bar Bar (0..*)
				baz Baz (0..1)
				
				condition FeatureCallComparisonDecreasing:
					if bar exists
					then bar -> before > bar -> after
				
				condition BarFeatureCallGreaterThanLiteralZero:
					if bar exists 
					then bar -> after > 0
							
				condition BazFeatureCallGreaterThanLiteralFive:
					if baz exists 
					then baz -> other > 5
							
				condition BazFeatureCallGreaterThanLiteralZero:
					if baz exists 
					then baz -> other > 0
			
			type Bar:
				before number (0..1)
				after number (0..1)
			
			type Baz:
				bazValue number (0..1)
				other number (0..1)
			'''.generateCode
//		println(code)
		classes = code.compileToClasses
	}

	
	@Test
	def allConditionsShouldSuceed() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(3)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(10)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance))))

		// FeatureCallComparisonDecreasing (success)
		assertCondition(fooInstance, 'FooFeatureCallComparisonDecreasing', true, "if bar exists then bar -> before > bar -> after")		
		
		// BarFeatureCallGreaterThanLiteralZero (success)
		assertCondition(fooInstance, 'FooBarFeatureCallGreaterThanLiteralZero', true, "if bar exists then bar -> after > 0")		
		
		// BazFeatureCallGreaterThanLiteralZero (success)
		assertCondition(fooInstance, 'FooBazFeatureCallGreaterThanLiteralZero', true, "if baz exists then baz -> other > 0")
		
		// BazFeatureCallGreaterThanLiteralFive (success)
		assertCondition(fooInstance, 'FooBazFeatureCallGreaterThanLiteralFive', true, "if baz exists then baz -> other > 5")	
	}

	@Test
	def oneConditionShouldFail() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(10), 'after', BigDecimal.valueOf(0)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(10)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance))))

		// FeatureCallComparisonDecreasing (success)
		assertCondition(fooInstance, 'FooFeatureCallComparisonDecreasing', true, "if bar exists then bar -> before > bar -> after")		
		
		// BarFeatureCallGreaterThanLiteralZero (fail)
		assertCondition(fooInstance, 'FooBarFeatureCallGreaterThanLiteralZero', false, "if bar exists then bar -> after > 0")		
		
		// BazFeatureCallGreaterThanLiteralZero (success)
		assertCondition(fooInstance, 'FooBazFeatureCallGreaterThanLiteralZero', true, "if baz exists then baz -> other > 0")
				
		// BazFeatureCallGreaterThanLiteralFive (success)
		assertCondition(fooInstance, 'FooBazFeatureCallGreaterThanLiteralFive', true, "if baz exists then baz -> other > 5")	
	}
	
	@Test
	def allConditionsShouldFail() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(-10), 'after', BigDecimal.valueOf(0)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(0)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance))))

		// FeatureCallComparisonDecreasing (fail)
		val dataRuleBarDescreasing = ValidationResult.cast(classes.runDataRule(fooInstance, 'FooFeatureCallComparisonDecreasing'))
		assertFalse(dataRuleBarDescreasing.success)
		assertThat(dataRuleBarDescreasing.definition, is("if bar exists then bar -> before > bar -> after"))
		assertThat(dataRuleBarDescreasing.failureReason.orElse(""), is("all elements of paths [Foo->getBar[0]->getBefore] values [-10] are not > than all elements of paths [Foo->getBar[0]->getAfter] values [0]"))

		// BarFeatureCallGreaterThanLiteralZero (fail)
		val dataRuleBarGreaterThanZero = ValidationResult.cast(classes.runDataRule(fooInstance, 'FooBarFeatureCallGreaterThanLiteralZero'))
		assertFalse(dataRuleBarGreaterThanZero.success)
		assertThat(dataRuleBarGreaterThanZero.getDefinition(), is("if bar exists then bar -> after > 0"))
		assertThat(dataRuleBarGreaterThanZero.failureReason.orElse(""), is("all elements of paths [Foo->getBar[0]->getAfter] values [0] are not > than all elements of paths [Integer] values [0]"))
		
		// BazFeatureCallGreaterThanLiteralZero (fail)
		val dataRuleBazGreaterThanZero = ValidationResult.cast(classes.runDataRule(fooInstance, 'FooBazFeatureCallGreaterThanLiteralZero'))
		assertFalse(dataRuleBazGreaterThanZero.success)
		assertThat(dataRuleBazGreaterThanZero.definition, is("if baz exists then baz -> other > 0"))
		assertThat(dataRuleBazGreaterThanZero.failureReason.orElse(""), is("all elements of paths [Foo->getBaz->getOther] values [0] are not > than all elements of paths [Integer] values [0]"))
		
		// BazFeatureCallGreaterThanLiteralFive (fail)
		val dataRuleBazGreaterThanFive = ValidationResult.cast(classes.runDataRule(fooInstance, 'FooBazFeatureCallGreaterThanLiteralFive'))
		assertFalse(dataRuleBazGreaterThanFive.success)
		assertThat(dataRuleBazGreaterThanFive.definition, is("if baz exists then baz -> other > 5"))
		assertThat(dataRuleBazGreaterThanFive.failureReason.orElse(""), is("all elements of paths [Foo->getBaz->getOther] values [0] are not > than all elements of paths [Integer] values [5]"))
	}

	// Util methods
		
	def assertCondition(RosettaModelObject model, String dataRuleName, boolean expectedSuccess, String expectedDefinition) {
		val dataRuleResult = ValidationResult.cast(classes.runDataRule(model, dataRuleName))
		assertThat(dataRuleResult.success, is(expectedSuccess))	
		assertThat(dataRuleResult.definition, is(expectedDefinition))
	}
}