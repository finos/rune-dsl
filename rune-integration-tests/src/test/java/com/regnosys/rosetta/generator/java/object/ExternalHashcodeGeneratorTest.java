package com.regnosys.rosetta.generator.java.object;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ExternalHashcodeGeneratorTest {

	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;
	@Inject
	private ModelHelper modelHelper;

	@Test
	void shouldGenerateExternalHashMethod() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				enum Enum: one

				type RosettaType:

				type PlainOldRosettaObject:
					basicTypE string (1..1)
					basicTypeList string (1..*)
					rosettaObject RosettaType (1..1)
					rosettaObjectList RosettaType (1..*)
					enumeration Enum (1..1)
					enumerationList Enum (1..*)
				""");
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);
		Class<?> poro = classes.get(modelHelper.rootPackage() + ".PlainOldRosettaObject");

		assertThat(declaredMethodNames(poro), hasItem("process"));
	}

	@Test
	void shouldHandleSuperClass() {
		generatorTestHelper.compileToClasses(generatorTestHelper.generateCode("""
				type Super:
				type Sub extends Super:
					basicTypE string (1..1)
				"""));
	}

	@Test
	void shouldHandleEmptyClass() {
		generatorTestHelper.compileToClasses(generatorTestHelper.generateCode("""
				type Empty:
				"""));
	}

	@Test
	void shouldHandleGlobalKeys() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type WithGlobalKey:
					[metadata key]
					foo string (1..1)
				""");
		generatorTestHelper.compileToClasses(code);
	}

	@Test
	void shouldNotGenerateForEnums() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				enum Enum: foo
				""");

		generatorTestHelper.compileToClasses(code);
	}

	private static java.util.List<String> declaredMethodNames(Class<?> clazz) {
		return Arrays.stream(clazz.getDeclaredMethods()).map(Method::getName).collect(Collectors.toList());
	}
}
