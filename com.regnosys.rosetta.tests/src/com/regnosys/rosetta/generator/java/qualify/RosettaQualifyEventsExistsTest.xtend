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
import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaQualifyEventsExistsTest {
	
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
			
			isEvent Exists
				Foo -> bar -> before exists
			
			isEvent SingleExists
				Foo -> bar -> before single exists
			
			isEvent MultipleExists
				Foo -> bar -> before multiple exists
			
			isEvent OnlyExists
				Foo -> bar -> before only exists
			
			isEvent OnlySingleExists
				Foo -> bar -> before only single exists
			
			isEvent OnlyMultipleExists
				Foo -> bar -> before only multiple exists

«««			TODO tests compilation only, add unit test
			isEvent MultipleSeparateOr_NoAliases_Exists
				Foo -> bar -> before exists or Foo -> bar -> after exists

«««			TODO tests compilation only, add unit test
			isEvent MultipleOr_NoAliases_Exists
				( Foo -> bar -> before or Foo -> bar -> after or Foo -> baz -> other ) exists
			
«««			TODO tests compilation only, add unit test
			isEvent MultipleOrBranchNode_NoAliases_Exists
				( Foo -> bar or Foo -> baz ) exists
			
«««			TODO tests compilation only, add unit test
			isEvent MultipleAnd_NoAliases_Exists
				( Foo -> bar -> before and Foo -> bar -> after and Foo -> baz -> other ) exists
			
«««			TODO tests compilation only, add unit test
			isEvent MultipleOrAnd_NoAliases_Exists
				Foo -> bar -> before exists or ( Foo -> bar -> after and Foo -> baz -> other ) exists
			
«««			TODO tests compilation only, add unit test
			isEvent MultipleOrAnd_NoAliases_Exists2
				(Foo -> bar -> before and Foo -> bar -> after) exists or Foo -> baz -> other exists or Foo -> baz -> bazValue exists
			
«««			TODO tests compilation only, add unit test
			isEvent MultipleOrAnd_NoAliases_Exists3
				(Foo -> bar -> before or Foo -> bar -> after) exists or (Foo -> baz -> other and Foo -> baz -> bazValue) exists

«««			TODO tests compilation only, add unit test
			isEvent AliasExists
				aliasBefore exists
			
«««			TODO tests compilation only, add unit test
			isEvent MultipleOr_SomeAliases_Exists
				( Foo -> bar -> before or Foo -> bar -> after or aliasOther ) exists
			
«««			TODO tests compilation only, add unit test
			isEvent MultipleOr_AllAliases_Exists
				( aliasBefore or aliasAfter or aliasOther ) exists
			
«««			TODO tests compilation only, add unit test
			isEvent MultipleOr_SingleAlias_Exists
				aliasBeforeOrAfterOrOther exists
			
«««			TODO tests compilation only, add unit test
			isEvent MultipleExistsWithOrAnd
				Foo -> bar -> before exists or ( Foo -> baz -> other exists and Foo -> bar -> after exists ) or Foo -> baz -> bazValue exists
			
			alias aliasBefore
				Foo -> bar -> before
			
			alias aliasAfter
				Foo -> bar -> after
			
			alias aliasOther
				Foo -> baz -> other
			
			alias aliasBeforeOrAfterOrOther
				Foo -> bar -> before or Foo -> bar -> after or Foo -> baz -> other
			'''.generateCode
		//println(code)
		classes = code.compileToClasses
	}

	@Test
	def listWith1Element_singleExists_should_qualify() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(15)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		assertEvent(results, "Exists", true, 1)
		assertEvent(results, "SingleExists", true, 1)
		assertEvent(results, "MultipleExists", false, 1)
	}

	@Test
	def listWith2Elements_multipleExists_should_qualify() {
		val barInstance1 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(15)), of())
		val barInstance2 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(14)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance1, barInstance2))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		assertEvent(results, "Exists", true, 1)
		assertEvent(results, "SingleExists", false, 1)
		assertEvent(results, "MultipleExists", true, 1)
	}
	
	@Test
	def listWith1Element_onlySingleExists_should_qualify() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(15)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		assertEvent(results, "OnlyExists", true, 1)
		assertEvent(results, "OnlySingleExists", true, 1)
		assertEvent(results, "OnlyMultipleExists", false, 1)
	}
 
	@Test
	def listWith1Element_manyFieldsSetOnParentObject_onlySingleExists_should_not_qualify() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(15), 'after', BigDecimal.valueOf(10)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		val result = getQualifyResult(results, "OnlySingleExists")
		assertFalse(result.success, 'Expected onlySingleExists to fail because many fields are set on parent object')
		assertThat('Error messages do not match', result.toString, is("QualifyResult.OnlySingleExists FAILURE [Result.Expression FAILURE Error: {[before] is not the only field set. Other set fields: [after]}]"))
	}

	@Test
	def listWith2Elements_onlyMultipleExists_should_qualify() {
		val barInstance1 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(15)), of())
		val barInstance2 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(14)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance1, barInstance2))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		assertEvent(results, "OnlyExists", true, 1)
		assertEvent(results, "OnlySingleExists", false, 1)
		assertEvent(results, "OnlyMultipleExists", true, 1)
	}

	@Test
	def listWith2Elements_manyFieldsSetOnParentObject_onlyMultipleExists_should_not_qualify() {
		val barInstance1 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(15), 'after', BigDecimal.valueOf(10)), of())
		val barInstance2 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(14), 'after', BigDecimal.valueOf(10)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance1, barInstance2))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		val result = getQualifyResult(results, "OnlyMultipleExists")
		assertFalse(result.success, 'Expected onlyMultipleExists to fail because many fields are set on parent object')
		assertThat('Error messages do not match', result.toString, is("QualifyResult.OnlyMultipleExists FAILURE [Result.Expression FAILURE Error: {[before] is not the only field set. Other set fields: [after] and [before] is not the only field set. Other set fields: [after]}]"))
	}
	
	// Exists tests using alias
	
	@Test
	def aliasWithExists_should_qualify() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(15)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		val result = getQualifyResult(results, "AliasExists")
		assertTrue(result.success, 'Expected success result')
		assertThat('Unexpected number of expressionDataRule results', result.expressionDataRuleResults.size, is(1))	
	}

	@Test
	def aliasWithMultipleOrExists_should_qualify() {
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(0)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of()))

		val result1 = getQualifyResult(createUtilAndGetAllResults(fooInstance), "MultipleOr_NoAliases_Exists")
		assertTrue(result1.success, 'Expected success result')
	
		val result2 = getQualifyResult(createUtilAndGetAllResults(fooInstance), "MultipleOrBranchNode_NoAliases_Exists")
		assertTrue(result2.success, 'Expected success result')

		val result3 = getQualifyResult(createUtilAndGetAllResults(fooInstance), "MultipleOr_SomeAliases_Exists")
		assertTrue(result3.success, 'Expected success result')
	
		val result4 = getQualifyResult(createUtilAndGetAllResults(fooInstance), "MultipleOr_AllAliases_Exists")
		assertTrue(result4.success, 'Expected success result')
	
		val result5 = getQualifyResult(createUtilAndGetAllResults(fooInstance), "MultipleOr_SingleAlias_Exists")
		assertTrue(result5.success, 'Expected success result')
	}

	@Test
	def aliasWithMultipleAndExists_should_qualify() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(15), 'after', BigDecimal.valueOf(10)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(0)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance))))

		val result1 = getQualifyResult(createUtilAndGetAllResults(fooInstance), "MultipleAnd_NoAliases_Exists")
		assertTrue(result1.success, 'Expected success result')
	}

	@Test
	def aliasWithMultipleOrAndExists_should_qualify() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(15), 'after', BigDecimal.valueOf(10)), of())
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(0)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', bazInstance), of('bar', ImmutableList.of(barInstance))))

		val result1 = getQualifyResult(createUtilAndGetAllResults(fooInstance), "MultipleOrAnd_NoAliases_Exists")
		assertTrue(result1.success, 'Expected success result')
	}

	// Util methods
		
	def assertEvent(List<QualifyResult> results, String isEventName, boolean expectedSuccess, int expressionDataRuleResultCount) {
		val result = getQualifyResult(results, isEventName)
		assertThat('Expected result (' + expectedSuccess + ') but got ' + result, result.success, is(expectedSuccess))
		assertThat('Unexpected number of expressionDataRule results', result.expressionDataRuleResults.size, is(expressionDataRuleResultCount))
	}
}