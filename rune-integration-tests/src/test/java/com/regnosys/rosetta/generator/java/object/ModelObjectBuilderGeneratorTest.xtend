package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.Lists
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import java.util.List
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class ModelObjectBuilderGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper
	
	@Test
	def void shouldGenerateJavaBuilderThatExtendsParentBuilders() {
		val genereated = '''
			type A:
				aa string (0..1)
			
			type B extends A:
				bb string (0..1)
			
			type C extends B:
				cc string (0..1)
			
			type D extends C:
				dd string (0..1)
				dd2 string (0..1)
			
		'''.generateCode

		val classes = genereated.compileToClasses

		val classD = classes.get(rootPackage + ".D")
		val classDBuilderInstance = classD.getMethod("builder").invoke(null);

		classDBuilderInstance.class.getMethod('setAa', String).invoke(classDBuilderInstance, 'fieldA');
		classDBuilderInstance.class.getMethod('setBb', String).invoke(classDBuilderInstance, 'fieldB');
		classDBuilderInstance.class.getMethod('setCc', String).invoke(classDBuilderInstance, 'fieldC');
		classDBuilderInstance.class.getMethod('setDd', String).invoke(classDBuilderInstance, 'fieldD');

		val classDInstance = classDBuilderInstance.class.getMethod('build').invoke(classDBuilderInstance);

		assertEquals(classDInstance.toString, 'D {dd=fieldD, dd2=null} C {cc=fieldC} B {bb=fieldB} A {aa=fieldA}')
	}

	@Test
	def void useBuilderAddMultipleTimes() {
		val code = '''
			type Tester:
				items string (0..*)
			
		'''.generateCode

		val classes = code.compileToClasses

		val classTester = classes.get(rootPackage + ".Tester")
		val classTesterBuilderInstance = classTester.getMethod("builder").invoke(null);

		classTesterBuilderInstance.class.getMethod('addItems', String).invoke(classTesterBuilderInstance, 'item1');
		classTesterBuilderInstance.class.getMethod('addItems', String).invoke(classTesterBuilderInstance, 'item2');
		classTesterBuilderInstance.class.getMethod('addItems', String).invoke(classTesterBuilderInstance, 'item3');

		val classTesterInstance = classTesterBuilderInstance.class.getMethod('build').invoke(
			classTesterBuilderInstance);

		val items = classTesterInstance.class.getMethod('getItems').invoke(classTesterInstance) as List<String>;

		assertEquals(Lists.newArrayList('item1', 'item2', 'item3'), items)
	}
	
	@Test
	def void shouldGenerateObjectUsingBuilderOfBuildersPattern() {
		val code = '''
			enum TestEnum:
				testEnumValueOne
			
			type One:
				oneField string (1..1)
			
			type Test:
				multipleEnums TestEnum (1..*)
				multipleOnes One (1..*)
				singleOne One (1..1)
			
		'''.generateCode

		val testClassCode = code.get(rootPackage + '.Test')

		// Base Case
		assertThat(testClassCode,
			containsString(
				'this.multipleOnes = ofNullable(builder.getMultipleOnes()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);'))

		// Only do this for attributes that are RosettaClass
		assertThat(testClassCode, not(containsString('this.multipleEnums = builder.multipleEnums.stream()')))

		// Builder contains builders of RosettaClasses
		assertThat(testClassCode, containsString('List<? extends One> multipleOnes;'))
		assertThat(testClassCode, containsString('One.OneBuilder singleOne;'))

		// Builder setters handles adding builder types
		assertThat(testClassCode, containsString('public Test.TestBuilder setSingleOne(One _singleOne) {'))
		assertThat(testClassCode, containsString('public Test.TestBuilder addMultipleOnes(One _multipleOnes) {'))

		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateObjectWhenSomeValuesAreNull() {
		val code = '''		
			type One:
				oneField string (1..1)
			
			type Test:
				rosettaObjectListField One (1..*)
				rosettaObjectField One (1..1)
				stringField string (1..1)
			
		'''.generateCode

		val classes = code.compileToClasses

		val testClass = classes.get(rootPackage + '.Test')
		val testBuilderInstance = testClass.getMethod('builder').invoke(null)

		testBuilderInstance.class.getMethod('setStringField', String).invoke(testBuilderInstance, 'test-value'); 
		val testClassInstance = testBuilderInstance.class.getMethod('build').invoke(testBuilderInstance);
		
		val stringFieldValue = testClassInstance.class.getMethod('getStringField').invoke(testClassInstance)
		assertThat("Test object build with null values", stringFieldValue, is('test-value'))
		
		val testClassBuilderFromInstance = testClass.getMethod('toBuilder').invoke(testClassInstance)
		val stringFieldValueFromInstance = testClassBuilderFromInstance.class.getMethod('getStringField').invoke(testClassBuilderFromInstance)
		assertThat(stringFieldValueFromInstance, is('test-value'))
	}
	
	@Test
	def void shouldGenerateObjectWithMethodToReturnItsStateAsBuilder() {
		val code = '''
			type RosettaObject:
				rosettaField string (1..1)
			
			type Parent:
				parentField RosettaObject (1..*)
				anotherParentField RosettaObject (1..1)
			
			type Child extends Parent:
				childField RosettaObject (1..1)
				anotherChildField RosettaObject (1..*)
			
		'''.generateCode
		
		val classes = code.compileToClasses
		
		val rosettaObjectClass = classes.get(rootPackage + '.RosettaObject')
		val rosettaObjctBuilder = rosettaObjectClass.getMethod('builder').invoke(null)
		
		// Create RosettaObject instance for Parent
		rosettaObjctBuilder.class.getMethod('setRosettaField', String).invoke(rosettaObjctBuilder, 'test-value-parent')
		val rosettaObjectParent = rosettaObjctBuilder.class.getMethod('build').invoke(rosettaObjctBuilder)
		
		// Create RosettaObject instance for Child
		rosettaObjctBuilder.class.getMethod('setRosettaField', String).invoke(rosettaObjctBuilder, 'test-value-child')
		val rosettaObjectChild= rosettaObjctBuilder.class.getMethod('build').invoke(rosettaObjctBuilder)
		
		// Build Child object
		val childClass = classes.get(rootPackage + '.Child')
		val childBuilder = childClass.getMethod('builder').invoke(null)
		childBuilder.class.getMethod('setChildField', rosettaObjectClass).invoke(childBuilder, rosettaObjectChild)
		childBuilder.class.getMethod('addParentField', rosettaObjectClass).invoke(childBuilder, rosettaObjectParent)
		val childInstance = childBuilder.class.getMethod('build').invoke(childBuilder)
		
		val childFieldValue = childInstance.class.getMethod('getChildField').invoke(childInstance)
		val parentFieldValue = childInstance.class.getMethod('getParentField').invoke(childInstance)
		
		// This is a hack, to get the assert later on, to behave correctly
		// TODO: give more information to compiler, to tell it parentFieldValue is arraylist of rosettaObjectClass
		val childParentValue = newArrayList(rosettaObjectParent)
		
		assertThat("childField is stamped correctly", childFieldValue, is(rosettaObjectChild))
		assertThat('parentField is stamped correctly', parentFieldValue, is(childParentValue))
	}

}
