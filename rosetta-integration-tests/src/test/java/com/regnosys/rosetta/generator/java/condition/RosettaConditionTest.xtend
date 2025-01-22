package com.regnosys.rosetta.generator.java.condition

import com.google.common.collect.ImmutableList
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
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
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
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
					then bar first -> before > bar first -> after
				
				condition BarFeatureCallGreaterThanLiteralZero:
					if bar exists 
					then bar first -> after > 0
							
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
		assertCondition(fooInstance, 'FooFeatureCallComparisonDecreasing', true, "if bar exists then bar first -> before > bar first -> after")		
		
		// BarFeatureCallGreaterThanLiteralZero (success)
		assertCondition(fooInstance, 'FooBarFeatureCallGreaterThanLiteralZero', true, "if bar exists then bar first -> after > 0")		
		
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
		assertCondition(fooInstance, 'FooFeatureCallComparisonDecreasing', true, "if bar exists then bar first -> before > bar first -> after")		
		
		// BarFeatureCallGreaterThanLiteralZero (fail)
		assertCondition(fooInstance, 'FooBarFeatureCallGreaterThanLiteralZero', false, "if bar exists then bar first -> after > 0")		
		
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
		val conditionBarDescreasing = ValidationResult.cast(classes.runCondition(fooInstance, 'FooFeatureCallComparisonDecreasing'))
		assertFalse(conditionBarDescreasing.success)
		assertThat(conditionBarDescreasing.definition, is("if bar exists then bar first -> before > bar first -> after"))
		assertThat(conditionBarDescreasing.failureReason.orElse(""), is("all elements of paths [Foo->getBar[0]->getBefore] values [-10] are not > than all elements of paths [Foo->getBar[0]->getAfter] values [0]"))

		// BarFeatureCallGreaterThanLiteralZero (fail)
		val conditionBarGreaterThanZero = ValidationResult.cast(classes.runCondition(fooInstance, 'FooBarFeatureCallGreaterThanLiteralZero'))
		assertFalse(conditionBarGreaterThanZero.success)
		assertThat(conditionBarGreaterThanZero.getDefinition(), is("if bar exists then bar first -> after > 0"))
		assertThat(conditionBarGreaterThanZero.failureReason.orElse(""), is("all elements of paths [Foo->getBar[0]->getAfter] values [0] are not > than all elements of paths [BigDecimal] values [0]"))
		
		// BazFeatureCallGreaterThanLiteralZero (fail)
		val conditionBazGreaterThanZero = ValidationResult.cast(classes.runCondition(fooInstance, 'FooBazFeatureCallGreaterThanLiteralZero'))
		assertFalse(conditionBazGreaterThanZero.success)
		assertThat(conditionBazGreaterThanZero.definition, is("if baz exists then baz -> other > 0"))
		assertThat(conditionBazGreaterThanZero.failureReason.orElse(""), is("all elements of paths [Foo->getBaz->getOther] values [0] are not > than all elements of paths [BigDecimal] values [0]"))
		
		// BazFeatureCallGreaterThanLiteralFive (fail)
		val conditionBazGreaterThanFive = ValidationResult.cast(classes.runCondition(fooInstance, 'FooBazFeatureCallGreaterThanLiteralFive'))
		assertFalse(conditionBazGreaterThanFive.success)
		assertThat(conditionBazGreaterThanFive.definition, is("if baz exists then baz -> other > 5"))
		assertThat(conditionBazGreaterThanFive.failureReason.orElse(""), is("all elements of paths [Foo->getBaz->getOther] values [0] are not > than all elements of paths [BigDecimal] values [5]"))
	}

	// Util methods
		
	def assertCondition(RosettaModelObject model, String conditionName, boolean expectedSuccess, String expectedDefinition) {
		val conditionResult = ValidationResult.cast(classes.runCondition(model, conditionName))
		assertThat(conditionResult.success, is(expectedSuccess))	
		assertThat(conditionResult.definition, is(expectedDefinition))
	}
}