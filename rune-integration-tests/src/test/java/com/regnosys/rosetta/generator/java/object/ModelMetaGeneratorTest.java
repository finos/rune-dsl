package com.regnosys.rosetta.generator.java.object;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.functions.ConditionValidator;
import com.rosetta.model.lib.functions.DefaultConditionValidator;
import com.rosetta.model.lib.functions.ModelObjectValidator;
import com.rosetta.model.lib.functions.NoOpModelObjectValidator;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.qualify.QualifyFunctionFactory;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidationResult.ValidationType;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.lib.validation.ValidatorFactory;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class ModelMetaGeneratorTest {

	@Inject
	private ModelHelper modelHelper;
	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;
	@Inject
	private ValidatorFactory validatorFactory;

	private final QualifyFunctionFactory funcFactory;

	ModelMetaGeneratorTest() {
		// don't use the Language Injector. This is the Test env for the model.
		funcFactory = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(ConditionValidator.class).toInstance(new DefaultConditionValidator());
				bind(ModelObjectValidator.class).toInstance(new NoOpModelObjectValidator());
			}
		}).getInstance(QualifyFunctionFactory.Default.class);
	}

	@Test
	void shouldGenerateGetQualifyFunctions() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				isEvent root Foo;

				type Foo:
					a string (0..1)

				type Bar extends Foo:
					b string (0..1)

				func Qualify_AExists:
					[qualification BusinessEvent]
					inputs: foo Foo (1..1)
					output: is_event boolean (1..1)
					set is_event:
						foo -> a exists

				func Qualify_AEqualsSomeValue:
					[qualification BusinessEvent]
					inputs: foo Foo (1..1)
					output: is_event boolean (1..1)
					set is_event:
						foo -> a = "someValue"
				""");
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		RosettaMetaData<?> fooMeta = (RosettaMetaData<?>) classes
				.get(modelHelper.rootPackage().child("meta") + ".FooMeta")
				.getDeclaredConstructor().newInstance();
		assertThat(fooMeta.getQualifyFunctions(funcFactory).size(), is(2));

		RosettaMetaData<?> barMeta = (RosettaMetaData<?>) classes
				.get(modelHelper.rootPackage().child("meta") + ".BarMeta")
				.getDeclaredConstructor().newInstance();
		assertThat(barMeta.getQualifyFunctions(funcFactory).size(), is(0));
	}

	@Test
	void shouldGenerateBasicTypeReferences() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				type Flat:
					oneField string (1..1)
						[metadata scheme]
					two int (1..*)
						[metadata reference]
					three date (1..1)
						[metadata reference]
				""");
		generatorTestHelper.compileToClasses(code);
	}

	@Test
	void shouldGenerateValidators() throws Exception {
		Map<String, String> code = generatorTestHelper.generateCode("""
				typeAlias Max5Text: string(maxLength: 5)

				type Foo:
					a string (1..2)
					b number (1..1)
					c int (1..*)
					d number(min: -1) (0..1)
					f Max5Text (0..*)
				""");

		assertEquals("""
				package com.rosetta.test.model.validation;

				import com.google.common.collect.Lists;
				import com.rosetta.model.lib.expression.ComparisonResult;
				import com.rosetta.model.lib.path.RosettaPath;
				import com.rosetta.model.lib.validation.ValidationResult;
				import com.rosetta.model.lib.validation.Validator;
				import com.rosetta.test.model.Foo;
				import java.math.BigDecimal;
				import java.util.List;

				import static com.google.common.base.Strings.isNullOrEmpty;
				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkCardinality;
				import static com.rosetta.model.lib.validation.ValidationResult.failure;
				import static com.rosetta.model.lib.validation.ValidationResult.success;
				import static java.util.stream.Collectors.toList;

				public class FooValidator implements Validator<Foo> {

					private List<ComparisonResult> getComparisonResults(Foo o) {
						return Lists.<ComparisonResult>newArrayList(
								checkCardinality("a", (List<String>) o.getA() == null ? 0 : o.getA().size(), 1, 2),\s
								checkCardinality("b", (BigDecimal) o.getB() != null ? 1 : 0, 1, 1),\s
								checkCardinality("c", (List<Integer>) o.getC() == null ? 0 : o.getC().size(), 1, 0),\s
								checkCardinality("d", (BigDecimal) o.getD() != null ? 1 : 0, 0, 1)
							);
					}

					@Override
					public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo o) {
						return getComparisonResults(o)
							.stream()
							.map(res -> {
								if (!isNullOrEmpty(res.getError())) {
									return failure("Foo", ValidationResult.ValidationType.CARDINALITY, "Foo", path, "", res.getError());
								}
								return success("Foo", ValidationResult.ValidationType.CARDINALITY, "Foo", path, "");
							})
							.collect(toList());
					}

				}
				""",
				code.get("com.rosetta.test.model.validation.FooValidator"));
		assertEquals("""
				package com.rosetta.test.model.validation;

				import com.google.common.collect.Lists;
				import com.rosetta.model.lib.expression.ComparisonResult;
				import com.rosetta.model.lib.path.RosettaPath;
				import com.rosetta.model.lib.validation.ValidationResult;
				import com.rosetta.model.lib.validation.Validator;
				import com.rosetta.test.model.Foo;
				import java.math.BigDecimal;
				import java.util.List;

				import static com.google.common.base.Strings.isNullOrEmpty;
				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkNumber;
				import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkString;
				import static com.rosetta.model.lib.validation.ValidationResult.failure;
				import static com.rosetta.model.lib.validation.ValidationResult.success;
				import static java.util.Optional.empty;
				import static java.util.Optional.of;
				import static java.util.stream.Collectors.toList;

				public class FooTypeFormatValidator implements Validator<Foo> {

					private List<ComparisonResult> getComparisonResults(Foo o) {
						return Lists.<ComparisonResult>newArrayList(
								checkNumber("c", o.getC(), empty(), of(0), empty(), empty()),\s
								checkNumber("d", o.getD(), empty(), empty(), of(new BigDecimal("-1")), empty()),\s
								checkString("f", o.getF(), 0, of(5), empty())
							);
					}

					@Override
					public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo o) {
						return getComparisonResults(o)
							.stream()
							.map(res -> {
								if (!isNullOrEmpty(res.getError())) {
									return failure("Foo", ValidationResult.ValidationType.TYPE_FORMAT, "Foo", path, "", res.getError());
								}
								return success("Foo", ValidationResult.ValidationType.TYPE_FORMAT, "Foo", path, "");
							})
							.collect(toList());
					}

				}
				""",
				code.get("com.rosetta.test.model.validation.FooTypeFormatValidator"));

		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(code);

		RosettaMetaData<?> fooMeta = (RosettaMetaData<?>) classes
				.get(modelHelper.rootPackage().child("meta") + ".FooMeta")
				.getDeclaredConstructor().newInstance();
		@SuppressWarnings("rawtypes")
		Validator validator = fooMeta.validator(validatorFactory);
		@SuppressWarnings("rawtypes")
		Validator typeFormatValidator = fooMeta.typeFormatValidator(validatorFactory);

		RosettaModelObject validFoo = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
				"a", List.of("test"),
				"b", new BigDecimal("123.42"),
				"c", List.of(-2),
				"d", new BigDecimal("0"),
				"f", List.of("abcde", "")));
		assertThat(getResults(validator, validFoo).get(0).isSuccess(), is(true));
		assertThat(getResults(typeFormatValidator, validFoo).get(0).isSuccess(), is(true));

		RosettaModelObject invalidFoo1 = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
				"a", List.of("a", "b", "c"),
				// 'b', null,
				"c", List.of(),
				// 'd', null,
				"f", List.of()));
		List<ValidationResult<?>> res1 = failures(getResults(validator, invalidFoo1));
		assertThat(failureReasons(res1), hasItem(equalTo("Maximum of 2 'a' are expected but found 3.")));
		assertThat(failureReasons(res1), hasItem(equalTo("'b' is a required field but does not exist.")));
		assertThat(failureReasons(res1), hasItem(equalTo("'c' is a required field but does not exist.")));

		assertThat(validationTypes(res1), everyItem(is(ValidationType.CARDINALITY)));
		assertThat(getResults(typeFormatValidator, invalidFoo1).get(0).isSuccess(), is(true));

		RosettaModelObject invalidFoo2 = generatorTestHelper.createInstanceUsingBuilder(classes, "Foo", Map.of(
				"a", List.of("a", "b"),
				"b", new BigDecimal("123.42"),
				"c", List.of(-2),
				"d", new BigDecimal("-1.1"),
				"f", List.of("aaaaaa", "bb", "ccccccc")));
		assertThat(getResults(validator, invalidFoo2).get(0).isSuccess(), is(true));
		List<ValidationResult<?>> res2 = failures(getResults(typeFormatValidator, invalidFoo2));
		assertThat(failureReasons(res2), hasItem(equalTo("Expected a number greater than or equal to -1 for 'd', but found -1.1.")));
		assertThat(failureReasons(res2), hasItem(equalTo("Field 'f' must have a value with maximum length of 5 characters but value 'aaaaaa' has length of 6 characters. - Field 'f' must have a value with maximum length of 5 characters but value 'ccccccc' has length of 7 characters.")));
		assertThat(validationTypes(res2), everyItem(is(ValidationType.TYPE_FORMAT)));
	}

	@Test
	void shouldGenerateUserFriendlyTypeFormatValidationErrors() throws Exception {
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(generatorTestHelper.generateCode("""
				type A:
					a string(minLength: 3, pattern: "A.*Z") (1..3)
					b string(maxLength: 5) (1..1)

				type B:
					a number(digits: 3) (1..1)
					b number(fractionalDigits: 2) (1..1)
					c number(min: 0, max: 10) (1..1)
				"""));

		RosettaMetaData<?> aMeta = (RosettaMetaData<?>) classes
				.get(modelHelper.rootPackage().child("meta") + ".AMeta")
				.getDeclaredConstructor().newInstance();
		@SuppressWarnings("rawtypes")
		Validator aTypeFormatValidator = aMeta.typeFormatValidator(validatorFactory);

		RosettaModelObject invalidA1 = generatorTestHelper.createInstanceUsingBuilder(classes, "A", Map.of(
				"a", List.of("AZ", "ABZ", "AA"),
				"b", "AAAAAA"));
		List<ValidationResult<?>> resA1 = failures(getResults(aTypeFormatValidator, invalidA1));

		assertThat(failureReasons(resA1), hasItem(equalTo("Field 'a' requires a value with minimum length of 3 characters but value 'AZ' has length of 2 characters. - Field 'a' requires a value with minimum length of 3 characters but value 'AA' has length of 2 characters. Field 'a' with value 'AA' does not match the pattern /A.*Z/.")));
		assertThat(failureReasons(resA1), hasItem(equalTo("Field 'b' must have a value with maximum length of 5 characters but value 'AAAAAA' has length of 6 characters.")));

		RosettaMetaData<?> bMeta = (RosettaMetaData<?>) classes
				.get(modelHelper.rootPackage().child("meta") + ".BMeta")
				.getDeclaredConstructor().newInstance();
		@SuppressWarnings("rawtypes")
		Validator bTypeFormatValidator = bMeta.typeFormatValidator(validatorFactory);

		RosettaModelObject invalidB1 = generatorTestHelper.createInstanceUsingBuilder(classes, "B", Map.of(
				"a", new BigDecimal("-1000"),
				"b", new BigDecimal("13.1415"),
				"c", new BigDecimal("-1")));
		List<ValidationResult<?>> resB1 = failures(getResults(bTypeFormatValidator, invalidB1));
		assertThat(failureReasons(resB1), hasItem(equalTo("Expected a maximum of 3 digits for 'a', but the number -1000 has 4.")));
		assertThat(failureReasons(resB1), hasItem(equalTo("Expected a maximum of 2 fractional digits for 'b', but the number 13.1415 has 4.")));
		assertThat(failureReasons(resB1), hasItem(equalTo("Expected a number greater than or equal to 0 for 'c', but found -1.")));

		RosettaModelObject invalidB2 = generatorTestHelper.createInstanceUsingBuilder(classes, "B", Map.of(
				"a", new BigDecimal("10.01"),
				"b", new BigDecimal("-123.14"),
				"c", new BigDecimal("11")));
		List<ValidationResult<?>> resB2 = failures(getResults(bTypeFormatValidator, invalidB2));
		assertThat(failureReasons(resB2), hasItem(equalTo("Expected a maximum of 3 digits for 'a', but the number 10.01 has 4.")));
		assertThat(failureReasons(resB2), hasItem(equalTo("Expected a number less than or equal to 10 for 'c', but found 11.")));

		RosettaModelObject invalidB3 = generatorTestHelper.createInstanceUsingBuilder(classes, "B", Map.of(
				"a", new BigDecimal("0.001"),
				"b", new BigDecimal("0"),
				"c", new BigDecimal("0")));
		ValidationResult<?> resB3 = getResults(bTypeFormatValidator, invalidB3).get(0);
		assertThat(resB3.isSuccess(), is(false));
		assertEquals("Expected a maximum of 3 digits for 'a', but the number 0.001 has 4.",
				resB3.getFailureReason().get());
	}

	@Test
	void typeFormatValidationShouldWorkForDifferentJavaNumberTypes() throws Exception {
		Map<String, Class<?>> classes = generatorTestHelper.compileToClasses(generatorTestHelper.generateCode("""
				type A:
					integers number(digits: 8, fractionalDigits: 0) (0..*)
					long number(digits: 10, fractionalDigits: 0) (1..1)
					bigInteger number(digits: 20, fractionalDigits: 0) (1..1)
				"""));

		RosettaMetaData<?> aMeta = (RosettaMetaData<?>) classes
				.get(modelHelper.rootPackage().child("meta") + ".AMeta")
				.getDeclaredConstructor().newInstance();
		@SuppressWarnings("rawtypes")
		Validator aTypeFormatValidator = aMeta.typeFormatValidator(validatorFactory);

		RosettaModelObject invalidA1 = generatorTestHelper.createInstanceUsingBuilder(classes, "A", Map.of(
				"integers", List.of(1, 2, 3),
				"long", 4L,
				"bigInteger", BigInteger.valueOf(5)));
		ValidationResult<?> resA1 = getResults(aTypeFormatValidator, invalidA1).get(0);
		assertThat(resA1.isSuccess(), is(true));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List<ValidationResult<?>> getResults(Validator validator, RosettaModelObject o) {
		return validator.getValidationResults(null, o);
	}

	private static List<ValidationResult<?>> failures(List<ValidationResult<?>> results) {
		return results.stream().filter(r -> !r.isSuccess()).collect(Collectors.toList());
	}

	private static List<String> failureReasons(List<ValidationResult<?>> results) {
		return results.stream().map(r -> r.getFailureReason().get()).collect(Collectors.toList());
	}

	private static List<ValidationType> validationTypes(List<ValidationResult<?>> results) {
		return results.stream().map(ValidationResult::getValidationType).collect(Collectors.toList());
	}
}
