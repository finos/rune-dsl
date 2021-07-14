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
				other number (0..1)
				beforeWithScheme number (0..1)
					[metadata scheme]
				afterWithScheme number (0..1)
					[metadata scheme]
				beforeList number (0..*)
				afterList number (0..*)
				beforeListWithScheme number (0..*)
					[metadata scheme]
				afterListWithScheme number (0..*)
					[metadata scheme]
			
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

			func Qualify_MultipleExists:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before multiple exists

			func Qualify_OnlyExists:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					foo -> bar -> before only exists
			
			func Qualify_OnlyExistsMultiplePaths:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					( foo -> bar -> before, foo -> bar -> after ) only exists

			func Qualify_OnlyExistsPathWithScheme:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					( foo -> bar -> before, foo -> bar -> afterWithScheme ) only exists
			
			func Qualify_OnlyExistsBothPathsWithScheme:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					( foo -> bar -> beforeWithScheme, foo -> bar -> afterWithScheme ) only exists

			func Qualify_OnlyExistsListMultiplePaths:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					( foo -> bar -> before, foo -> bar -> afterList ) only exists

			func Qualify_OnlyExistsListPathWithScheme:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					( foo -> bar -> before, foo -> bar -> afterListWithScheme ) only exists
			
			func Qualify_OnlyExistsListBothPathsWithScheme:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				assign-output is_event:
					( foo -> bar -> beforeListWithScheme, foo -> bar -> afterListWithScheme ) only exists

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
		//println(code)
		//.writeClasses("RosettaQualifyEventsExistsTest")
		classes = code.compileToClasses
	}

	@Test
	def shouldQualifyExistsAndSingleExists() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(15)), of())
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		assertEvent(results, "Exists", true)
		assertEvent(results, "SingleExists", true)
	}

	@Test
	def shouldQualifyOnlyExists1() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', 
			of('before', BigDecimal.valueOf(15)), 
			of()
		)
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		assertEvent(results, "OnlyExists", true)
		assertEvent(results, "OnlyExistsMultiplePaths", false)
		assertEvent(results, "OnlyExistsPathWithScheme", false)
		assertEvent(results, "OnlyExistsBothPathsWithScheme", false)
	}
	
	@Test
	def shouldQualifyOnlyExists2() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', 
			of('before', BigDecimal.valueOf(15),
				'after', BigDecimal.valueOf(20)), 
			of()
		)
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		assertEvent(results, "OnlyExists", false)
		assertEvent(results, "OnlyExistsMultiplePaths", true)
		assertEvent(results, "OnlyExistsPathWithScheme", false)
		assertEvent(results, "OnlyExistsBothPathsWithScheme", false)
	}
	
	@Test
	def shouldQualifyOnlyExists3() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', 
			of('before', BigDecimal.valueOf(15),
				'afterWithSchemeValue', BigDecimal.valueOf(20)), 
			of()
		)
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		assertEvent(results, "OnlyExists", false)
		assertEvent(results, "OnlyExistsMultiplePaths", false)
		assertEvent(results, "OnlyExistsPathWithScheme", true)
		assertEvent(results, "OnlyExistsBothPathsWithScheme", false)
	}
	
	@Test
	def shouldQualifyOnlyExists4() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', 
			of('beforeWithSchemeValue', BigDecimal.valueOf(15),
				'afterWithSchemeValue', BigDecimal.valueOf(20)), 
			of()
		)
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))
		
		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		assertEvent(results, "OnlyExists", false)
		assertEvent(results, "OnlyExistsMultiplePaths", false)
		assertEvent(results, "OnlyExistsPathWithScheme", false)
		assertEvent(results, "OnlyExistsBothPathsWithScheme", true)
	}
	
	@Test
	def shouldQualifyOnlyExists5() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', 
			of('before', BigDecimal.valueOf(15)), 
			of('afterList', List.of(BigDecimal.valueOf(20), BigDecimal.valueOf(21)))
		)
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))
		
		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		assertEvent(results, "OnlyExistsListMultiplePaths", true)
		assertEvent(results, "OnlyExistsListPathWithScheme", false)
		assertEvent(results, "OnlyExistsListBothPathsWithScheme", false)
	}
	
	@Test
	def shouldNotQualifyOnlyExists() {
		val barInstance = classes.createInstanceUsingBuilder('Bar', 
			of('before', BigDecimal.valueOf(15),
				'after', BigDecimal.valueOf(20),
				'other', BigDecimal.valueOf(25)), 
			of()
		)
		val fooInstance = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance))))

		// Assert Event
		val results = createUtilAndGetAllResults(fooInstance)
		assertEvent(results, "OnlyExists", false)
		assertEvent(results, "OnlyExistsMultiplePaths", false)
	}
 	
	// Util methods
		
	def assertEvent(List<QualifyResult> results, String isEventName, boolean expectedSuccess) {
		val result = getQualifyResult(results, isEventName)
		assertThat('Expected result (' + expectedSuccess + ') but got ' + result, result.success, is(expectedSuccess))
	}
}