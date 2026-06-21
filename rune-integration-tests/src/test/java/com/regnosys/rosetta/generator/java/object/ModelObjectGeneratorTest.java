package com.regnosys.rosetta.generator.java.object;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.records.Date;
import com.rosetta.util.DottedPath;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class ModelObjectGeneratorTest {

	@Inject
	private ModelHelper modelHelper;
	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;

	@Test
	void testAttributeWithSameNameAsJavaKeyword() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type A:
					new string (0..1)

					condition Foo:
						new exists
				""");
		generatorTestHelper.compileToClasses(code);
	}

	@Test
	void testObjectReservedNames() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Path:
					name string (0..1)

					condition NameExists:
						name exists
				""");
		generatorTestHelper.compileToClasses(code);
	}

	@Test
	void useBuilderAddMultipleTimes() throws Exception {
		Map<String, Class<?>> classes = generatorTestHelper.compileJava8("""
				type Tester:
					items string (0..*)
				""");

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
	void generateStringBasicType() throws Exception {
		Map<String, Class<?>> classes = generatorTestHelper.compileJava8("""
				type Tester:
					one string (0..1)
					list string (0..*)
				""");

		assertEquals(String.class, classes.get(modelHelper.rootPackage() + ".Tester").getMethod("getOne").getReturnType());
	}

	@Test
	void generateIntBasicType() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Tester:
					one int (0..1)
					list int (0..*)
				""");
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		assertEquals(Integer.class, classes.get(modelHelper.rootPackage() + ".Tester").getMethod("getOne").getReturnType());
	}

	@Test
	void generateNumberBasicType() throws Exception {
		Map<String, Class<?>> classes = generatorTestHelper.compileJava8("""
				type Tester:
					one number (0..1)
					list number (0..*)
				""");

		assertEquals(BigDecimal.class, classes.get(modelHelper.rootPackage() + ".Tester").getMethod("getOne").getReturnType());
	}

	@Test
	void generateBooleanBasicType() throws Exception {
		Map<String, Class<?>> classes = generatorTestHelper.compileJava8("""
				type Tester:
					one boolean (0..1)
					list boolean (0..*)
				""");

		assertEquals(Boolean.class, classes.get(modelHelper.rootPackage() + ".Tester").getMethod("getOne").getReturnType());
	}

	@Test
	void generateDateBasicType() throws Exception {
		Map<String, Class<?>> classes = generatorTestHelper.compileJava8("""
				type Tester:
					one date (0..1)
					list date (0..*)
				""");
		assertEquals(Date.class, classes.get(modelHelper.rootPackage() + ".Tester").getMethod("getOne").getReturnType());
	}

	@Test
	void generateDateTimeBasicType() throws Exception {
		Map<String, Class<?>> classes = generatorTestHelper.compileJava8("""
				type Tester:
					one date (0..1)
					list date (0..*)
					zoned zonedDateTime (0..1)
				""");
		assertEquals(Date.class, classes.get(modelHelper.rootPackage() + ".Tester").getMethod("getOne").getReturnType());
		assertEquals(ZonedDateTime.class, classes.get(modelHelper.rootPackage() + ".Tester").getMethod("getZoned").getReturnType());
	}

	@Test
	void generateTimeBasicType() throws Exception {
		Map<String, Class<?>> classes = generatorTestHelper.compileJava8("""
				type Tester:
					one time (0..1)
					list time (0..*)
				""");
		assertEquals(LocalTime.class, classes.get(modelHelper.rootPackage() + ".Tester").getMethod("getOne").getReturnType());
	}

	@Test
	void shouldGenerateFunctioningJavaObjects() throws Exception {
		Map<String, Class<?>> classes = generatorTestHelper.compileJava8("""
				type TestObject: <"">
					fieldOne string (0..1) <"">
				""");
		Class<?> generatedClass = classes.get(modelHelper.rootPackage() + ".TestObject");
		Object builderInstance = generatedClass.getMethod("builder").invoke(null);
		Object inst = invoke(builderInstance, "prune");
		inst = invoke(builderInstance, "build");
		assertNull(invoke(inst, "getFieldOne"));

		invoke(builderInstance, "setFieldOne", "value");
		inst = invoke(builderInstance, "build");
		assertEquals("value", invoke(inst, "getFieldOne"));
	}

	@Test
	void shouldGenerateMetadFieldWhenAttributeSchemePresent() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type TestObject: <"">
					fieldOne string (0..1) [metadata scheme]
				""");
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);
		Class<?> generatedClass = classes.get(modelHelper.rootPackage() + ".TestObject");

		Method schemeMethod = generatedClass.getMethod("getFieldOne");
		assertThat(schemeMethod, CoreMatchers.notNullValue());

		Object builderInstance = generatedClass.getMethod("builder").invoke(null);
		Object metad = invoke(builderInstance, "getOrCreateFieldOne");
		invoke(metad, "setValue", "fieldOne");

		Object inst = invoke(builderInstance, "build");
		Object metadValue = invoke(inst, "getFieldOne");
		Object value = invoke(metadValue, "getValue");

		assertThat(value, is("fieldOne"));
	}

	@Test
	void shouldGenerateRosettaReferenceField() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type TestObject: <"">
					fieldOne Test2 (0..1)
						[metadata reference]

				type Test2:
					[metadata key]
				""");
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);
		Class<?> generatedClass = classes.get(modelHelper.rootPackage() + ".TestObject");

		Method schemeMethod = generatedClass.getMethod("getFieldOne");
		assertThat(schemeMethod, CoreMatchers.notNullValue());

		Object builderInstance = generatedClass.getMethod("builder").invoke(null);
		Object metad = invoke(builderInstance, "getOrCreateFieldOne");
		invoke(metad, "setExternalReference", "fieldOne");

		Object inst = invoke(builderInstance, "build");
		Object metadValue = invoke(inst, "getFieldOne");
		Object value = invoke(metadValue, "getExternalReference");

		assertThat(value, is("fieldOne"));
	}

	@Test
	void shouldGenerateBasicReferenceField() throws Exception {
		DottedPath namespace = DottedPath.splitOnDots("test.ns.basicref");
		Map<String, String> code = generatorTestHelper.generateCode(String.format("""
				namespace "%s"

				// import basic types
				import com.rosetta.test.model.*

				type TestObject: <"">
					fieldOne date (0..1) [metadata reference]
				""", namespace));
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);
		Class<?> generatedClass = classes.get(namespace.child("TestObject").withDots());

		Method schemeMethod = generatedClass.getMethod("getFieldOne");
		assertThat(schemeMethod, CoreMatchers.notNullValue());

		Object builderInstance = generatedClass.getMethod("builder").invoke(null);
		Object metad = invoke(builderInstance, "getOrCreateFieldOne");
		invoke(metad, "setExternalReference", "fieldOne");

		Object inst = invoke(builderInstance, "build");
		Object metadValue = invoke(inst, "getFieldOne");
		Object value = invoke(metadValue, "getExternalReference");

		assertThat(value, is("fieldOne"));
	}

	@Test
	void shouldCreateFieldWithReferenceTypeWhenAttributeIsReference() throws Exception {
		Map<String, Class<?>> generatedClass = generatorTestHelper.compileJava8("""

				type ComplexObject:
					[metadata key]

				type TestObject: <"">
					fieldOne ComplexObject (0..1)
						[metadata reference]
				""");

		Class<?> testClass = generatedClass.get(modelHelper.rootPackage() + ".TestObject");

		Method getter = testClass.getMethod("getFieldOne");
		assertThat(getter, CoreMatchers.notNullValue());
		assertThat(getter.getReturnType().getName(), is("com.rosetta.test.model.metafields.ReferenceWithMetaComplexObject"));
	}

	@Test
	void shouldGenerateTypeWithMetaFieldImport() throws Exception {
		DottedPath namespace = DottedPath.splitOnDots("test.ns.metafield");
		Map<String, String> code = generatorTestHelper.generateCode(String.format("""
				namespace "%s"
				version "test"

				// import basic types
				import com.rosetta.test.model.*

				type Foo:
					[metadata key]

					attr string (0..1)
				""", namespace));
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);
		Class<?> generatedClass = classes.get(namespace.child("Foo").withDots());

		Method schemeMethod = generatedClass.getMethod("getAttr");
		assertThat(schemeMethod, CoreMatchers.notNullValue());
	}

	@Test
	void shouldImplementGlobalKeyWhenDefined() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type WithGlobalKey:
					[metadata key]
					bar string (1..1)
				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);
		Class<?> withGlobalKeys = classes.get(modelHelper.rootPackage() + ".WithGlobalKey");

		assertThat(Arrays.stream(withGlobalKeys.getInterfaces())
				.anyMatch(i -> i.getName().equals("com.rosetta.model.lib.GlobalKey")), is(true));
	}

	@Test
	void shouldOmmitGlobalKeyAnnotationWhenNotDefined() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type AttributeGlobalKeyTest:
					withoutGlobalKey string (1..1)
				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);
		Class<?> testClass = classes.get(modelHelper.rootPackage() + ".AttributeGlobalKeyTest");
		boolean withoutGlobalKey = Arrays.stream(testClass.getMethod("getWithoutGlobalKey").getAnnotations())
				.anyMatch(a -> a.annotationType().getName().contains("GlobalKey"));

		assertThat(withoutGlobalKey, is(false));
	}

	@Test
	void shouldGenerateReferenceAttributeAsReference() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Foo:
					[metadata key]
					bar string (1..1)

				type AttributeGlobalKeyTest:
					withGlobalKey Foo (1..1) [metadata reference]
				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);
		Class<?> testClass = classes.get(modelHelper.rootPackage() + ".AttributeGlobalKeyTest");
		Method globalKeymethod = testClass.getMethod("getWithGlobalKey");
		Class<?> returnType = globalKeymethod.getReturnType();

		assertThat(returnType.getSimpleName().equals("ReferenceWithMetaFoo"), is(true));
	}

	@Test
	@Disabled
	void testGenerateClassList() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type A extends B:
					c C (1..*)

				type B:

				type C :
					one int (0..1)
					list int (0..*)


				type D:
					s string (1..*)
				""");
		Class<?> rosetta = generatorTestHelper.compileToClasses(code).get(modelHelper.rootPackage() + ".Rosetta");

		@SuppressWarnings("unchecked")
		List<Class<? extends RosettaModelObject>> rosettaClassList = (List<Class<? extends RosettaModelObject>>) rosetta
				.getMethod("classes").invoke(null);

		assertThat(rosettaClassList.stream().map(Class::getSimpleName).toList(),
				CoreMatchers.hasItems("A", "B", "C", "D"));
	}

	@Test
	void shouldExtendATypeWithSameAttribute() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Foo:
					a string (0..1)
					b string (0..1)

				type Bar extends Foo:
					override a string (0..1)
				""");
		generatorTestHelper.compileToClasses(code);
	}

	@Test
	void shouldSetAttributesOnEmptyClassWithInheritance() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Foo:
					attr string (0..1)

				type Bar extends Foo:
				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);
		Class<?> subclassInstance = classes.get(modelHelper.rootPackage() + ".Bar");

		// set the super class attribute
		Object builderInstance = subclassInstance.getMethod("builder").invoke(null);
		invoke(builderInstance, "setAttr", "blah");
		RosettaModelObject subclassInstance2 = (RosettaModelObject) invoke(builderInstance, "build");

		assertThat(invoke(subclassInstance2, "getAttr"), is("blah"));

		// use toBuilder method and rebuild, the attribute should still be set
		Object toBuilderInstance = subclassInstance2.toBuilder();
		RosettaModelObject subclassInstance3 = (RosettaModelObject) invoke(toBuilderInstance, "build");

		assertThat(invoke(subclassInstance3, "getAttr"), is("blah"));
	}

	@Test
	void isProductWithEnumValueRef() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				isProduct root Foo;

				enum Enum:
					A
					B

				type Foo:
					attr Enum (0..1)

				func Qualify_FooProd:
					[qualification Product]
					inputs: foo Foo (1..1)
					output: is_product boolean (1..1)
					set is_product:
						foo -> attr = Enum -> A
				""");

		generatorTestHelper.compileToClasses(code);
	}

	@Test
	void internalReferenceTest() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""

				type Foo:
					foo string (1..1)
						[metadata location]

				type Bar:
					bar string (1..1)
						[metadata address "pointsTo"=Foo->foo]

				""");
		Map<String, Class<?>> generatedClass = generatorTestHelper.compileToClasses(code);

		Class<?> barClass = generatedClass.get(modelHelper.rootPackage() + ".Bar");

		Method getter = barClass.getMethod("getBar");
		assertThat(getter, CoreMatchers.notNullValue());
		assertThat(getter.getReturnType().getName(), is("com.rosetta.model.metafields.ReferenceWithMetaString"));

		Class<?> fooClass = generatedClass.get(modelHelper.rootPackage() + ".Foo");
		Object builderInstance = fooClass.getMethod("builder").invoke(null);
		Object metad = invoke(builderInstance, "getOrCreateFoo");
		Object metas = invoke(metad, "getOrCreateMeta");
		@SuppressWarnings("unchecked")
		List<Integer> keys = (List<Integer>) invoke(metas, "getKey");
		assertThat(keys.size(), is(1));
	}

	@Disabled
	@Test
	void shouldNotCopyCertainFieldsIntoBuilder() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Foo globalKey
					attr string (0..1)
				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		Class<?> fooClass = classes.get(modelHelper.rootPackage() + ".Foo");

		// set the super class attribute
		Object fooBuilder = fooClass.getMethod("builder").invoke(null);
		invoke(fooBuilder, "setGlobalKey", "test-globalKey-value");
		RosettaModelObject foo = (RosettaModelObject) invoke(fooBuilder, "build");

		assertThat(invoke(foo, "getGlobalKey"), is("test-globalKey-value"));

		// use toBuilder method and rebuild, the attribute should still be set
		Object fooBuilder2 = foo.toBuilder();
		RosettaModelObject foo2 = (RosettaModelObject) invoke(fooBuilder2, "build");

		assertThat(invoke(foo2, "getGlobalKey"), CoreMatchers.nullValue());
	}

	@Test
	void shouldPruneListUnchanged() throws Exception {
		List<String> bar = createObjectWithListThenPruneAndReturnList(ImmutableList.of("a", "b"));
		assertNotNull(bar);
		assertEquals(2, bar.size());
	}

	private List<String> createObjectWithListThenPruneAndReturnList(List<String> list) throws Exception {
		Map<String, Class<?>> classes = generatorTestHelper.compileJava8("""
				type Foo:
					bar string (0..*)
				""");

		Map<String, Object> itemsToSet = ImmutableMap.of();
		Map<String, List<?>> itemsToAddToList = ImmutableMap.of("bar", list);
		RosettaModelObject fooInstance = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", itemsToSet,
				itemsToAddToList);
		Object prunedInstance = fooInstance.toBuilder().prune();
		@SuppressWarnings("unchecked")
		List<String> result = (List<String>) invoke(prunedInstance, "getBar");
		return result;
	}

	/**
	 * Invokes a method by name on the given receiver, mirroring the behaviour of
	 * Xtend's {@code ReflectExtensions.invoke}: it searches the class hierarchy for
	 * a method matching the name, argument count and assignable (boxed) parameter
	 * types, makes it accessible and invokes it.
	 */
	private static Object invoke(Object receiver, String methodName, Object... args) throws Exception {
		Class<?> clazz = receiver.getClass();
		while (clazz != null) {
			for (Method method : clazz.getDeclaredMethods()) {
				if (isCompatible(method, methodName, args)) {
					method.setAccessible(true);
					return method.invoke(receiver, args);
				}
			}
			clazz = clazz.getSuperclass();
		}
		throw new NoSuchMethodException(methodName);
	}

	private static boolean isCompatible(Method method, String name, Object... args) {
		if (!method.getName().equals(name)) {
			return false;
		}
		if (method.getParameterTypes().length != args.length) {
			return false;
		}
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			Object arg = args[i];
			Class<?> paramType = method.getParameterTypes()[i];
			if (paramType.isPrimitive()) {
				paramType = wrapperTypeFor(paramType);
			}
			if (arg != null && !paramType.isInstance(arg)) {
				return false;
			}
		}
		return true;
	}

	private static Class<?> wrapperTypeFor(Class<?> primitive) {
		if (primitive == boolean.class) {
			return Boolean.class;
		}
		if (primitive == byte.class) {
			return Byte.class;
		}
		if (primitive == char.class) {
			return Character.class;
		}
		if (primitive == short.class) {
			return Short.class;
		}
		if (primitive == int.class) {
			return Integer.class;
		}
		if (primitive == long.class) {
			return Long.class;
		}
		if (primitive == float.class) {
			return Float.class;
		}
		if (primitive == double.class) {
			return Double.class;
		}
		if (primitive == void.class) {
			return Void.class;
		}
		throw new IllegalArgumentException(primitive.getName());
	}
}
