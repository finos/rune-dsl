package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.Lists
import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.rosetta.model.lib.RosettaKeyValue
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.annotations.RosettaQualified
import com.rosetta.model.lib.annotations.RosettaSynonym
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.List
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.xbase.lib.util.ReflectExtensions
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.core.Is.is
import static org.junit.jupiter.api.Assertions.*
import static org.hamcrest.MatcherAssert.*
import com.rosetta.model.lib.GlobalKey

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaObjectGeneratorTest {

	@Inject extension ReflectExtensions
	@Inject extension ModelHelper
	@Inject extension CodeGeneratorTestHelper

	@Test
	def void useBuilderAddMultipleTimes() {
		val classes = '''
			class Tester
			{
				items string (0..*);
			}
		'''.compileJava8

		val classTester = classes.get(javaPackages.model.packageName + ".Tester")
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
	def void generateStringBasicType() {
		val classes = '''
			class Tester
			{
				one string (0..1);
				list string (0..*);
			}
		'''.compileJava8

		assertEquals(String, classes.get(javaPackages.model.packageName + ".Tester").getMethod('getOne').returnType)
	}

	@Test
	def void generateIntBasicType() {
		val code = '''
			class Tester
			{
				one int (0..1);
				list int (0..*);
			}
		'''.generateCode
		//code.writeClasses("intTest")
		val classes = code.compileToClasses

		assertEquals(Integer, classes.get(javaPackages.model.packageName + ".Tester").getMethod('getOne').returnType)

	}

	@Test
	def void generateNumberBasicType() {
		val classes = '''
			class Tester
			{
				one number (0..1);
				list number (0..*);
			}
		'''.compileJava8

		assertEquals(BigDecimal, classes.get(javaPackages.model.packageName + ".Tester").getMethod('getOne').returnType)

	}

	@Test
	def void generateBooleanBasicType() {
		val classes = '''
			class Tester
			{
				one boolean (0..1);
				list boolean (0..*);
			}
		'''.compileJava8

		assertEquals(Boolean, classes.get(javaPackages.model.packageName + ".Tester").getMethod('getOne').returnType)

	}

	@Test
	def void generateDateBasicType() {
		val classes = '''
			class Tester
			{
				one date (0..1);
				list date (0..*);
			}
		'''.compileJava8
		assertEquals(LocalDate, classes.get(javaPackages.model.packageName + ".Tester").getMethod('getOne').returnType)
	}

	@Test
	def void generateDateTimeBasicType() {
		val classes = '''
			class Tester
			{
				one dateTime (0..1);
				list dateTime (0..*);
				zoned zonedDateTime (0..1);
			}
		'''.compileJava8
		assertEquals(LocalDateTime,
			classes.get(javaPackages.model.packageName + ".Tester").getMethod('getOne').returnType)
		assertEquals(ZonedDateTime,
			classes.get(javaPackages.model.packageName + ".Tester").getMethod('getZoned').returnType)
	}

	@Test
	def void generateTimeBasicType() {
		val classes = '''
			class Tester
			{
				one time (0..1);
				list time (0..*);
			}
		'''.compileJava8
		assertEquals(LocalTime, classes.get(javaPackages.model.packageName + ".Tester").getMethod('getOne').returnType)
	}

	@Test
	def void shouldGenerateAbstractJavaClass() {
		val classes = '''
			abstract class AbstractTestObject <"">
			{
				fieldOne string (0..1) <"">;
			}
		'''.compileJava8
		val generatedClass = classes.get(javaPackages.model.packageName + ".AbstractTestObject")
		assertThat("java class is abstract", Modifier.isAbstract(generatedClass.modifiers), is(true))

		val generatedBuilderClass = generatedClass.declaredClasses.findFirst[name.contains('AbstractTestObjectBuilder')]
		assertThat("Object builder is also abstract", Modifier.isAbstract(generatedBuilderClass.modifiers), is(true))
		assertThat('Builder inherits from Base builder', generatedBuilderClass.superclass.name,
			containsString('RosettaModelObjectBuilder'))
	}

	@Test
	def void shouldGenerateFunctioningJavaObjects() {
		val classes = '''
			class TestObject <"">
			{
				fieldOne string (0..1) <"">;
			}
		'''.compileJava8
		val generatedClass = classes.get(javaPackages.model.packageName + ".TestObject")
		val builderInstance = generatedClass.getMethod("builder").invoke(null);
		var inst = builderInstance.invoke("prune");
		inst = builderInstance.invoke("build");
		assertNull(inst.invoke("getFieldOne"))
		//assertNull(inst)

		//inst.set("fieldOne", "value")
		builderInstance.invoke("setFieldOne", "value");
		inst = builderInstance.invoke("build");
		assertEquals("value", inst.invoke("getFieldOne"))
	}

	@Test
	def void shouldGenerateMetadFieldWhenAttributeSchemePresent() {
		val code = '''
			metaType scheme string


			class TestObject <"">
			{
				fieldOne string (0..1) scheme;
			}
		'''.generateCode
		//code.writeClasses("objectTest")
		val classes = code.compileToClasses
		val generatedClass = classes.get(javaPackages.model.packageName + ".TestObject")

		val schemeMethod = generatedClass.getMethod("getFieldOne")
		assertThat(schemeMethod, CoreMatchers.notNullValue())

		val builderInstance = generatedClass.getMethod("builder").invoke(null);
		val metad = builderInstance.invoke("getOrCreateFieldOne")
		metad.invoke("setValue","fieldOne")

		val inst = builderInstance.invoke("build");
		val metadValue = inst.invoke("getFieldOne")
		val value = metadValue.invoke("getValue")

		assertThat(value, is("fieldOne"))
	}
	
	@Test
	def void shouldGenerateRosettaReferenceFieldt() {
		val code = '''
			metaType reference string


			class TestObject <"">
			{
				fieldOne Test2 (0..1) reference;
			}
			
			class Test2 {
				
			}
		'''.generateCode
		//code.writeClasses("objectTest")
		val classes = code.compileToClasses
		val generatedClass = classes.get(javaPackages.model.packageName + ".TestObject")

		val schemeMethod = generatedClass.getMethod("getFieldOne")
		assertThat(schemeMethod, CoreMatchers.notNullValue())

		val builderInstance = generatedClass.getMethod("builder").invoke(null);
		val metad = builderInstance.invoke("getOrCreateFieldOne")
		metad.invoke("setExternalReference","fieldOne")

		val inst = builderInstance.invoke("build");
		val metadValue = inst.invoke("getFieldOne")
		val value = metadValue.invoke("getExternalReference")

		assertThat(value, is("fieldOne"))
	}
	
	@Test
	def void shouldGenerateBasicReferenceField() {
		val code = '''
			metaType reference string


			class TestObject <"">
			{
				fieldOne date (0..1) reference;
			}
		'''.generateCode
		//code.writeClasses("BasicReferenceTest")
		val classes = code.compileToClasses
		val generatedClass = classes.get(javaPackages.model.packageName + ".TestObject")

		val schemeMethod = generatedClass.getMethod("getFieldOne")
		assertThat(schemeMethod, CoreMatchers.notNullValue())

		val builderInstance = generatedClass.getMethod("builder").invoke(null);
		val metad = builderInstance.invoke("getOrCreateFieldOne")
		metad.invoke("setExternalReference","fieldOne")

		val inst = builderInstance.invoke("build");
		val metadValue = inst.invoke("getFieldOne")
		val value = metadValue.invoke("getExternalReference")

		assertThat(value, is("fieldOne"))
	}

	@Test
	def void shouldCreateFieldWithReferenceTypeWhenAttributeIsReference() {
		val code = '''
			metaType reference string
			
			class ComplexObject {}
			
			class TestObject <"">
			{
				fieldOne ComplexObject (0..1) reference;
			}
		'''.generateCode
		//code.writeClasses("shouldCreateFieldWithReferenceTypeWhenAttributeIsReference")
		val generatedClass = code.compileToClasses

		val testClass = generatedClass.get(javaPackages.model.packageName + '.TestObject')

		val getter = testClass.getMethod("getFieldOne")
		assertThat(getter, CoreMatchers.notNullValue())
		assertThat(getter.returnType.name, is('com.rosetta.test.model.metafields.ReferenceWithMetaComplexObject'))
	}

	@Test
	def void shouldGenerateSchemeFieldWithSynonym() {
		val code = '''			
			metaType scheme string
			
			class TestObject <"">
			{
				one string (0..1) scheme;
					[synonym FpML value oneSyn meta oneScheme]
			}
		'''.generateCode
		//code.writeClasses("SchemeFieldWithSynonym")
		val generatedClass = code.compileToClasses
		val testClass = generatedClass.get(javaPackages.model.packageName + '.TestObject')
		val getter = testClass.getMethod("getOne")

		assertThat(getter.annotations.filter[RosettaSynonym.isAssignableFrom(class)].size, is(1))

		val annotation = getter.getAnnotation(RosettaSynonym)
		assertThat(annotation.value, is('oneSyn'))
	}

	@Test
	def void shouldImplementRosettaKeyValueWhenDefined() {
		val code = '''
			class WithRosettaKeyValue rosettaKeyValue {
				bar string (1..1);
			}
		'''.generateCode

		val classes = code.compileToClasses
		val WithRosettaKeyValue = classes.get(javaPackages.model.packageName + '.WithRosettaKeyValue')

		assertThat(WithRosettaKeyValue.interfaces.exists[name.equals('com.rosetta.model.lib.GlobalKey')], is(false))
		assertThat(WithRosettaKeyValue.interfaces.exists[name.equals('com.rosetta.model.lib.RosettaKeyValue')],
			is(true))
	}

	@Test
	def void shouldImplementRosettaKeyAndRosettaKeyValueWhenDefined() {
		val code = '''
			class WithRosettaKeys key rosettaKeyValue {
				bar string (1..1);
			}
		'''.generateCode
		//code.writeClasses("shouldImplementRosettaKeyAndRosettaKeyValueWhenDefined")

		val classes = code.compileToClasses
		val withRosettaKeys = classes.get(javaPackages.model.packageName + '.WithRosettaKeys')

		assertThat(withRosettaKeys.interfaces.exists[name.equals('com.rosetta.model.lib.GlobalKey')], is(true))
		assertThat(withRosettaKeys.interfaces.exists[name.equals('com.rosetta.model.lib.RosettaKeyValue')], is(true))
	}

	@Test
	def void shouldOmmitRosettaKeyAnnotationWhenNotDefined() {
		val code = '''
			class AttributeRosettaKeyTest {
				withoutRosettaKey string (1..1);
			}
		'''.generateCode

		val classes = code.compileToClasses
		val testClass = classes.get(javaPackages.model.packageName + '.AttributeRosettaKeyTest')
		val withoutRosettaKey = testClass.getMethod("getWithoutRosettaKey").annotations.exists [
			annotationType.name.contains('RosettaKey')
		]

		assertThat(withoutRosettaKey, is(false))
	}

	@Test
	def void shouldGenerateReferenceAttributeAsReference() {
		val code = '''
			metaType reference string
			
			class Foo key {
				bar string (1..1);
			}
			
			class AttributeRosettaKeyTest {
				withRosettaKey Foo (1..1) reference;
			}
		'''.generateCode
		//code.writeClasses("shouldGenerateRosettaKeyAttributeAsString")

		val classes = code.compileToClasses
		val testClass = classes.get(javaPackages.model.packageName + '.AttributeRosettaKeyTest')
		val rosettaKeymethod = testClass.getMethod("getWithRosettaKey")
		val returnType = rosettaKeymethod.returnType

		assertThat(returnType.simpleName.equals("ReferenceWithMetaFoo"), is(true))
	}

	@Test
	@Disabled
	def void testGenerateClassList() {
		val code = '''
			class A extends B
			{
				c C (1..*);
			}
			
			class B { }
			
			class C {
				one int (0..1);
				list int (0..*);
			}
			
			class D {
				s string (1..*);
			}
			
		'''.generateCode
		// val classList = code.get(javaPackages.model.packageName + '.Rosetta')
		// println(classList)
		val rosetta = code.compileToClasses.get(javaPackages.model.packageName + '.Rosetta')

		val rosettaClassList = rosetta.getMethod("classes").invoke(null) as List<Class<? extends RosettaModelObject>>

		assertThat(rosettaClassList.map[simpleName], hasItems('A', 'B', 'C', 'D'))
	}

	@Disabled @Test // Move to rosetta-cdm project
	def void shouldExcludeMetaDataFromRosettaKeyValueHashCode() {
		// only fields "a" and "b" should be included in the rosettaKeyValue hash
		val code = '''
			isProduct root Foo;
			isEvent root Foo;
			
			metaType scheme string
			metaType id string
			metaType reference string
			
			class Foo rosettaKey rosettaKeyValue {
				id (0..1);
				//reference;
				a string (1..1);
				b string (1..1) scheme;
				e productType (1..1);
				f eventType (1..1);
				g calculation (1..1);
				eventEffect string (1..1);
			}
		'''.generateCode

		val classes = code.compileToClasses

		val input1 = newHashMap
		input1.put('a', '1')
		input1.put('b', '2')
		// input1.put('id', '3') // the anchor
		input1.put('e', '4')
		// input1.put('reference', '5') // reference
		input1.put('f', '6')
		input1.put('g', '7')
		input1.put('eventEffect', '8')
		val foo1 = classes.createInstanceUsingBuilder('Foo', input1, of())

		// all fields changed except for "a" and "b"
		val input2 = newHashMap
		input2.put('a', '1')
		input2.put('b', '2')
		// input2.put('id', '30') // the anchor
		input2.put('e', '40')
		// input2.put('reference', '50') // reference
		input2.put('f', '60')
		input2.put('g', '70')
		input2.put('eventEffect', '80')
		val foo2 = classes.createInstanceUsingBuilder('Foo', input2, of())

		assertThat("RosettaKeyValue should match because only meta data has changed",
			(foo1 as RosettaKeyValue).rosettaKeyValue, is((foo2 as RosettaKeyValue).rosettaKeyValue))
		assertThat("RosettaKey should not match because meta data has changed", (foo1 as GlobalKey).meta.getGlobalKey,
			not(is((foo2 as GlobalKey).meta.getGlobalKey)))
	}

	@Test
	def shouldGenerateRosettaQualifiedAnnotationForProductType() {
		val code = '''
			isProduct root Foo;
			
			class Foo {
				a string (0..1);
			}
			
			class Bar {
				b productType (0..1);
			}
		'''.generateCode
		val classes = code.compileToClasses
		val testClass = classes.get(javaPackages.model.packageName + '.Bar')
		val annotation = testClass.getAnnotation(RosettaQualified)

		assertNotNull(annotation)
		assertThat(annotation.attribute, is('b'))
		assertThat(annotation.qualifiedClass.simpleName, is('Foo'))
	}

	@Test
	def shouldGenerateRosettaQualifiedAnnotationForEventType() {
		val code = '''
			isEvent root Foo;
			
			class Foo {
				a string (0..1);
			}
			
			class Bar {
				b eventType (0..1);
			}
		'''.generateCode
		val classes = code.compileToClasses
		val testClass = classes.get(javaPackages.model.packageName + '.Bar')
		val annotation = testClass.getAnnotation(RosettaQualified)

		assertNotNull(annotation)
		assertThat(annotation.attribute, is('b'))
		assertThat(annotation.qualifiedClass.simpleName, is('Foo'))
	}

	@Test
	def shouldGenerateRosettaCalculationTypeAsString() {
		val code = '''
			class Foo {
				bar calculation (0..1);
			}
		'''.generateCode
		val classes = code.compileToClasses
		val testClass = classes.get(javaPackages.model.packageName + '.Foo')

		assertEquals(String, testClass.getMethod("getBar").returnType)
	}

	@Test
	def void shouldSetAttributesOnEmptyClassWithInheritance() {
		val code = '''
			class Foo
			{
				attr string (0..1);
			}
			
			class Bar extends Foo { }
		'''.generateCode

		val classes = code.compileToClasses

		val subclassInstance = classes.get(javaPackages.model.packageName + '.Bar')

		// set the super class attribute
		val builderInstance = subclassInstance.getMethod("builder").invoke(null)
		builderInstance.invoke("setAttr", "blah")
		val subclassInstance2 = builderInstance.invoke("build") as RosettaModelObject

		assertThat(subclassInstance2.invoke("getAttr"), is('blah'))

		// use toBuilder method and rebuild, the attribute should still be set
		val toBuilderInstance = subclassInstance2.toBuilder();
		val subclassInstance3 = toBuilderInstance.invoke("build") as RosettaModelObject

		assertThat(subclassInstance3.invoke("getAttr"), is('blah'))
	}

	@Disabled @Test
	def void shouldNotCopyCertainFieldsIntoBuilder() {
		val code = '''
			class Foo rosettaKey
			{
				attr string (0..1);
			}
		'''.generateCode

		val classes = code.compileToClasses

		val fooClass = classes.get(javaPackages.model.packageName + '.Foo')

		// set the super class attribute
		val fooBuilder = fooClass.getMethod("builder").invoke(null)
		fooBuilder.invoke("setRosettaKey", "test-rosettaKey-value")
		val foo = fooBuilder.invoke("build") as RosettaModelObject

		assertThat(foo.invoke("getRosettaKey"), is('test-rosettaKey-value'))
		
		// use toBuilder method and rebuild, the attribute should still be set
		val fooBuilder2 = foo.toBuilder();
		val foo2 = fooBuilder2.invoke("build") as RosettaModelObject

		assertThat(foo2.invoke("getRosettaKey"), nullValue())
	}
}
