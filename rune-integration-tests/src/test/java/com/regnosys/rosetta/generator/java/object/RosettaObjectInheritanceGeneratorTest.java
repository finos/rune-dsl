package com.regnosys.rosetta.generator.java.object;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

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
class RosettaObjectInheritanceGeneratorTest {

	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;
	@Inject
	private ModelHelper modelHelper;

	@Test
	void shouldGenerateJavaClassWithMultipleParents() {
		Map<String, String> generated = generatorTestHelper.generateCode("""
				type A:
					aa string (0..1)

				type B extends A:
					bb string (0..1)

				type C extends B:
					cc string (0..1)

				type D extends C:
					dd string (0..1)

				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(generated);

		Class<?> classA = classes.get(modelHelper.rootPackage() + ".A");
		Class<?> classB = classes.get(modelHelper.rootPackage() + ".B");
		Class<?> classC = classes.get(modelHelper.rootPackage() + ".C");
		Class<?> classD = classes.get(modelHelper.rootPackage() + ".D");

		assertTrue(classC.isAssignableFrom(classD));
		assertTrue(classB.isAssignableFrom(classC));
		assertTrue(classA.isAssignableFrom(classB));
	}

	@Disabled // override is deprecated
	@Test
	void shouldGenerateJavaClassWithOverridenAttributesAcrossNamespaces() {
		// The two model files are passed in reverse order: this test only works if
		// the order of the "files" is the opposite from that provided.
		Map<String, String> generated = generatorTestHelper.generateCode("""
				namespace "original"

				type A:
					aa string (0..1)

				type Top:
					aField A (0..1)
				""", """
				namespace "extending"

				import original.*

				type A extends original.A:
					bb string (0..1)

				type Top extends original.Top:
					override aField A (0..1)
				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(generated);
		Class<?> extendingTop = classes.get("extending.Top");
		assertThrows(NoSuchFieldException.class, () -> extendingTop.getDeclaredField("aField"));
	}

	@Disabled // override is deprecated
	@Test
	void shouldGenerateJavaClassWithOverridenListAttributesAcrossNamespaces() {
		// The two model files are passed in reverse order: this test only works if
		// the order of the "files" is the opposite from that provided.
		Map<String, String> generated = generatorTestHelper.generateCode("""
				namespace "original"
				type A:
					aa string (0..1)

				type Top:
					aField A (0..*)
				""", """
				namespace "extending"

				import original.*

				type A extends original.A:
					bb string (0..1)

				type Top extends original.Top:
					override aField A (0..*)
				""");

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(generated);
		Class<?> extendingTop = classes.get("extending.Top");
		assertThrows(NoSuchFieldException.class, () -> extendingTop.getDeclaredField("aField"));
	}

	@Disabled
	@Test
	void shouldGenerateJavaClassWithConditionsListAttributesAcrossNamespaces() {
		// The two model files are passed in reverse order: this test only works if
		// the order of the "files" is the opposite from that provided.
		Map<String, String> generated = generatorTestHelper.generateCode("""
				namespace "original"
				type A:
					b B (0..1)

				type B:
					c C (0..1)

				type C:
					cField1 string (0..*)
				""", """
				namespace "extending"

				import original.*

				type A extends original.A:
					override b B (0..1)

				type B extends original.B:
					override c C (0..1)

				type C extends original.C:
					cField2 string (0..1)
					cField3 B (0..1)

					 condition cField3Exists: cField3 -> c exists

				""");

		generatorTestHelper.compileToClasses(generated);
	}
}
