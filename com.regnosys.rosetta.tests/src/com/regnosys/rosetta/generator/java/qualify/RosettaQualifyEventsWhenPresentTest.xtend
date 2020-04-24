package com.regnosys.rosetta.generator.java.qualify

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.model.lib.RosettaModelObject
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
class RosettaQualifyEventsWhenPresentTest {
	
	@Inject extension CodeGeneratorTestHelper
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
			
			isEvent WhenPresentExpr
				Foo -> baz -> bazValue when present = 15
			'''.generateCode
		//println(code)
		classes = code.compileToClasses
	}
	
	@Test
	def whenPresentExpr_isPresent_and_matches_should_qualify() {
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(15)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of()))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		val result = getQualifyResult(results, "WhenPresentExpr")
		assertTrue(result.success, 'Unexpected success result')
		assertThat('Unexpected number of expressionDataRule results', result.expressionDataRuleResults.size, is(1))
	}
	
	@Test
	def whenPresentExpr_isPresent_and_doesNotMatch_should_not_qualify() {
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(20)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of()))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		val result = getQualifyResult(results, "WhenPresentExpr")
		assertFalse(result.success)
		assertThat('Unexpected number of expressionDataRule results', result.expressionDataRuleResults.size, is(1))
	}
	
	@Test
	def whenPresentExpr_isNotPresent_should_qualify() {
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(20)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of()))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		val result = getQualifyResult(results, "WhenPresentExpr")
		assertTrue(result.success, 'Unexpected success result')
		assertThat('Unexpected number of expressionDataRule results', result.expressionDataRuleResults.size, is(1))
	}
}