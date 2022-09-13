package com.regnosys.rosetta.generator.java.rule

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.rosetta.model.lib.path.RosettaPath
import java.util.Map
import java.util.Optional
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ChoiceRuleGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper
	@Inject extension RosettaExtensions
	
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
			'field2', 'field two value'),
			of())
	
		val testChoiceRuleClass = classes.get(rootPackage.choiceRule.name + ".TestRequiredChoice")
		
		val testChoiceRule = testChoiceRuleClass.declaredConstructor.newInstance;
		
		val validationResult = testChoiceRuleClass.getMatchingMethod("validate", #[RosettaPath, testInstance.class])
			.invoke(testChoiceRule, null, testInstance)
				
		val isSuccess = validationResult.class.getMethod("isSuccess", null).invoke(validationResult) as Boolean
		assertFalse(isSuccess)

		val failureReason = validationResult.class.getMethod("getFailureReason", null).invoke(validationResult) as Optional<String>
		assertEquals('One and only one field must be set of \'field1\', \'field2\'. Set fields are \'field1\', \'field2\'.', failureReason.get)
	}
	
	@Test
	def void failedOptionalChoiceRuleValidation() {
		val classes = createTestClassAndRule()
			
		val testInstance = classes.createInstanceUsingBuilder('Test',of(
			'field1', 'field one value',
			'field2', 'field two value'),
			of())
		
		println(classes)
		val testChoiceRuleClass = classes.get(rootPackage.choiceRule.name + ".TestOptionalChoice")
		val testChoiceRule = testChoiceRuleClass.declaredConstructor.newInstance;
		
		val validationResult = testChoiceRuleClass.getMatchingMethod("validate", #[RosettaPath ,testInstance.class])
			.invoke(testChoiceRule, null, testInstance)
				
		val isSuccess = validationResult.class.getMethod("isSuccess", null).invoke(validationResult) as Boolean
		assertFalse(isSuccess)

		val failureReason = validationResult.class.getMethod("getFailureReason", null).invoke(validationResult) as Optional<String>		
		assertEquals('Zero or one field must be set of \'field1\', \'field2\'. Set fields are \'field1\', \'field2\'.', failureReason.get)
	}
	
	protected def Map<String, Class<?>> createTestClassAndRule() {
		return '''
			type Test:
				field1 string (0..1)
				field2 string (0..1)
			
				condition RequiredChoice:
					required choice field1, field2
			
				condition OptionalChoice:
					optional choice field1, field2
			
		'''
		.generateCode
		.compileToClasses
	}	
}