package com.regnosys.rosetta.generator.java.object;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

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
public class ModelObjectBoilerPlateTest {

	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;
	@Inject
	private ModelHelper modelHelper;

	@Test
	void shouldGenerateObjectWithBoilerPlate() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Test:
					testField string (1..1)
				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		Class<?> thisTestClass = classes.get(modelHelper.rootPackage() + ".Test");
		Object thisTestBuilder = thisTestClass.getMethod("builder").invoke(null);
		thisTestBuilder.getClass().getMethod("setTestField", String.class).invoke(thisTestBuilder, "test-value");
		Object thisTest = thisTestBuilder.getClass().getMethod("build").invoke(thisTestBuilder);

		Class<?> thatTestClass = classes.get(modelHelper.rootPackage() + ".Test");
		Object thatTestBuilder = thatTestClass.getMethod("builder").invoke(null);
		thatTestBuilder.getClass().getMethod("setTestField", String.class).invoke(thatTestBuilder, "test-value");
		Object thatTest = thatTestBuilder.getClass().getMethod("build").invoke(thatTestBuilder);

		assertThat(thisTest, is(thatTest));
		assertThat(thisTest.toString(), allOf(containsString("Test"), containsString("testField=test-value")));
	}

	@Test
	void shouldGenerateObjectBuilderWithBoilerPlate() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Test:
					testField string (1..1)
				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		Class<?> thisTestClass = classes.get(modelHelper.rootPackage() + ".Test");
		Object thisTestBuilder = thisTestClass.getMethod("builder").invoke(null);
		thisTestBuilder.getClass().getMethod("setTestField", String.class).invoke(thisTestBuilder, "test-value");

		Class<?> thatTestClass = classes.get(modelHelper.rootPackage() + ".Test");
		Object thatTestBuilder = thatTestClass.getMethod("builder").invoke(null);
		thatTestBuilder.getClass().getMethod("setTestField", String.class).invoke(thatTestBuilder, "test-value");

		assertThat(thisTestBuilder, is(thatTestBuilder));
		assertThat(thisTestBuilder.toString(),
				allOf(containsString("TestBuilder"), containsString("testField=test-value")));
	}

	@Test
	void shouldGenerateHashCodeForEnumsUsingClassName() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				enum TestEnum :
					TestEnumValue

				type ThatHasEnums:
					testEnum TestEnum (1..1)
					testEnums TestEnum (1..*)
				""");

		String thatHasEnumsClass = code.get(modelHelper.rootPackage() + ".ThatHasEnums");

		String singleHashCodeContribution = "_result = 31 * _result + (testEnum != null ? testEnum.getClass().getName().hashCode() : 0);";
		String multipleHashCodeContribution = "_result = 31 * _result + (testEnums != null ? testEnums.stream().map(Object::getClass).map(Class::getName).mapToInt(String::hashCode).sum() : 0);";

		assertThat(thatHasEnumsClass,
				allOf(containsString(singleHashCodeContribution), containsString(multipleHashCodeContribution)));
	}
}
