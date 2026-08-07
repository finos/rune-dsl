package com.regnosys.rosetta.generator.java.labels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.rosetta.model.lib.annotations.RuneLabelProvider;
import com.rosetta.model.lib.functions.LabelProvider;
import com.rosetta.model.lib.path.RosettaPath;

/**
 * End-to-end discovery test for type-rooted label providers: generates, compiles and loads a whole
 * model, then checks the two halves of the mechanism <i>together</i> — the {@code @RuneLabelProvider}
 * stamped on the pojo interface by {@code ModelObjectGenerator}, and the provider class emitted by
 * {@link LabelProviderGenerator}. Those are produced by two different generators off one shared gate,
 * so a drift between them would not fail either generator's own test: it would leave an annotation
 * pointing at a class that was never generated, i.e. a compile error in a downstream model build.
 * Asserting the annotation's value <i>is</i> the loaded provider class is what pins that shut.
 * <p>
 * The model deliberately contains no transform function and no report — a flat labelled type on its
 * own, which is the shape a CSV import produces and the case that generated nothing at all before
 * type-rooted providers existed.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class TypeLabelProviderDiscoveryTest {

	@Inject
	private RosettaTestModelService modelService;

	private JavaTestModel compileModel() {
		return modelService.toJavaTestModel("""
				namespace test

				type Foo:
					attr1 string (1..1)
						[label "Attr One"]
					attr2 int (1..1)
						[label "Attr Two"]
					attr3 number (1..1)

				type Bar:
					barAttr string (1..1)
				""").compile();
	}

	@Test
	void labelledTypePojoPointsAtItsOwnGeneratedLabelProvider() {
		JavaTestModel model = compileModel();

		Class<? extends LabelProvider> providerClass = model.getTypeJavaLabelProviderClass("Foo");
		assertEquals("test.labels.types.FooLabelProvider", providerClass.getName());

		RuneLabelProvider annotation = model.getTypeJavaClass("Foo").getAnnotation(RuneLabelProvider.class);
		assertNotNull(annotation, "Expected the pojo interface of the labelled type Foo to carry @RuneLabelProvider");
		assertSame(
				providerClass,
				annotation.labelProvider(),
				"The pojo annotation must point at the provider class the generator actually emitted");

		LabelProvider provider = model.getTypeJavaLabelProviderInstance("Foo");
		assertEquals("Attr One", provider.getLabel(RosettaPath.valueOf("attr1")));
		assertEquals("Attr Two", provider.getLabel(RosettaPath.valueOf("attr2")));
		assertNull(provider.getLabel(RosettaPath.valueOf("attr3")));
	}

	@Test
	void unlabelledTypeGetsNeitherAnnotationNorProvider() {
		JavaTestModel model = compileModel();

		assertNull(
				model.getTypeJavaClass("Bar").getAnnotation(RuneLabelProvider.class),
				"Bar has no labels of its own, so its pojo must be left untouched");
		assertThrows(
				NoSuchElementException.class,
				() -> model.getTypeJavaLabelProviderSource("Bar"),
				"Bar has no labels of its own, so no provider should have been generated for it");
	}
}
