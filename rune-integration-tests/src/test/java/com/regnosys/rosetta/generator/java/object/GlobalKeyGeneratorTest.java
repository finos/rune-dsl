package com.regnosys.rosetta.generator.java.object;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class GlobalKeyGeneratorTest {

	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;
	@Inject
	private ModelHelper modelHelper;

	@Test
	void shouldGenerateGlobalKeyFieldAndGetterWhenSet() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type WithGlobalKey:
					[metadata key]
					foo string (1..1)
				""");
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);
		Class<?> withGlobalKey = classes.get(modelHelper.rootPackage() + ".WithGlobalKey");

		assertThat(declaredMethodNames(withGlobalKey), hasItem("getMeta"));
	}

	@Test
	void shouldNotGenerateFieldsAndGetterWhenNotDefined() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type WithoutGlobalKeys:
					foo string (1..1)
				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);
		Class<?> withoutGlobalKeys = classes.get(modelHelper.rootPackage() + ".WithoutGlobalKeys");

		assertThat(methodNames(withoutGlobalKeys), not(hasItem("getMeta")));
	}

	// TODO fails when the metaType is moved to annotations.rosetta or basictypes.rosetta (and removed from the code here).
	@Test
	void shouldGenerateGlobalReferenceField() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				metaType reference string

				type Foo:
					[metadata key]
					bar string (1..1)

				type Baz:
					foo Foo (1..1)
						[metadata reference]

					condition:
						foo -> reference = "reference"
				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		Class<?> foo = classes.get(modelHelper.rootPackage() + ".Foo");
		assertThat(declaredMethodNames(foo), hasItem("getBar"));
		assertThat(declaredMethodNames(foo), hasItem("getMeta"));

		Class<?> baz = classes.get(modelHelper.rootPackage() + ".Baz");
		assertThat(declaredMethodNames(baz), hasItem("getFoo"));
		Method fooMethod = baz.getMethod("getFoo");
		Class<?> returnType = fooMethod.getReturnType();
		assertThat(returnType.getSimpleName(), is("ReferenceWithMetaFoo"));
	}

	// TODO the path containing a reference should work - the path is the same apart from it starts at the attribute rather than the type level).
	// TODO fails when the metaType is moved to annotations.rosetta or basictypes.rosetta (and removed from the code here).
	@Test
	@Disabled
	void shouldGenerateGlobalReferenceField2() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				metaType reference string

				type Foo:
					[metadata key]
					bar string (1..1)

				type Baz:
					foo Foo (1..1)
						[metadata reference]

					condition:
						foo -> reference exists
				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		Class<?> foo = classes.get(modelHelper.rootPackage() + ".Foo");
		assertThat(declaredFieldNames(foo), hasItem("bar"));
		assertThat(declaredFieldNames(foo), hasItem("meta"));

		Class<?> baz = classes.get(modelHelper.rootPackage() + ".Baz");
		assertThat(declaredFieldNames(baz), hasItem("foo"));
		Method fooMethod = baz.getMethod("getFoo");
		Class<?> returnType = fooMethod.getReturnType();
		assertThat(returnType.getSimpleName(), is("ReferenceWithMetaFoo"));
	}

	private static List<String> declaredMethodNames(Class<?> clazz) {
		return Arrays.stream(clazz.getDeclaredMethods()).map(Method::getName).collect(Collectors.toList());
	}

	private static List<String> methodNames(Class<?> clazz) {
		return Arrays.stream(clazz.getMethods()).map(Method::getName).collect(Collectors.toList());
	}

	private static List<String> declaredFieldNames(Class<?> clazz) {
		return Arrays.stream(clazz.getDeclaredFields()).map(java.lang.reflect.Field::getName).collect(Collectors.toList());
	}
}
