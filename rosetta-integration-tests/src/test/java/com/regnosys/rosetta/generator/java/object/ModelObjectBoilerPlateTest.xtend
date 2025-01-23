package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.*
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class ModelObjectBoilerPlateTest {
	
	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper
	
	@Test
	def void shouldGenerateObjectWithBoilerPlate() {
		val code = '''
			type Test:
				testField string (1..1)
		'''.generateCode
	
		val classes = code.compileToClasses
	
		
		val thisTestClass = classes.get(rootPackage + '.Test')
		val thisTestBuilder = thisTestClass.getMethod('builder').invoke(null)
		thisTestBuilder.class.getMethod('setTestField', String).invoke(thisTestBuilder, 'test-value'); 
		val thisTest = thisTestBuilder.class.getMethod('build').invoke(thisTestBuilder);
		
		val thatTestClass = classes.get(rootPackage + '.Test')
		val thatTestBuilder = thatTestClass.getMethod('builder').invoke(null)
		thatTestBuilder.class.getMethod('setTestField', String).invoke(thatTestBuilder, 'test-value'); 
		val thatTest = thatTestBuilder.class.getMethod('build').invoke(thatTestBuilder);
		
		assertThat(thisTest, is(thatTest))
		assertThat(thisTest.toString, allOf(containsString('Test'), containsString('testField=test-value')))
	}
	
	@Test
	def void shouldGenerateObjectBuilderWithBoilerPlate() {
		val code = '''
			type Test:
				testField string (1..1)
		'''.generateCode
	
		val classes = code.compileToClasses
	
		val thisTestClass = classes.get(rootPackage + '.Test')
		val thisTestBuilder = thisTestClass.getMethod('builder').invoke(null)
		thisTestBuilder.class.getMethod('setTestField', String).invoke(thisTestBuilder, 'test-value'); 
		
		val thatTestClass = classes.get(rootPackage + '.Test')
		val thatTestBuilder = thatTestClass.getMethod('builder').invoke(null)
		thatTestBuilder.class.getMethod('setTestField', String).invoke(thatTestBuilder, 'test-value'); 
		
		assertThat(thisTestBuilder, is(thatTestBuilder))
		assertThat(thisTestBuilder.toString, allOf(containsString('TestBuilder'), containsString('testField=test-value')))
	}
	
	@Test
	def void shouldGenerateHashCodeForEnumsUsingClassName() {
		val code = '''
			enum TestEnum :
				TestEnumValue
			
			type ThatHasEnums:
				testEnum TestEnum (1..1)
				testEnums TestEnum (1..*)
		'''.generateCode
		
		val thatHasEnumsClass = code.get(rootPackage + '.ThatHasEnums')
				
		val singleHashCodeContribution = '_result = 31 * _result + (testEnum != null ? testEnum.getClass().getName().hashCode() : 0);'
		val multipleHashCodeContribution = '_result = 31 * _result + (testEnums != null ? testEnums.stream().map(Object::getClass).map(Class::getName).mapToInt(String::hashCode).sum() : 0);'
		
		 assertThat(thatHasEnumsClass, allOf(containsString(singleHashCodeContribution), containsString(multipleHashCodeContribution)))
	}
}