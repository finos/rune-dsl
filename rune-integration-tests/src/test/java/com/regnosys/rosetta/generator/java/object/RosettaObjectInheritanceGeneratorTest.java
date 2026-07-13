package com.regnosys.rosetta.generator.java.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.rosetta.model.lib.RosettaModelObjectBuilder;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class RosettaObjectInheritanceGeneratorTest {

	@Inject
	private RosettaTestModelService testModelService;

	@Test
	void shouldGenerateJavaClassWithMultipleParents() {
		JavaTestModel model = testModelService.toJavaTestModel("""
				type A:
					aa string (0..1)

				type B extends A:
					bb string (0..1)

				type C extends B:
					cc string (0..1)

				type D extends C:
					dd string (0..1)
				""").compile();

		Class<?> classA = model.getTypeJavaClass("A");
		Class<?> classB = model.getTypeJavaClass("B");
		Class<?> classC = model.getTypeJavaClass("C");
		Class<?> classD = model.getTypeJavaClass("D");

		assertTrue(classC.isAssignableFrom(classD));
		assertTrue(classB.isAssignableFrom(classC));
		assertTrue(classA.isAssignableFrom(classB));
	}

	@Disabled // override is deprecated
	@Test
	void shouldGenerateJavaClassWithOverridenAttributesAcrossNamespaces() {
		// The extending model is passed as the primary source, so that `getTypeJavaClass`
		// resolves `Top` to `extending.Top`.
		JavaTestModel model = testModelService.toJavaTestModel("""
				namespace "extending"

				import original.*

				type A extends original.A:
					bb string (0..1)

				type Top extends original.Top:
					override aField A (0..1)
				""", """
				namespace "original"

				type A:
					aa string (0..1)

				type Top:
					aField A (0..1)
				""").compile();

		Class<?> extendingTop = model.getTypeJavaClass("Top");
		assertThrows(NoSuchFieldException.class, () -> extendingTop.getDeclaredField("aField"));
	}

	@Disabled // override is deprecated
	@Test
	void shouldGenerateJavaClassWithOverridenListAttributesAcrossNamespaces() {
		// The extending model is passed as the primary source, so that `getTypeJavaClass`
		// resolves `Top` to `extending.Top`.
		JavaTestModel model = testModelService.toJavaTestModel("""
				namespace "extending"

				import original.*

				type A extends original.A:
					bb string (0..1)

				type Top extends original.Top:
					override aField A (0..*)
				""", """
				namespace "original"

				type A:
					aa string (0..1)

				type Top:
					aField A (0..*)
				""").compile();

		Class<?> extendingTop = model.getTypeJavaClass("Top");
		assertThrows(NoSuchFieldException.class, () -> extendingTop.getDeclaredField("aField"));
	}

	@Disabled
	@Test
	void shouldGenerateJavaClassWithConditionsListAttributesAcrossNamespaces() {
		testModelService.toJavaTestModel("""
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
				""", """
				namespace "original"

				type A:
					b B (0..1)

				type B:
					c C (0..1)

				type C:
					cField1 string (0..*)
				""").compile();
	}

	@Test
	void shouldNotThrowWhenReadingAnAttributeOverriddenToZeroCardinality() throws Exception {
		// See https://github.com/finos/rune-dsl/issues/1321
		// When a child type overrides a multi-cardinality parent attribute with a zero (single)
		// cardinality, the attribute is stored as a single, always-null field. The builder getter
		// that adapts this single field back to the parent's list type used to wrap the field
		// unconditionally (`Collections.singletonList(datedValue.toBuilder())`), throwing an NPE
		// whenever an inherited condition read the attribute. The getter must instead be null-safe
		// and return an empty list for the absent attribute.
		JavaTestModel model = testModelService.toJavaTestModel("""
				type DatedValue:
					x int (0..1)

				type Schedule:
					datedValue DatedValue (0..*)

					condition DatedValueExists:
						datedValue exists

				type Quantity extends Schedule:
					override datedValue DatedValue (0..0)
				""").compile();

		Class<?> quantityClass = model.getTypeJavaClass("Quantity");
		RosettaModelObjectBuilder builder = (RosettaModelObjectBuilder) quantityClass.getMethod("builder").invoke(null);
		List<?> datedValue = (List<?>) builder.getClass().getMethod("getDatedValue").invoke(builder);
		assertEquals(List.of(), datedValue);
	}
}
