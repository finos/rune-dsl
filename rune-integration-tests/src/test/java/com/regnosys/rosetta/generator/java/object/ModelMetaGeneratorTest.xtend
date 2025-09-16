package com.regnosys.rosetta.generator.java.object

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.rosetta.model.lib.functions.ConditionValidator
import com.rosetta.model.lib.functions.DefaultConditionValidator
import com.rosetta.model.lib.functions.ModelObjectValidator
import com.rosetta.model.lib.functions.NoOpModelObjectValidator
import com.rosetta.model.lib.meta.RosettaMetaData
import com.rosetta.model.lib.qualify.QualifyFunctionFactory
import com.rosetta.model.lib.validation.ValidationResult.ValidationType
import java.math.BigDecimal
import java.math.BigInteger
import java.util.List
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static java.util.Map.of
import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class ModelMetaGeneratorTest {
	
	@Inject extension ModelHelper
	@Inject extension CodeGeneratorTestHelper
	
	final QualifyFunctionFactory funcFactory
	
	new() {
		// don't use the Language Injector. This is the Test env for the model.
		funcFactory = Guice.createInjector(new AbstractModule() {

			override protected configure() {
				bind(ConditionValidator).toInstance(new DefaultConditionValidator)
				bind(ModelObjectValidator).toInstance(new NoOpModelObjectValidator)
			}
		}).getInstance(QualifyFunctionFactory.Default)
	}
	
	@Test
	def void shouldGenerateGetQualifyFunctions() {
		val code = '''
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
		'''.generateCode
		val classes = code.compileToClasses

		val fooMeta = RosettaMetaData.cast(classes.get(rootPackage.child("meta") + '.FooMeta').declaredConstructor.newInstance)
		assertThat(fooMeta.getQualifyFunctions(funcFactory).size, is(2))
		
		val barMeta = RosettaMetaData.cast(classes.get(rootPackage.child("meta") + '.BarMeta').declaredConstructor.newInstance)
		assertThat(barMeta.getQualifyFunctions(funcFactory).size, is(0))
		
	}
	
	@Test
	def void shouldGenerateBasicTypeReferences() {
		val code = '''
			type Flat:
				oneField string (1..1)
					[metadata scheme]
				two int (1..*) 
					[metadata reference]
				three date (1..1)
					[metadata reference]
		'''.generateCode
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateValidators() {
		val code = '''
			typeAlias Max5Text: string(maxLength: 5)
			
			type Foo:
				a string (1..2)
				b number (1..1)
				c int (1..*)
				d number(min: -1) (0..1)
				f Max5Text (0..*)
		'''.generateCode
		
		assertEquals(
			'''
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
			import static com.rosetta.model.lib.expression.ExpressionOperators.checkCardinality;
			import static com.rosetta.model.lib.validation.ValidationResult.failure;
			import static com.rosetta.model.lib.validation.ValidationResult.success;
			import static java.util.stream.Collectors.toList;
			
			public class FooValidator implements Validator<Foo> {
			
				private List<ComparisonResult> getComparisonResults(Foo o) {
					return Lists.<ComparisonResult>newArrayList(
							checkCardinality("a", (List<String>) o.getA() == null ? 0 : o.getA().size(), 1, 2), 
							checkCardinality("b", (BigDecimal) o.getB() != null ? 1 : 0, 1, 1), 
							checkCardinality("c", (List<Integer>) o.getC() == null ? 0 : o.getC().size(), 1, 0), 
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
			'''.toString,
			code.get('com.rosetta.test.model.validation.FooValidator')
		)
		assertEquals(
			'''
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
			import static com.rosetta.model.lib.expression.ExpressionOperators.checkNumber;
			import static com.rosetta.model.lib.expression.ExpressionOperators.checkString;
			import static com.rosetta.model.lib.validation.ValidationResult.failure;
			import static com.rosetta.model.lib.validation.ValidationResult.success;
			import static java.util.Optional.empty;
			import static java.util.Optional.of;
			import static java.util.stream.Collectors.toList;
			
			public class FooTypeFormatValidator implements Validator<Foo> {
			
				private List<ComparisonResult> getComparisonResults(Foo o) {
					return Lists.<ComparisonResult>newArrayList(
							checkNumber("c", o.getC(), empty(), of(0), empty(), empty()), 
							checkNumber("d", o.getD(), empty(), empty(), of(new BigDecimal("-1")), empty()), 
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
			'''.toString,
			code.get('com.rosetta.test.model.validation.FooTypeFormatValidator')
		)
		
		val classes = code.compileToClasses

		val fooMeta = RosettaMetaData.cast(classes.get(rootPackage.child("meta") + '.FooMeta').declaredConstructor.newInstance)
		val validator = fooMeta.validator
		val typeFormatValidator = fooMeta.typeFormatValidator
		
		val validFoo = classes.createInstanceUsingBuilder('Foo', of(
			'a', List.of("test"),
			'b', new BigDecimal("123.42"),
			'c', List.of(-2),
			'd', new BigDecimal("0"),
			'f', List.of("abcde", "")
		))
		assertThat(validator.validate(null, validFoo).success, is(true))
		assertThat(typeFormatValidator.validate(null, validFoo).success, is(true))
		
		val invalidFoo1 = classes.createInstanceUsingBuilder('Foo', of(
			'a', List.of("a", "b", "c"),
			// 'b', null,
			'c', List.of(),
			// 'd', null,
			'f', List.of()
		))
		val res1 = validator.validate(null, invalidFoo1)
		assertThat(res1.success, is(false))
		assertEquals("Maximum of 2 'a' are expected but found 3.; 'b' is a required field but does not exist.; 'c' is a required field but does not exist.",
			res1.failureReason.get
		)
		assertThat(res1.validationType, is(ValidationType.CARDINALITY))
		assertThat(typeFormatValidator.validate(null, invalidFoo1).success, is(true))
		
		val invalidFoo2 = classes.createInstanceUsingBuilder('Foo', of(
			'a', List.of("a", "b"),
			'b', new BigDecimal("123.42"),
			'c', List.of(-2),
			'd', new BigDecimal("-1.1"),
			'f', List.of("aaaaaa", "bb", "ccccccc")
		))
		assertThat(validator.validate(null, invalidFoo2).success, is(true))
		val res2 = typeFormatValidator.validate(null, invalidFoo2)
		assertThat(res2.success, is(false))
		assertEquals("Expected a number greater than or equal to -1 for 'd', but found -1.1.; Field 'f' must have a value with maximum length of 5 characters but value 'aaaaaa' has length of 6 characters. - Field 'f' must have a value with maximum length of 5 characters but value 'ccccccc' has length of 7 characters.",
			res2.failureReason.get
		)
		assertThat(res2.validationType, is(ValidationType.TYPE_FORMAT))
	}
	
	@Test
	def void shouldGenerateUserFriendlyTypeFormatValidationErrors() {
		val classes = '''			
			type A:
				a string(minLength: 3, pattern: "A.*Z") (1..3)
				b string(maxLength: 5) (1..1)
			
			type B:
				a number(digits: 3) (1..1)
				b number(fractionalDigits: 2) (1..1)
				c number(min: 0, max: 10) (1..1)
		'''.generateCode.compileToClasses
		
		val aMeta = RosettaMetaData.cast(classes.get(rootPackage.child("meta") + '.AMeta').declaredConstructor.newInstance)
		val aTypeFormatValidator = aMeta.typeFormatValidator
		
		val invalidA1 = classes.createInstanceUsingBuilder('A', of(
			'a', List.of("AZ", "ABZ", "AA"),
			'b', "AAAAAA"
		))
		val resA1 = aTypeFormatValidator.validate(null, invalidA1)
		assertThat(resA1.success, is(false))
        assertEquals("Field 'a' requires a value with minimum length of 3 characters but value 'AZ' has length of 2 characters. - Field 'a' requires a value with minimum length of 3 characters but value 'AA' has length of 2 characters. Field 'a' with value 'AA' does not match the pattern /A.*Z/.; Field 'b' must have a value with maximum length of 5 characters but value 'AAAAAA' has length of 6 characters.",
            resA1.failureReason.get
		)
		
		val bMeta = RosettaMetaData.cast(classes.get(rootPackage.child("meta") + '.BMeta').declaredConstructor.newInstance)
		val bTypeFormatValidator = bMeta.typeFormatValidator
		
		val invalidB1 = classes.createInstanceUsingBuilder('B', of(
			'a', new BigDecimal('-1000'),
			'b', new BigDecimal('13.1415'),
			'c', new BigDecimal('-1')
		))
		val resB1 = bTypeFormatValidator.validate(null, invalidB1)
		assertThat(resB1.success, is(false))
		assertEquals("Expected a maximum of 3 digits for 'a', but the number -1000 has 4.; Expected a maximum of 2 fractional digits for 'b', but the number 13.1415 has 4.; Expected a number greater than or equal to 0 for 'c', but found -1.",
			resB1.failureReason.get
		)
		val invalidB2 = classes.createInstanceUsingBuilder('B', of(
			'a', new BigDecimal('10.01'),
			'b', new BigDecimal('-123.14'),
			'c', new BigDecimal('11')
		))
		val resB2 = bTypeFormatValidator.validate(null, invalidB2)
		assertThat(resB2.success, is(false))
		assertEquals("Expected a maximum of 3 digits for 'a', but the number 10.01 has 4.; Expected a number less than or equal to 10 for 'c', but found 11.",
			resB2.failureReason.get
		)
		val invalidB3 = classes.createInstanceUsingBuilder('B', of(
			'a', new BigDecimal('0.001'),
			'b', new BigDecimal('0'),
			'c', new BigDecimal('0')
		))
		val resB3 = bTypeFormatValidator.validate(null, invalidB3)
		assertThat(resB3.success, is(false))
		assertEquals("Expected a maximum of 3 digits for 'a', but the number 0.001 has 4.",
			resB3.failureReason.get
		)
	}
	
	@Test
	def void typeFormatValidationShouldWorkForDifferentJavaNumberTypes() {
		val classes = '''			
			type A:
				integers number(digits: 8, fractionalDigits: 0) (0..*)
				long number(digits: 10, fractionalDigits: 0) (1..1)
				bigInteger number(digits: 20, fractionalDigits: 0) (1..1)
		'''.generateCode.compileToClasses
		
		val aMeta = RosettaMetaData.cast(classes.get(rootPackage.child("meta") + '.AMeta').declaredConstructor.newInstance)
		val aTypeFormatValidator = aMeta.typeFormatValidator
		
		val invalidA1 = classes.createInstanceUsingBuilder('A', of(
			'integers', List.of(1, 2, 3),
			'long', 4l,
			'bigInteger', BigInteger.valueOf(5)
		))
		val resA1 = aTypeFormatValidator.validate(null, invalidA1)
		assertThat(resA1.success, is(true))
	}
}