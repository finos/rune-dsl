package com.regnosys.rosetta.generator.java.expression

import com.google.common.collect.ImmutableList
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.model.lib.RosettaModelObject
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
import com.regnosys.rosetta.generator.java.function.FunctionGeneratorHelper
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class RosettaCountOperationTest {
	
	@Inject extension CodeGeneratorTestHelper
	@Inject extension FunctionGeneratorHelper
	
	Map<String, Class<?>> classes
	
	@BeforeEach
	def void setUp() {
		val code = '''
			type Foo:
				bar Bar (0..*)
				corge number (0..1)
			
			type Bar:
				baz Baz (0..1)
				qux number (0..1)

			type Baz:
				quux number (0..1)

			func BranchNodeCountComparisonToLiteral:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar -> baz count = 2
			
			func BranchAndLeafNodeCountComparisonToLiterals:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar -> baz count = 1
					and foo -> bar -> qux count = 1
			
			func LeafNodeCountComparisonToLiteral:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar -> qux count = 2
			
			func BranchNodeCountComparisonToFeatureCall:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar -> baz count = foo -> corge
			
			func LeafNodeCountComparisonToFeatureCall:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
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
		
		assertResult('BranchNodeCountComparisonToLiteral', foo, true)
		assertResult('BranchAndLeafNodeCountComparisonToLiterals', foo, false)
		assertResult('LeafNodeCountComparisonToLiteral', foo, false)
		assertResult('BranchNodeCountComparisonToFeatureCall', foo, false)
		assertResult('LeafNodeCountComparisonToFeatureCall', foo, false)
	}

	@Test
	def should_match_BranchAndLeafNodeCountComparisonToLiterals_only() {
		val baz = classes.createInstanceUsingBuilder('Baz', of('quux', BigDecimal.valueOf(1.1)), of())
		val bar1 = classes.createInstanceUsingBuilder('Bar', of('baz', baz), of())
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('qux', BigDecimal.valueOf(1.2)), of())
		val foo = createFoo(ImmutableList.of(bar1, bar2), 5)

		assertResult('BranchNodeCountComparisonToLiteral', foo, false)
		assertResult('BranchAndLeafNodeCountComparisonToLiterals', foo, true)
		assertResult('LeafNodeCountComparisonToLiteral', foo, false)
		assertResult('BranchNodeCountComparisonToFeatureCall', foo, false)
		assertResult('LeafNodeCountComparisonToFeatureCall', foo, false)
	}
		
	@Test
	def should_match_LeafNodeCountComparisonToLiteral_only() {
		val bar1 = classes.createInstanceUsingBuilder('Bar', of('qux', BigDecimal.valueOf(1.1)), of())
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('qux', BigDecimal.valueOf(1.2)), of())
		val foo = createFoo(ImmutableList.of(bar1, bar2), 5)

		assertResult('BranchNodeCountComparisonToLiteral', foo, false)
		assertResult('BranchAndLeafNodeCountComparisonToLiterals', foo, false)
		assertResult('LeafNodeCountComparisonToLiteral', foo, true)
		assertResult('BranchNodeCountComparisonToFeatureCall', foo, false)
		assertResult('LeafNodeCountComparisonToFeatureCall', foo, false)
	}
	
	@Test
	def should_match_BranchNodeCountComparisonToFeatureCall_only() {
		val baz = classes.createInstanceUsingBuilder('Baz', of('quux', BigDecimal.valueOf(100)), of())
		val bar = classes.createInstanceUsingBuilder('Bar', of('baz', baz), of())
		val foo = createFoo(ImmutableList.of(bar), 1)

		assertResult('BranchNodeCountComparisonToLiteral', foo, false)
		assertResult('BranchAndLeafNodeCountComparisonToLiterals', foo, false)
		assertResult('LeafNodeCountComparisonToLiteral', foo, false)
		assertResult('BranchNodeCountComparisonToFeatureCall', foo, true)
		assertResult('LeafNodeCountComparisonToFeatureCall', foo, false)
	}
	
	@Test
	def should_match_LeafNodeCountComparisonToFeatureCall_only() {
		val bar = classes.createInstanceUsingBuilder('Bar', of('qux', BigDecimal.valueOf(1.1)), of())
		val foo = createFoo(ImmutableList.of(bar), 1)

		assertResult('BranchNodeCountComparisonToLiteral', foo, false)
		assertResult('BranchAndLeafNodeCountComparisonToLiterals', foo, false)
		assertResult('LeafNodeCountComparisonToLiteral', foo, false)
		assertResult('BranchNodeCountComparisonToFeatureCall', foo, false)
		assertResult('LeafNodeCountComparisonToFeatureCall', foo, true)
	}
	
	@Test
	def should_notMatchUnsetObject() {
		val foo = createFoo(ImmutableList.of(), 5)
		
		assertResult('BranchNodeCountComparisonToLiteral', foo, false)
		assertResult('BranchAndLeafNodeCountComparisonToLiterals', foo, false)
		assertResult('LeafNodeCountComparisonToLiteral', foo, false)
		assertResult('BranchNodeCountComparisonToFeatureCall', foo, false)
		assertResult('LeafNodeCountComparisonToFeatureCall', foo, false)
	}

	// Util methods
		
	def assertResult(String funcName, RosettaModelObject input, boolean expectedResult) {
		val func = classes.createFunc(funcName);
		val res = func.invokeFunc(Boolean, input)
		assertThat(res, is(expectedResult))
	}
	
	def createFoo(List<Object> bars, int corge) {
		return RosettaModelObject.cast(
			classes.createInstanceUsingBuilder(
				'Foo', 
				of('corge', BigDecimal.valueOf(corge)), 
				of('bar', bars)))
	}
}