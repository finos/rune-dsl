package com.regnosys.rosetta.generator.java.rule

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import java.util.Map
import java.util.Optional
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static org.junit.jupiter.api.Assertions.*
import com.rosetta.model.lib.path.RosettaPath

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ChoiceRuleGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper
	
	@Test
	def void choiceRuleJavaClassName() {		
		assertEquals('TestChoiceRule', ChoiceRuleGenerator.choiceRuleClassName('Test'))
		assertEquals('TestChoiceRule', ChoiceRuleGenerator.choiceRuleClassName('Test_choiceRule'))
		assertEquals('TestChoiceRule', ChoiceRuleGenerator.choiceRuleClassName('Test_choice_rule'))
		assertEquals('TestChoiceRule', ChoiceRuleGenerator.choiceRuleClassName('TestChoiceRule'))
		assertEquals('TestChoiceRule', ChoiceRuleGenerator.choiceRuleClassName('Test_ChoiceRule'))
		assertEquals('TestChoiceRule', ChoiceRuleGenerator.choiceRuleClassName('Test_choice'))
	}
	
	@Test
	def void failedRequiredChoiceRuleValidation() {
		val classes = createTestClassAndRule()
			
		val testInstance = classes.createInstanceUsingBuilder('TestClass',of(
			'field1', 'field one value',
			'field2', 'field two value'),
			of())
	
		val testChoiceRuleClass = classes.get(javaPackages.choiceRule.packageName + ".Test1ChoiceRule")
		
		val testChoiceRule = testChoiceRuleClass.newInstance;
		
		val validationResult = testChoiceRuleClass.getMethod("validate", RosettaPath, testInstance.class)
			.invoke(testChoiceRule, null, testInstance)
				
		val isSuccess = validationResult.class.getMethod("isSuccess", null).invoke(validationResult) as Boolean
		assertFalse(isSuccess)

		val failureReason = validationResult.class.getMethod("getFailureReason", null).invoke(validationResult) as Optional<String>
		assertEquals('One and only one field must be set of \'field1\', \'field2\'. Set fields are \'field1\', \'field2\'.', failureReason.get)
	}
	
	@Test
	def void failedOptionalChoiceRuleValidation() {
		val classes = createTestClassAndRule()
			
		val testInstance = classes.createInstanceUsingBuilder('TestClass',of(
			'field1', 'field one value',
			'field2', 'field two value'),
			of())
	
		val testChoiceRuleClass = classes.get(javaPackages.choiceRule.packageName + ".Test2ChoiceRule")
		val testChoiceRule = testChoiceRuleClass.newInstance;
		
		val validationResult = testChoiceRuleClass.getMethod("validate", RosettaPath ,testInstance.class)
			.invoke(testChoiceRule, null, testInstance)
				
		val isSuccess = validationResult.class.getMethod("isSuccess", null).invoke(validationResult) as Boolean
		assertFalse(isSuccess)

		val failureReason = validationResult.class.getMethod("getFailureReason", null).invoke(validationResult) as Optional<String>		
		assertEquals('Zero or one field must be set of \'field1\', \'field2\'. Set fields are \'field1\', \'field2\'.', failureReason.get)
	}
	
	protected def Map<String, Class<?>> createTestClassAndRule() {
		return '''
			class TestClass {
				field1 string (0..1);
				field2 string (0..1);
			}
			
			choice rule Test1_ChoiceRule 
				for TestClass required choice between field1 and field2
			
			choice rule Test2_ChoiceRule 
				for TestClass optional choice between field1 and field2
			
		'''
		.generateCode
		.compileToClasses
	}	
}