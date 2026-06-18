package com.regnosys.rosetta.generator.java.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class RosettaProcessorTest {
	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;

	private void assertProcessEquals(String expected, RosettaModelObject rmo) {
		RosettaAttributePathProcessor fooProcessor = new RosettaAttributePathProcessor();
		rmo.process(RosettaPath.valueOf("ROOT"), fooProcessor);
		assertEquals(expected, fooProcessor.getResult().stream()
				.map(Object::toString)
				.collect(Collectors.joining("\n")) + "\n");
	}

	@Test
	void processFlatType() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Foo:
					attr1 int (0..1)
					attr2 string (0..2)
					attr3 int (1..1)
				""");
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		Map<String, Object> values1 = new LinkedHashMap<>();
		values1.put("attr1", 42);
		values1.put("attr2", List.of("A", "B"));
		values1.put("attr3", 0);
		RosettaModelObject foo1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", values1);
		assertProcessEquals("""
				ROOT.attr1
				ROOT.attr2
				ROOT.attr3
				""", foo1);

		Map<String, Object> values2 = new LinkedHashMap<>();
		values2.put("attr1", null);
		values2.put("attr2", List.of());
		values2.put("attr3", 0);
		RosettaModelObject foo2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", values2);
		assertProcessEquals("""
				ROOT.attr1
				ROOT.attr2
				ROOT.attr3
				""", foo2);
	}

	@Test
	void processNestedType() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Foo:
					attr1 int (0..1)
					attr2 Bar (0..2)
					attr3 Bar (1..1)

				type Bar:
					bar Bar (0..1)
				""");
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		Map<String, Object> innerBar = new LinkedHashMap<>();
		innerBar.put("bar", null);

		Map<String, Object> barWithNull = new LinkedHashMap<>();
		barWithNull.put("bar", null);

		Map<String, Object> barWithNested = new LinkedHashMap<>();
		barWithNested.put("bar", generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", innerBar));

		Map<String, Object> attr3Bar = new LinkedHashMap<>();
		attr3Bar.put("bar", null);

		Map<String, Object> fooValues = new LinkedHashMap<>();
		fooValues.put("attr1", 42);
		fooValues.put("attr2", List.of(
				generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", barWithNull),
				generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", barWithNested)));
		fooValues.put("attr3", generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", attr3Bar));
		RosettaModelObject foo = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", fooValues);
		assertProcessEquals("""
				ROOT.attr1
				ROOT.attr2
				ROOT.attr2(0).bar
				ROOT.attr2(1).bar
				ROOT.attr2(1).bar.bar
				ROOT.attr3
				ROOT.attr3.bar
				""", foo);
	}

	@Test
	void processTypeWithSupertype() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Foo:
					attr1 int (0..1)

				type Bar extends Foo:
					attr2 string (0..2)
					attr3 int (1..1)
				""");
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		Map<String, Object> values = new LinkedHashMap<>();
		values.put("attr1", 42);
		values.put("attr2", List.of("A", "B"));
		values.put("attr3", 0);
		RosettaModelObject bar = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", values);
		assertProcessEquals("""
				ROOT.attr1
				ROOT.attr2
				ROOT.attr3
				""", bar);
	}

	@Test
	void processTypeWithOverridenAttributes() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type A:
					a int (1..1)

				type Foo:
					attr1 int (0..1)
					attr2 A (0..2)
					attr3 string (1..1)

				type Bar extends Foo:
					override attr1 int (0..1)
					override attr2 A (0..2)
					attr4 int (1..1)
				""");
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		Map<String, Object> aValues = new LinkedHashMap<>();
		aValues.put("a", 42);

		Map<String, Object> barValues = new LinkedHashMap<>();
		barValues.put("attr1", 42);
		barValues.put("attr2", List.of(generatorTestHelper.createInstanceUsingBuilder(classes, "A", aValues)));
		barValues.put("attr3", "Bla");
		barValues.put("attr4", 0);
		RosettaModelObject bar = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", barValues);
		assertProcessEquals("""
				ROOT.attr1
				ROOT.attr2
				ROOT.attr2(0).a
				ROOT.attr3
				ROOT.attr4
				""", bar);
	}
}
