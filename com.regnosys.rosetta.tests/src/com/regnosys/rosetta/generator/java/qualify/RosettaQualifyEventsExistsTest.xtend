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
			isEvent root Foo;
			
			type Foo:
				bar Bar (0..*)
				baz Baz (0..1)
			
			type Bar:
				before number (0..1)
				after number (0..1)
			
			type Baz:
				bazValue number (0..1)
				other number (0..1)
			
			
			func Qualify_Exists:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before exists
			
			func Qualify_SingleExists:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before single exists
			
«««			func Qualify_MultipleExists:
«««				[qualification BusinessEvent]
«««				inputs: foo Foo (1..1)
«««				output: is_event boolean (1..1)
«««				assign-output is_event:
«««					foo -> bar -> before multiple exists
			
			func Qualify_OnlyExists:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before only exists
			
			func Qualify_OnlySingleExists:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before only single exists
			
«««			func Qualify_OnlyMultipleExists:
«««				[qualification BusinessEvent]
«««				inputs: foo Foo (1..1)
«««				output: is_event boolean (1..1)
«««				assign-output is_event:
«««					foo -> bar -> before only mulitple exists

«««			TODO tests compilation only, add unit test
			func Qualify_MultipleSeparateOr_NoAliases_Exists:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before exists or foo -> bar -> after exists

«««			TODO tests compilation only, add unit test
			func Qualify_MultipleOr_NoAliases_Exists:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					( foo -> bar -> before or foo -> bar -> after or foo -> baz -> other ) exists
			
«««			TODO tests compilation only, add unit test
			func Qualify_MultipleOrBranchNode_NoAliases_Exists:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					( foo -> bar or foo -> baz ) exists
			
«««			TODO tests compilation only, add unit test
			func Qualify_MultipleAnd_NoAliases_Exists:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					( foo -> bar -> before and foo -> bar -> after and foo -> baz -> other ) exists
			
«««			TODO tests compilation only, add unit test
			func Qualify_MultipleOrAnd_NoAliases_Exists:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before exists or ( foo -> bar -> after and foo -> baz -> other ) exists
			
«««			TODO tests compilation only, add unit test
			func Qualify_MultipleOrAnd_NoAliases_Exists2:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					(foo -> bar -> before and foo -> bar -> after) exists or foo -> baz -> other exists or foo -> baz -> bazValue exists
			
«««			TODO tests compilation only, add unit test
			func Qualify_MultipleOrAnd_NoAliases_Exists3:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					(foo -> bar -> before or foo -> bar -> after) exists or (foo -> baz -> other and foo -> baz -> bazValue) exists
			
«««			TODO tests compilation only, add unit test
			func Qualify_MultipleExistsWithOrAnd:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before exists or ( foo -> baz -> other exists and foo -> bar -> after exists ) or foo -> baz -> bazValue exists
			'''.generateCode
		//.writeClasses("RosettaQualifyEventsExistsTest")
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
	}

	@Test
	def listWith1Element_onlySingleExists_should_qualify() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(15)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		assertEvent(results, "OnlyExists", true, 1)
		assertEvent(results, "OnlySingleExists", true, 1)
	}
 
	@Test
	def listWith1Element_manyFieldsSetOnParentObject_onlySingleExists_should_not_qualify() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(15), 'after', BigDecimal.valueOf(10)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		val result = getQualifyResult(results, "OnlySingleExists")
		assertFalse(result.success, 'Expected onlySingleExists to fail because many fields are set on parent object')
		assertThat('Error messages do not match', result.toString, is("QualifyResult.OnlySingleExists FAILURE [Result.Expression FAILURE Error: {Qualify_OnlySingleExists returned false.}]"))
	}
	
	// Util methods
		
	def assertEvent(List<QualifyResult> results, String isEventName, boolean expectedSuccess, int expressionDataRuleResultCount) {
		val result = getQualifyResult(results, isEventName)
		assertThat('Expected result (' + expectedSuccess + ') but got ' + result, result.success, is(expectedSuccess))
		assertThat('Unexpected number of expressionDataRule results', result.expressionDataRuleResults.size, is(expressionDataRuleResultCount))
	}
}