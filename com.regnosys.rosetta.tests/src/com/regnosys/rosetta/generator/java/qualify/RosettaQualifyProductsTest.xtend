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
class RosettaQualifyProductsTest {
	
	@Inject extension CodeGeneratorTestHelper
	@Inject extension QualifyTestHelper
	
	Map<String, Class<?>> classes
	
	@BeforeEach
	def void setUp() {
		val code = '''
			isProduct root Foo;
			
			type Foo:
				bar Bar (0..*)
				corge number (0..1)
			
			type Bar:
				baz Baz (0..1)
				qux number (0..1)

			type Baz:
				quux number (0..1)

			func Qualify_BranchNodeCountComparisonToLiteral:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> baz count = 2
			
			func Qualify_BranchAndLeafNodeCountComparisonToLiterals:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> baz count = 1
					and foo -> bar -> qux count = 1
			
			func Qualify_LeafNodeCountComparisonToLiteral:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> qux count = 2
			
			func Qualify_BranchNodeCountComparisonToFeatureCall:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> baz count = foo -> corge
			
			func Qualify_LeafNodeCountComparisonToFeatureCall:
				[qualification Product]
				inputs: foo Foo (1..1)
				output: is_product boolean (1..1)
				assign-output is_product:
					foo -> bar -> qux count = foo -> corge
		'''.generateCode
		//println(code)
		classes = code.compileToClasses
	}
	
	@Test
	def should_match_BranchNodeCountComparisonToLiteral_only() {
		val baz = classes.createInstanceUsingBuilder('Baz', of('quux', BigDecimal.valueOf(1.1)), of())
		val bar1 = classes.createInstanceUsingBuilder('Bar', of('baz', baz), of())
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('baz', baz), of())
		val foo = createFoo(ImmutableList.of(bar1, bar2), 5)
		
		val results = createUtilAndGetAllResults(foo)
		
		assertResult(results, 'BranchNodeCountComparisonToLiteral', true)
		assertResult(results, 'BranchAndLeafNodeCountComparisonToLiterals', false)
		assertResult(results, 'LeafNodeCountComparisonToLiteral', false)
		assertResult(results, 'BranchNodeCountComparisonToFeatureCall', false)
		assertResult(results, 'LeafNodeCountComparisonToFeatureCall', false)
	}

	@Test
	def should_match_BranchAndLeafNodeCountComparisonToLiterals_only() {
		val baz = classes.createInstanceUsingBuilder('Baz', of('quux', BigDecimal.valueOf(1.1)), of())
		val bar1 = classes.createInstanceUsingBuilder('Bar', of('baz', baz), of())
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('qux', BigDecimal.valueOf(1.2)), of())
		val foo = createFoo(ImmutableList.of(bar1, bar2), 5)

		val results = createUtilAndGetAllResults(foo)

		assertResult(results, 'BranchNodeCountComparisonToLiteral', false)
		assertResult(results, 'BranchAndLeafNodeCountComparisonToLiterals', true)
		assertResult(results, 'LeafNodeCountComparisonToLiteral', false)
		assertResult(results, 'BranchNodeCountComparisonToFeatureCall', false)
		assertResult(results, 'LeafNodeCountComparisonToFeatureCall', false)
	}
		
	@Test
	def should_match_LeafNodeCountComparisonToLiteral_only() {
		val bar1 = classes.createInstanceUsingBuilder('Bar', of('qux', BigDecimal.valueOf(1.1)), of())
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('qux', BigDecimal.valueOf(1.2)), of())
		val foo = createFoo(ImmutableList.of(bar1, bar2), 5)

		val results = createUtilAndGetAllResults(foo)

		assertResult(results, 'BranchNodeCountComparisonToLiteral', false)
		assertResult(results, 'BranchAndLeafNodeCountComparisonToLiterals', false)
		assertResult(results, 'LeafNodeCountComparisonToLiteral', true)
		assertResult(results, 'BranchNodeCountComparisonToFeatureCall', false)
		assertResult(results, 'LeafNodeCountComparisonToFeatureCall', false)
	}
	
	@Test
	def should_match_BranchNodeCountComparisonToFeatureCall_only() {
		val baz = classes.createInstanceUsingBuilder('Baz', of('quux', BigDecimal.valueOf(100)), of())
		val bar = classes.createInstanceUsingBuilder('Bar', of('baz', baz), of())
		val foo = createFoo(ImmutableList.of(bar), 1)

		val results = createUtilAndGetAllResults(foo)

		assertResult(results, 'BranchNodeCountComparisonToLiteral', false)
		assertResult(results, 'BranchAndLeafNodeCountComparisonToLiterals', false)
		assertResult(results, 'LeafNodeCountComparisonToLiteral', false)
		assertResult(results, 'BranchNodeCountComparisonToFeatureCall', true)
		assertResult(results, 'LeafNodeCountComparisonToFeatureCall', false)
	}
	
	@Test
	def should_match_LeafNodeCountComparisonToFeatureCall_only() {
		val bar = classes.createInstanceUsingBuilder('Bar', of('qux', BigDecimal.valueOf(1.1)), of())
		val foo = createFoo(ImmutableList.of(bar), 1)

		val results = createUtilAndGetAllResults(foo)

		assertResult(results, 'BranchNodeCountComparisonToLiteral', false)
		assertResult(results, 'BranchAndLeafNodeCountComparisonToLiterals', false)
		assertResult(results, 'LeafNodeCountComparisonToLiteral', false)
		assertResult(results, 'BranchNodeCountComparisonToFeatureCall', false)
		assertResult(results, 'LeafNodeCountComparisonToFeatureCall', true)
	}
	
	@Test
	def should_notMatchUnsetObject() {
		val foo = createFoo(ImmutableList.of(), 5)
		
		val results = createUtilAndGetAllResults(foo)
		
		assertResult(results, 'BranchNodeCountComparisonToLiteral', false)
		assertResult(results, 'BranchAndLeafNodeCountComparisonToLiterals', false)
		assertResult(results, 'LeafNodeCountComparisonToLiteral', false)
		assertResult(results, 'BranchNodeCountComparisonToFeatureCall', false)
		assertResult(results, 'LeafNodeCountComparisonToFeatureCall', false)
	}
		
	def assertResult(List<QualifyResult> results, String isProductName, boolean expectedSuccess) {
		val result = results.filter[it.name==isProductName].toList.get(0)
		assertThat(result.success, is(expectedSuccess))
	}
	
	def createFoo(List<Object> bars, int corge) {
		return RosettaModelObject.cast(
			classes.createInstanceUsingBuilder(
				'Foo', 
				of('corge', BigDecimal.valueOf(corge)), 
				of('bar', bars)))
	}
}