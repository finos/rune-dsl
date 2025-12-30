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
}
