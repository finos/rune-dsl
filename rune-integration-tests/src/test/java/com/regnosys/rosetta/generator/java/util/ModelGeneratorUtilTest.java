package com.regnosys.rosetta.generator.java.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ModelGeneratorUtilTest {

	@Inject
	private ModelHelper modelHelper;
	@Inject
	private ModelGeneratorUtil generatorUtil;

	private Data parseFoo(String model) {
		return modelHelper.parseRosetta(model).getElements().stream()
				.filter(Data.class::isInstance)
				.map(Data.class::cast)
				.filter(d -> "Foo".equals(d.getName()))
				.findFirst()
				.orElseThrow();
	}

	// The generated javadoc uses the platform line separator (`\r\n` on Windows), while the
	// expected text blocks always use `\n` (per the JLS), so normalize before comparing.
	private void assertJavadoc(String expected, CharSequence javaDoc) {
		assertEquals(expected, javaDoc.toString().replace("\r\n", "\n"));
	}

	@Test
	void testDocReferenceJavaDoc() {
		String model = """
				body Organisation Org1
				corpus Agreement Org1 "Agreement 1" Agr1

				segment name

				type Foo:
					[docReference Org1 Agr1 name "something" provision "some provision"]
					bar string (1..1)
				""";

		Data fooType = parseFoo(model);

		CharSequence javaDoc = generatorUtil.javadoc(fooType);

		String expected = """
				/**
				 *
				 * Body Org1
				 * Corpus Agreement Agr1 Agreement 1 \s
				 * name "something"
				 *
				 * Provision some provision
				 *
				 */
				""";

		assertJavadoc(expected, javaDoc);
	}

	@Test
	void testMultiDocReferenceJavaDoc() {
		String model = """
				body Organisation Org1
				corpus Agreement Org1 "Agreement 1" Agr1

				body Organisation Org2
				corpus View Org2 "View 2" Vw2

				segment name

				type Foo:
					[docReference Org1 Agr1 name "something" provision "some provision"]
					[docReference Org2 Vw2 name "something else" provision "some other provision"]

					bar string (1..1)
				""";

		Data fooType = parseFoo(model);

		CharSequence javaDoc = generatorUtil.javadoc(fooType);

		String expected = """
				/**
				 *
				 * Body Org1
				 * Corpus Agreement Agr1 Agreement 1 \s
				 * name "something"
				 *
				 * Provision some provision
				 *
				 *
				 * Body Org2
				 * Corpus View Vw2 View 2 \s
				 * name "something else"
				 *
				 * Provision some other provision
				 *
				 */
				""";

		assertJavadoc(expected, javaDoc);
	}

	@Test
	void testDocReferenceAndDefJavaDoc() {
		String model = """
				body Organisation Org1
				corpus Agreement Org1 "Agreement 1" Agr1

				segment name

				type Foo: <"Foo def 12345">
					[docReference Org1 Agr1 name "something" provision "some provision"]

					bar string (1..1)
				""";

		Data fooType = parseFoo(model);

		CharSequence javaDoc = generatorUtil.javadoc(fooType);

		String expected = """
				/**
				 * Foo def 12345
				 *
				 * Body Org1
				 * Corpus Agreement Agr1 Agreement 1 \s
				 * name "something"
				 *
				 * Provision some provision
				 *
				 */
				""";

		assertJavadoc(expected, javaDoc);
	}

	@Test
	void testDefJavaDoc() {
		String model = """
				type Foo: <"Foo def 12345">
					bar string (1..1)
				""";

		Data fooType = parseFoo(model);

		CharSequence javaDoc = generatorUtil.javadoc(fooType);

		String expected = """
				/**
				 * Foo def 12345
				 */
				""";

		assertJavadoc(expected, javaDoc);
	}

	@Test
	void testDocRefOnAttributeJavaDoc() {
		String model = """
				body Organisation Org1
				corpus Agreement Org1 "Agreement 1" Agr1

				body Organisation Org2
				corpus View Org2 "View 2" Vw2

				segment name

				type Foo:
					bar string (1..1) <"Foo def 12345">
					[docReference Org1 Agr1 name "something" provision "some provision"]
					[docReference Org2 Vw2 name "something else" provision "some other provision"]

				""";

		Attribute fooBarAttr = modelHelper.parseRosetta(model).getElements().stream()
				.filter(Data.class::isInstance)
				.map(Data.class::cast)
				.filter(d -> "Foo".equals(d.getName()))
				.flatMap(d -> d.getAttributes().stream())
				.filter(a -> "bar".equals(a.getName()))
				.findFirst()
				.orElseThrow();

		CharSequence javaDoc = generatorUtil.javadoc(fooBarAttr);

		String expected = """
				/**
				 * Foo def 12345
				 *
				 * Body Org1
				 * Corpus Agreement Agr1 Agreement 1 \s
				 * name "something"
				 *
				 * Provision some provision
				 *
				 *
				 * Body Org2
				 * Corpus View Vw2 View 2 \s
				 * name "something else"
				 *
				 * Provision some other provision
				 *
				 */
				""";

		assertJavadoc(expected, javaDoc);
	}

}
