package com.regnosys.rosetta.generator.java.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FunctionGeneratorDefaultTest {

	@Inject
	private FunctionGeneratorHelper functionGeneratorHelper;
	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;

	@Test
	void defaultOperatorEvaluatesToLeftWhenBothSidesPresent() {
		var model = """
				func Foo:
					inputs:
						left string (0..1)
						right string (1..1)
					output: result string (1..1)
					set result:
						left default right
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var result = functionGeneratorHelper.invokeFunc(foo, String.class, "a", "b");

		assertEquals("a", result);
	}

	@Test
	void defaultOperatorEvaluatesToRightWhenLeftIsEmpty() {
		var model = """
				func Foo:
					inputs:
						left string (0..1)
						right string (1..1)
					output: result string (1..1)
					set result:
						left default right
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var result = functionGeneratorHelper.invokeFunc(foo, String.class, null, "b");

		assertEquals("b", result);
	}

	@Test
	void defaultOperatorEvaluatesToLeftWhenBothSidesPresentWithAlias() {
		var model = """
				func Foo:
					inputs:
						left string (0..1)
						right string (1..1)
					output:
						result string (1..1)

					alias aliasResult:
						left default right

					set result:
						aliasResult
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var result = functionGeneratorHelper.invokeFunc(foo, String.class, "a", "b");

		assertEquals("a", result);
	}

	@Test
	void defaultOperatorEvaluatesToLeftWhenBothSidesPresentMultiCardinality() {
		var model = """
				func Foo:
					inputs:
						left string (1..*)
						right string (1..*)
					output: result string (1..*)
					set result:
						left default right
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var result = functionGeneratorHelper.invokeFunc(foo, List.class, List.of("a1", "a2"), List.of("b1", "b2"));

		assertEquals(List.of("a1", "a2"), result);
	}

	@Test
	void defaultOperatorEvaluatesToRightWhenLeftIsEmptyMultiCardinality() {
		var model = """
				func Foo:
					inputs:
						left string (1..*)
						right string (1..*)
					output: result string (1..*)
					set result:
						left default right
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var result = functionGeneratorHelper.invokeFunc(foo, List.class, List.of(), List.of("b1", "b2"));

		assertEquals(List.of("b1", "b2"), result);
	}

	@Test
	void defaultOperatorEvaluatesToLeftWhenBothSidesPresentMultiCardinalityComplexType() {
		var model = """
				type Bar:
					attr string (0..1)

				func Foo:
					inputs:
						left Bar (1..*)
						right Bar (1..*)
					output:
						result Bar (1..*)

					set result:
						left default right
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var a1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "a1"));
		var a2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "a2"));
		var b1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "b1"));
		var b2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "b2"));

		var result = functionGeneratorHelper.invokeFunc(foo, List.class, List.of(a1, a2), List.of(b1, b2));

		assertEquals(List.of(a1, a2), result);
	}

	@Test
	void defaultOperatorEvaluatesToRightWhenBothSidesPresentMultiCardinalityComplexType() {
		var model = """
				type Bar:
					attr string (0..1)

				func Foo:
					inputs:
						left Bar (1..*)
						right Bar (1..*)
					output:
						result Bar (1..*)

					set result:
						left default right
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var b1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "b1"));
		var b2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "b2"));

		var result = functionGeneratorHelper.invokeFunc(foo, List.class, List.of(), List.of(b1, b2));

		assertEquals(List.of(b1, b2), result);
	}

	@Test
	void defaultOperatorEvaluatesToLeftWhenBothSidesPresentMultiCardinalityComplexSubType() {
		var model = """
				type Bar:
					attr1 string (0..1)

				type Baz extends Bar:
					attr2 string (0..1)

				func Foo:
					inputs:
						left Bar (1..*)
						right Baz (1..*)
					output:
						result Bar (1..*)

					set result:
						left default right
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var a1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr1", "a1"));
		var a2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr1", "a2"));
		var b1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Baz", Map.of("attr2", "b1"));
		var b2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Baz", Map.of("attr2", "b2"));

		var result = functionGeneratorHelper.invokeFunc(foo, List.class, List.of(a1, a2), List.of(b1, b2));

		assertEquals(List.of(a1, a2), result);
	}

	@Test
	void defaultOperatorEvaluatesToRightWhenBothSidesPresentMultiCardinalityComplexSubType() {
		var model = """
				type Bar:
					attr1 string (0..1)

				type Baz extends Bar:
					attr2 string (0..1)

				func Foo:
					inputs:
						left Bar (1..*)
						right Baz (1..*)
					output:
						result Bar (1..*)

					set result:
						left default right
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var b1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Baz", Map.of("attr2", "b1"));
		var b2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Baz", Map.of("attr2", "b2"));

		var result = functionGeneratorHelper.invokeFunc(foo, List.class, List.of(), List.of(b1, b2));

		assertEquals(List.of(b1, b2), result);
	}

	@Test
	void defaultOperatorEvaluatesToLeftWhenBothSidesPresentMultiCardinalityComplexTypeWithAlias() {
		var model = """
				type Bar:
					attr string (0..1)

				func Foo:
					inputs:
						left Bar (1..*)
						right Bar (1..*)
					output:
						result Bar (1..*)

					alias aliasResult:
						left default right

					set result:
						aliasResult
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var a1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "a1"));
		var a2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "a2"));
		var b1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "b1"));
		var b2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "b2"));

		var result = functionGeneratorHelper.invokeFunc(foo, List.class, List.of(a1, a2), List.of(b1, b2));

		assertEquals(List.of(a1, a2), result);
	}

	@Test
	void defaultOperatorEvaluatesToRightWhenBothSidesPresentMultiCardinalityComplexTypeWithAlias() {
		var model = """
				type Bar:
					attr string (0..1)

				func Foo:
					inputs:
						left Bar (1..*)
						right Bar (1..*)
					output:
						result Bar (1..*)

					alias aliasResult:
						left default right

					set result:
						aliasResult
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var b1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "b1"));
		var b2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "b2"));

		var result = functionGeneratorHelper.invokeFunc(foo, List.class, List.of(), List.of(b1, b2));

		assertEquals(List.of(b1, b2), result);
	}

	@Test
	void defaultOperatorEvaluatesToLeftWhenBothSidesPresentMultiCardinalityComplexTypeWithIf() {
		var model = """
				type Bar:
					attr string (0..1)

				func Foo:
					inputs:
						left Bar (1..*)
						right Bar (1..*)
						cond boolean (1..1)
					output:
						result Bar (1..*)

					set result:
						if cond
						then left default right
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var a1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "a1"));
		var a2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "a2"));
		var b1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "b1"));
		var b2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "b2"));

		var result = functionGeneratorHelper.invokeFunc(foo, List.class, List.of(a1, a2), List.of(b1, b2), true);

		assertEquals(List.of(a1, a2), result);
	}

	@Test
	void defaultOperatorEvaluatesToRightWhenBothSidesPresentMultiCardinalityComplexTypeWithIf() {
		var model = """
				type Bar:
					attr string (0..1)

				func Foo:
					inputs:
						left Bar (1..*)
						right Bar (1..*)
						cond boolean (1..1)
					output:
						result Bar (1..*)

					set result:
						if cond
						then left default right
				""";
		var code = generatorTestHelper.generateCode(model);

		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		var b1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "b1"));
		var b2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "b2"));

		var result = functionGeneratorHelper.invokeFunc(foo, List.class, List.of(), List.of(b1, b2), true);

		assertEquals(List.of(b1, b2), result);
	}

	@Test
	void defaultOperatorEvaluatesListLiteralWithSingleFallback() {
		var model = """
				func Foo:
					output:
						result string (0..*)
					set result:
						["A", "B"] default "C"
				""";
		var code = generatorTestHelper.generateCode(model);
		var classes = generatorTestHelper.compileToClasses(code);
		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		assertEquals(List.of("A", "B"), functionGeneratorHelper.invokeFunc(foo, List.class));
	}

	@Test
	void defaultOperatorEvaluatesMultiLeftWithSingleRight() {
		var model = """
				func Foo:
					inputs:
						left string (0..*)
						right string (1..1)
					output:
						result string (0..*)
					set result:
						left default right
				""";
		var code = generatorTestHelper.generateCode(model);
		var classes = generatorTestHelper.compileToClasses(code);
		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		assertEquals(List.of("A", "B"), functionGeneratorHelper.invokeFunc(foo, List.class, List.of("A", "B"), "C"));
		assertEquals(List.of("C"), functionGeneratorHelper.invokeFunc(foo, List.class, List.of(), "C"));
	}

	@Test
	void defaultOperatorEvaluatesSingleLeftWithMultiRight() {
		var model = """
				func Foo:
					inputs:
						left string (0..1)
						right string (1..*)
					output:
						result string (1..*)
					alias defaulted:
						left default right
					set result:
						defaulted

				func EmptyLeftDefaultMulti:
					output:
						result string (1..*)
					set result:
						empty default ["B", "C"]
				""";
		var code = generatorTestHelper.generateCode(model);
		var classes = generatorTestHelper.compileToClasses(code);

		var foo = functionGeneratorHelper.createFunc(classes, "Foo");
		assertEquals(List.of("A"), functionGeneratorHelper.invokeFunc(foo, List.class, "A", List.of("B", "C")));
		assertEquals(List.of("B", "C"), functionGeneratorHelper.invokeFunc(foo, List.class, null, List.of("B", "C")));

		var emptyLeft = functionGeneratorHelper.createFunc(classes, "EmptyLeftDefaultMulti");
		assertEquals(List.of("B", "C"), functionGeneratorHelper.invokeFunc(emptyLeft, List.class));
	}

	@Test
	void defaultOperatorWithMixedCardinalityChainedFeatureCallAndListOperations() {
		var model = """
				type Bar:
					attr string (1..1)

				type Baz extends Bar:
					extra string (0..1)

				func MultiDefaultSingleFeatureCall:
					inputs:
						bars Bar (0..*)
						fallback Bar (1..1)
					output:
						result string (1..1)
					alias attrs:
						(bars default fallback) -> attr
					set result:
						attrs join ","

				func SingleDefaultMultiFeatureCall:
					inputs:
						optBar Bar (0..1)
						fallbackBars Baz (1..*)
					output:
						result string (1..*)
					alias attrs:
						(optBar default fallbackBars) -> attr
					set result:
						attrs
				""";
		var code = generatorTestHelper.generateCode(model);
		var classes = generatorTestHelper.compileToClasses(code);

		var b1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "A"));
		var b2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "B"));
		var fallback = generatorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("attr", "C"));
		var baz1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Baz", Map.of("attr", "X", "extra", "e1"));
		var baz2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Baz", Map.of("attr", "Y", "extra", "e2"));

		var multiDefaultSingle = functionGeneratorHelper.createFunc(classes, "MultiDefaultSingleFeatureCall");
		assertEquals("A,B", functionGeneratorHelper.invokeFunc(multiDefaultSingle, String.class, List.of(b1, b2), fallback));
		assertEquals("C", functionGeneratorHelper.invokeFunc(multiDefaultSingle, String.class, List.of(), fallback));

		var singleDefaultMulti = functionGeneratorHelper.createFunc(classes, "SingleDefaultMultiFeatureCall");
		assertEquals(List.of("A"), functionGeneratorHelper.invokeFunc(singleDefaultMulti, List.class, b1, List.of(baz1, baz2)));
		assertEquals(List.of("X", "Y"), functionGeneratorHelper.invokeFunc(singleDefaultMulti, List.class, null, List.of(baz1, baz2)));
	}

	@Test
	void defaultOperatorWithComparisonResultAndMultiBoolean() {
		var model = """
				func Foo:
					inputs:
						cond boolean (1..1)
						a int (0..1)
						b int (1..1)
					output:
						result boolean (1..*)
					alias defaulted:
						(if cond then a = b) default [True, False]
					set result:
						defaulted
				""";
		var code = generatorTestHelper.generateCode(model);
		var classes = generatorTestHelper.compileToClasses(code);
		var foo = functionGeneratorHelper.createFunc(classes, "Foo");

		assertEquals(List.of(true), functionGeneratorHelper.invokeFunc(foo, List.class, true, 5, 5));
		assertEquals(List.of(false), functionGeneratorHelper.invokeFunc(foo, List.class, true, null, 5));
		assertEquals(List.of(true, false), functionGeneratorHelper.invokeFunc(foo, List.class, false, 5, 5));
	}
}
