package com.regnosys.rosetta.generator.java.condition

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.Validator
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
import com.rosetta.model.lib.validation.ValidationResult
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class OneOfRuleGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ConditionTestHelper
	
	static final RosettaPath TEST_PATH = RosettaPath.valueOf('a.b.c')
	
	Map<String, Class<?>> classes
	
	@BeforeEach
	def void setUp() {
		classes = '''
			type Foo:
				attr1 string (0..1)
				attr2 string (0..1)
				
				condition OneOf: one-of
		'''
		.generateCode
		.compileToClasses
	}
	
	def <T extends RosettaModelObject> ValidationResult<T> doValidate(RosettaPath p, Validator<T> validator, RosettaModelObject toVal) {
		return validator.validate(p, toVal as T);
	}
	
	@Test
	def void shouldPassRuleAsOneAttributeSet() {
		val foo = getInstance('Foo', of('attr1', 'attr1 value'))
		
		val result = doValidate(TEST_PATH, classes.createConditionInstance('FooOneOf'), foo.toBuilder)
		
		assertTrue(result.success)
		assertFalse(result.failureReason.present)
	}
	
	@Test
	def void shouldFailRuleAsNeitherAttributesAreSet() {
		val foo = getInstance('Foo', of())
		
		val result = doValidate(TEST_PATH, classes.createConditionInstance('FooOneOf'), foo.toBuilder)
		
		assertFalse(result.success)
		assertThat(result.failureReason.orElse(''), is("One and only one field must be set of 'attr1', 'attr2'. No fields are set."))
	}
	
	@Test
	def void shouldFailRuleAsBothAttributesAreSet() {
		val foo = getInstance('Foo', of('attr1', 'attr1 value', 'attr2', 'attr2 value'))
		
		val result = doValidate(TEST_PATH, classes.createConditionInstance('FooOneOf'), foo.toBuilder)
		
		assertFalse(result.success)
		assertThat(result.failureReason.orElse(''), is("One and only one field must be set of 'attr1', 'attr2'. Set fields are 'attr1', 'attr2'."))
	}
	
	private def getInstance(String className, Map<String, Object> itemsToSet) {
		classes.createInstanceUsingBuilder(className, itemsToSet) as RosettaModelObject
	}
}