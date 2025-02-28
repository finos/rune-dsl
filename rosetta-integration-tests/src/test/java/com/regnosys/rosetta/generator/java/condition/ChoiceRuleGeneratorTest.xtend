package com.regnosys.rosetta.generator.java.condition

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import java.util.List
import java.util.Map
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static org.junit.jupiter.api.Assertions.*
import javax.inject.Inject
import com.regnosys.rosetta.RosettaEcoreUtil

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class ChoiceRuleGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension RosettaEcoreUtil
	@Inject extension ConditionTestHelper
	
	@Test
	def void choiceRuleJavaClassName() {
		assertEquals('Test', toConditionJavaType('Test'))
		assertEquals('TestChoiceRule', toConditionJavaType('Test_choiceRule'))
		assertEquals('TestChoiceRule', toConditionJavaType('Test_choice_rule'))
		assertEquals('TestChoiceRule', toConditionJavaType('TestChoiceRule'))
		assertEquals('TestChoiceRule', toConditionJavaType('Test_ChoiceRule'))
		assertEquals('TestChoice', toConditionJavaType('Test_choice'))
	}
	
	@Test
	def void failedRequiredChoiceRuleValidation() {
		val classes = createTestClassAndRule()
			
		val testInstance = classes.createInstanceUsingBuilder('Test',of(
			'field1', 'field one value',
			'field2', List.of('field two value')),
			of())
	
		val validationResult = classes.runCondition(testInstance, "TestRequiredChoice")
				
		assertFalse(validationResult.isSuccess)

		val failureReason = validationResult.failureReason
		assertEquals('One and only one field must be set of \'field1\', \'field2\'. Set fields are \'field1\', \'field2\'.', failureReason.get)
	}
	
	@Test
	def void succeededRequiredChoiceRuleValidation() {
		val classes = createTestClassAndRule()
			
		val testInstance = classes.createInstanceUsingBuilder('Test',of(
			'field1', 'field one value',
			'field2', List.of()),
			of())
	
		val validationResult = classes.runCondition(testInstance, "TestRequiredChoice")
					
		assertTrue(validationResult.isSuccess)
	}
	
	@Test
	def void failedOptionalChoiceRuleValidation() {
		val classes = createTestClassAndRule()
			
		val testInstance = classes.createInstanceUsingBuilder('Test',of(
			'field1', 'field one value',
			'field2', List.of('field two value')),
			of())
		
		val validationResult = classes.runCondition(testInstance, "TestOptionalChoice")
				
		assertFalse(validationResult.isSuccess)

		val failureReason = validationResult.failureReason
		assertEquals('Zero or one field must be set of \'field1\', \'field2\'. Set fields are \'field1\', \'field2\'.', failureReason.get)
	}
	
	private def Map<String, Class<?>> createTestClassAndRule() {
		return '''
			type Test:
				field1 string (0..1)
				field2 string (0..2)
			
				condition RequiredChoice:
					required choice field1, field2
			
				condition OptionalChoice:
					optional choice field1, field2
			
		'''
		.generateCode
		.compileToClasses
	}
}