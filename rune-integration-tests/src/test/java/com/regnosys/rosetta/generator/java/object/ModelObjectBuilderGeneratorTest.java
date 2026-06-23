package com.regnosys.rosetta.generator.java.object;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.Lists;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class ModelObjectBuilderGeneratorTest {

	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;
	@Inject
	private ModelHelper modelHelper;

	@Test
	void shouldGenerateJavaBuilderThatExtendsParentBuilders() throws Exception {
		Map<String, String> generated = generatorTestHelper.generateCode("""
				type A:
					aa string (0..1)

				type B extends A:
					bb string (0..1)

				type C extends B:
					cc string (0..1)

				type D extends C:
					dd string (0..1)
					dd2 string (0..1)

				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(generated);

		Class<?> classD = classes.get(modelHelper.rootPackage() + ".D");
		Object classDBuilderInstance = classD.getMethod("builder").invoke(null);

		classDBuilderInstance.getClass().getMethod("setAa", String.class).invoke(classDBuilderInstance, "fieldA");
		classDBuilderInstance.getClass().getMethod("setBb", String.class).invoke(classDBuilderInstance, "fieldB");
		classDBuilderInstance.getClass().getMethod("setCc", String.class).invoke(classDBuilderInstance, "fieldC");
		classDBuilderInstance.getClass().getMethod("setDd", String.class).invoke(classDBuilderInstance, "fieldD");

		Object classDInstance = classDBuilderInstance.getClass().getMethod("build").invoke(classDBuilderInstance);

		assertEquals("D {dd=fieldD, dd2=null} C {cc=fieldC} B {bb=fieldB} A {aa=fieldA}", classDInstance.toString());
	}

	@Test
	void useBuilderAddMultipleTimes() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Tester:
					items string (0..*)

				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		Class<?> classTester = classes.get(modelHelper.rootPackage() + ".Tester");
		Object classTesterBuilderInstance = classTester.getMethod("builder").invoke(null);

		classTesterBuilderInstance.getClass().getMethod("addItems", String.class).invoke(classTesterBuilderInstance, "item1");
		classTesterBuilderInstance.getClass().getMethod("addItems", String.class).invoke(classTesterBuilderInstance, "item2");
		classTesterBuilderInstance.getClass().getMethod("addItems", String.class).invoke(classTesterBuilderInstance, "item3");

		Object classTesterInstance = classTesterBuilderInstance.getClass().getMethod("build").invoke(classTesterBuilderInstance);

		@SuppressWarnings("unchecked")
		List<String> items = (List<String>) classTesterInstance.getClass().getMethod("getItems").invoke(classTesterInstance);

		assertEquals(Lists.newArrayList("item1", "item2", "item3"), items);
	}

	@Test
	void shouldGenerateObjectUsingBuilderOfBuildersPattern() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				enum TestEnum:
					testEnumValueOne

				type One:
					oneField string (1..1)

				type Test:
					multipleEnums TestEnum (1..*)
					multipleOnes One (1..*)
					singleOne One (1..1)

				""");

		String testClassCode = code.get(modelHelper.rootPackage() + ".Test");

		// Base Case
		assertThat(testClassCode, containsString(
				"this.multipleOnes = ofNullable(builder.getMultipleOnes()).filter(_l->!_l.isEmpty()).map(list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(ImmutableList.toImmutableList())).orElse(null);"));

		// Only do this for attributes that are RosettaClass
		assertThat(testClassCode, not(containsString("this.multipleEnums = builder.multipleEnums.stream()")));

		// Builder contains builders of RosettaClasses
		assertThat(testClassCode, containsString("List<? extends One> multipleOnes;"));
		assertThat(testClassCode, containsString("One.OneBuilder singleOne;"));

		// Builder setters handles adding builder types
		assertThat(testClassCode, containsString("public Test.TestBuilder setSingleOne(One _singleOne) {"));
		assertThat(testClassCode, containsString("public Test.TestBuilder addMultipleOnes(One _multipleOnes) {"));

		generatorTestHelper.compileToClasses(code);
	}

	@Test
	void shouldGenerateObjectWhenSomeValuesAreNull() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type One:
					oneField string (1..1)

				type Test:
					rosettaObjectListField One (1..*)
					rosettaObjectField One (1..1)
					stringField string (1..1)

				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		Class<?> testClass = classes.get(modelHelper.rootPackage() + ".Test");
		Object testBuilderInstance = testClass.getMethod("builder").invoke(null);

		testBuilderInstance.getClass().getMethod("setStringField", String.class).invoke(testBuilderInstance, "test-value");
		Object testClassInstance = testBuilderInstance.getClass().getMethod("build").invoke(testBuilderInstance);

		Object stringFieldValue = testClassInstance.getClass().getMethod("getStringField").invoke(testClassInstance);
		assertThat("Test object build with null values", stringFieldValue, is("test-value"));

		Object testClassBuilderFromInstance = testClass.getMethod("toBuilder").invoke(testClassInstance);
		Object stringFieldValueFromInstance = testClassBuilderFromInstance.getClass().getMethod("getStringField")
				.invoke(testClassBuilderFromInstance);
		assertThat(stringFieldValueFromInstance, is("test-value"));
	}

	@Test
	void shouldGenerateObjectWithMethodToReturnItsStateAsBuilder() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type RosettaObject:
					rosettaField string (1..1)

				type Parent:
					parentField RosettaObject (1..*)
					anotherParentField RosettaObject (1..1)

				type Child extends Parent:
					childField RosettaObject (1..1)
					anotherChildField RosettaObject (1..*)

				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		Class<?> rosettaObjectClass = classes.get(modelHelper.rootPackage() + ".RosettaObject");
		Object rosettaObjctBuilder = rosettaObjectClass.getMethod("builder").invoke(null);

		// Create RosettaObject instance for Parent
		rosettaObjctBuilder.getClass().getMethod("setRosettaField", String.class).invoke(rosettaObjctBuilder, "test-value-parent");
		Object rosettaObjectParent = rosettaObjctBuilder.getClass().getMethod("build").invoke(rosettaObjctBuilder);

		// Create RosettaObject instance for Child
		rosettaObjctBuilder.getClass().getMethod("setRosettaField", String.class).invoke(rosettaObjctBuilder, "test-value-child");
		Object rosettaObjectChild = rosettaObjctBuilder.getClass().getMethod("build").invoke(rosettaObjctBuilder);

		// Build Child object
		Class<?> childClass = classes.get(modelHelper.rootPackage() + ".Child");
		Object childBuilder = childClass.getMethod("builder").invoke(null);
		childBuilder.getClass().getMethod("setChildField", rosettaObjectClass).invoke(childBuilder, rosettaObjectChild);
		childBuilder.getClass().getMethod("addParentField", rosettaObjectClass).invoke(childBuilder, rosettaObjectParent);
		Object childInstance = childBuilder.getClass().getMethod("build").invoke(childBuilder);

		Object childFieldValue = childInstance.getClass().getMethod("getChildField").invoke(childInstance);
		Object parentFieldValue = childInstance.getClass().getMethod("getParentField").invoke(childInstance);

		// This is a hack, to get the assert later on, to behave correctly
		// TODO: give more information to compiler, to tell it parentFieldValue is arraylist of rosettaObjectClass
		List<Object> childParentValue = Lists.newArrayList(rosettaObjectParent);

		assertThat("childField is stamped correctly", childFieldValue, is(rosettaObjectChild));
		assertThat("parentField is stamped correctly", parentFieldValue, is(childParentValue));
	}
}
