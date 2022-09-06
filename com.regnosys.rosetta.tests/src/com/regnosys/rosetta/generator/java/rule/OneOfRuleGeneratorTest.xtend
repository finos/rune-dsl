package com.regnosys.rosetta.generator.java.rule

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.meta.RosettaMetaData
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

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class OneOfRuleGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper
	
	static final RosettaPath TEST_PATH = RosettaPath.valueOf('a.b.c')
	
	Map<String, Class<?>> classes
	
	@BeforeEach
	def void setUp() {
		classes = '''
			type Foo:
				attr1 string (0..1)
				attr2 string (0..1)
				
				condition: one-of
		'''
		.generateCode
		.compileToClasses
	}
	
	@Test
	def void oneOfRuleClassName() {		
		assertEquals('FooOneOfRule', ChoiceRuleGenerator.oneOfRuleClassName('Foo'))
	}
	
	@Test
	def void oneOfRuleName() {		
		assertEquals('Foo_oneOf', ChoiceRuleGenerator.oneOfRuleName('Foo'))
	}
	
	def <T extends RosettaModelObject> ValidationResult<T> doValidate(RosettaPath p, Validator<T> validator, RosettaModelObject toVal) {
		return validator.validate(p, toVal as T);
	}
	
	@Test
	def void shouldPassRuleAsOneAttributeSet() {
		val foo = getInstance('Foo', of('attr1', 'attr1 value'))
		
		val result = doValidate(TEST_PATH, getOneOfRule('Foo'), foo.toBuilder)
		
		assertTrue(result.success)
		assertFalse(result.failureReason.present)
	}
	
	@Test
	def void shouldFailRuleAsNeitherAttributesAreSet() {
		val foo = getInstance('Foo', of())
		
		val result = doValidate(TEST_PATH, getOneOfRule('Foo'), foo.toBuilder)
		
		assertFalse(result.success)
		assertThat(result.failureReason.orElse(''), is("One and only one field must be set of 'attr1', 'attr2'. No fields are set."))
	}
	
	@Test
	def void shouldFailRuleAsBothAttributesAreSet() {
		val foo = getInstance('Foo', of('attr1', 'attr1 value', 'attr2', 'attr2 value'))
		
		val result = doValidate(TEST_PATH, getOneOfRule('Foo'), foo.toBuilder)
		
		assertFalse(result.success)
		assertThat(result.failureReason.orElse(''), is("One and only one field must be set of 'attr1', 'attr2'. Set fields are 'attr1', 'attr2'."))
	}
	
	private def getInstance(String className, Map<String, Object> itemsToSet) {
		classes.createInstanceUsingBuilder(className, itemsToSet) as RosettaModelObject
	}
	
	private def Validator<? extends RosettaModelObject> getOneOfRule(String className) {
		val metaClass = classes.get(rootPackage.meta.name + '.' + className + 'Meta').declaredConstructor.newInstance as RosettaMetaData<? extends RosettaModelObject>
		val choiceRules = metaClass.choiceRuleValidators
		
		assertThat(choiceRules.size, is(1))
		
		val oneOfRule = choiceRules.get(0)
		
		assertThat(oneOfRule.class.simpleName, is(className + 'OneOf0'))
		
		return oneOfRule as Validator<RosettaModelObject>
	}
}