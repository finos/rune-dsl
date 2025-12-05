/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rosetta.model.lib.expression;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.Mapper.Path;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.validation.ChoiceRuleValidationMethod;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExpressionOperatorsNullSafe {
	
	// notExists
	
	public static <T> ComparisonResult notExists(Mapper<T> o) {
		if (o.resultCount()==0) {
			return ComparisonResult.success();
		}
		return ComparisonResult.failure(o.getPaths() + " does exist and is " + formatMultiError(o));
	}
	
	// exists
	
	public static <T> ComparisonResult exists(Mapper<T> o) {
		if (o.resultCount()>0) {
			return ComparisonResult.success();
		}
		return ComparisonResult.failure(o.getErrorPaths() + " does not exist");
	}
	
	// singleExists
	
	public static <T> ComparisonResult singleExists(Mapper<T> o) {
		if (o.resultCount()==1) {
			return  ComparisonResult.success();
		}
		
		String error = o.resultCount() > 0 ?
				String.format("Expected single %s but found %s [%s]", o.getPaths(), o.resultCount(), formatMultiError(o)) :
				String.format("Expected single %s but found zero", o.getErrorPaths());
		
		return ComparisonResult.failure(error);
	}
	
	// multipleExists
	
	public static <T> ComparisonResult multipleExists(Mapper<T> o) {
		if (o.resultCount()>1) {
			return ComparisonResult.success();
		}
		
		String error = o.resultCount() > 0 ?
				String.format("Expected multiple %s but only one [%s]", o.getPaths(), formatMultiError(o)) :
				String.format("Expected multiple %s but found zero", o.getErrorPaths());
				
		return ComparisonResult.failure(error);
	}
	
	// onlyExists
	
	@Deprecated // Since 9.11.3
	public static ComparisonResult onlyExists(List<? extends Mapper<?>> o) {
		// Validation rule checks that all parents match
		Set<RosettaModelObject> parents = o.stream()
				.map(Mapper::getParentMulti)
				.flatMap(Collection::stream)
				.map(RosettaModelObject.class::cast)
			    .collect(Collectors.toSet());
		
		if (parents.size() == 0) {
			return ComparisonResult.failure("No fields set.");
		}

		// Find attributes to check
		Set<String> fields = o.stream()
				.flatMap(m -> Stream.concat(m.getPaths().stream(), m.getErrorPaths().stream()))
				.map(ExpressionOperatorsNullSafe::getAttributeName)
				.collect(Collectors.toSet());
		
		// The number of attributes to check, should equal the number of mappers
		if (fields.size() != o.size()) {
			return ComparisonResult.failure("All required fields not set.");
		}
		
		// Run validation then and results together 
		return parents.stream()
			.map(p -> validateOnlyExists(p, fields))
			.reduce(ComparisonResult.success(), (a, b) -> a.andNullSafe(b));
	}
	
	public static <T> ComparisonResult onlyExists(Mapper<T> mapper, List<String> allFieldNames, List<String> requiredFields) {
		List<T> objects = mapper.getMulti();
		
		if (objects == null || objects.isEmpty()) {
			String requiredFieldsMessage = requiredFields.stream().collect(Collectors.joining("', '", "'", "'"));
			String errorMessage = String.format("Expected only %s to be set, but object was absent.", requiredFieldsMessage);
			return ComparisonResult.failure(errorMessage);
		}
		
		return objects.stream()
				.map(p -> validateOnlyExists(p, allFieldNames, requiredFields))
				.reduce(ComparisonResult.success(), (a, b) -> a.andNullSafe(b));
	}

	private static <T> ComparisonResult validateOnlyExists(T object, List<String> allFieldNames, List<String> requiredFields) {
		List<String> populatedFieldNames = new LinkedList<>();
		for (String a: allFieldNames) {
			try {
				Method getter = object.getClass().getMethod("get" + StringUtils.capitalize(a));
				if (ExistenceChecker.isSet(getter.invoke(object))) {
					populatedFieldNames.add(a);
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			}
		}
				
		if (new HashSet<>(populatedFieldNames).equals(new HashSet<>(requiredFields))) {
			return ComparisonResult.success();
		}
		String requiredFieldsMessage = requiredFields.stream().collect(Collectors.joining("', '", "'", "'"));
		String errorMessage;
		if (populatedFieldNames.size() == 0) {
			errorMessage = String.format("Expected only %s to be set, but no fields were set.", requiredFieldsMessage);
		} else {
			String setFields = populatedFieldNames.stream().collect(Collectors.joining("', '", "'", "'"));
			errorMessage = String.format("Expected only %s to be set, but set fields are: %s.", requiredFieldsMessage, setFields);
		}
		return ComparisonResult.failure(errorMessage);
	}
	
	/**
	 * @return attributeName - get the attribute name which is the path leaf node, unless attribute has metadata (scheme/reference etc), where it is the paths penultimate node. 
	 */
	private static String getAttributeName(Path p) {
		String attr = p.getLastName();
		return "value".equals(attr) || "reference".equals(attr) || "globalReference".equals(attr) ? 
				p.getNames().get(p.getNames().size() - 2) : 
				attr;
	}
	
	@Deprecated // Since 9.11.3
	private static <T extends RosettaModelObject> ComparisonResult validateOnlyExists(T parent, Set<String> fields) {
		@SuppressWarnings("unchecked")
		RosettaMetaData<T> meta = (RosettaMetaData<T>) parent.metaData();
		ValidatorWithArg<? super T, Set<String>> onlyExistsValidator = meta.onlyExistsValidator();
		if (onlyExistsValidator != null) {
			ValidationResult<? extends RosettaModelObject> validationResult = onlyExistsValidator.validate(null, parent, fields);
			// Translate validationResult into comparisonResult
			return validationResult.isSuccess() ?
					ComparisonResult.success() : 
					ComparisonResult.failure(validationResult.getFailureReason().orElse(""));
		} else {
			return ComparisonResult.success();
		}
	}
	
	/**
	 * DoIf implementation for Mappers
	 */
	public static <T, A extends Mapper<T>> A doIf(Mapper<Boolean> test, Supplier<A> ifthen, Supplier<A> elsethen) {
		boolean testResult = test.getMulti().stream().allMatch(Boolean::booleanValue);
		if (testResult) return ifthen.get();
		else return elsethen.get();
	}
	@SuppressWarnings("unchecked")
	public static <T, A extends Mapper<T>> A doIf(Mapper<Boolean> test, Supplier<A> ifthen) {
		return doIf(test, ifthen, () -> (A) MapperS.of((T) null));
	}
	
	
	/**
	 * DoIf implementation for ComparisonResult.
	 */
	public static ComparisonResult resultDoIf(Mapper<Boolean> test, Supplier<Mapper<Boolean>> ifthen, Supplier<Mapper<Boolean>> elsethen) {
		boolean testResult = test.getMulti().stream().allMatch(Boolean::booleanValue);
		if (testResult) {
			return toComparisonResult(ifthen.get());
		} else {
			return toComparisonResult(elsethen.get());
		}
	}
	
	public static ComparisonResult resultDoIf(Mapper<Boolean> test, Supplier<Mapper<Boolean>> ifthen) {
		return resultDoIf(test, ifthen, () -> ComparisonResult.success());
	}
	
	private static ComparisonResult toComparisonResult(Mapper<Boolean> mapper) {
		if (mapper instanceof ComparisonResult) {
			return (ComparisonResult) mapper;
		} else {
			return mapper.getMulti().stream().allMatch(Boolean::booleanValue) ? ComparisonResult.success() : ComparisonResult.failure("");
		}
	}
	
	interface CompareFunction<T, U> {
	    ComparisonResult apply(T t, U u, CardinalityOperator o);
	}
	
	// areEqual
	
	public static <T, U> ComparisonResult areEqual(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
		return ExpressionEqualityUtilNullSafe.evaluate(m1, m2, o, ExpressionEqualityUtilNullSafe::areEqual);
	}
	
	// notEqual
		
	public static <T, U> ComparisonResult notEqual(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
		return ExpressionEqualityUtilNullSafe.evaluate(m1, m2, o, ExpressionEqualityUtilNullSafe::notEqual);
	}
	
	public static <T extends Comparable<? super T>> ComparisonResult notEqual(ComparisonResult r1, ComparisonResult r2) {
		return r1.get() != r2.get() ? ComparisonResult.success() : ComparisonResult.failure("Results are not equal");
	}
	
	// greaterThan
		
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThan(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
		return ExpressionCompareUtilNullSafe.evaluate(m1, m2, o, ExpressionCompareUtilNullSafe::greaterThan);
	}
	
	// greaterThanEquals
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThanEquals(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
		return ExpressionCompareUtilNullSafe.evaluate(m1, m2, o, ExpressionCompareUtilNullSafe::greaterThanEquals);
	}
	
	// lessThan

	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThan(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o)  {
		return ExpressionCompareUtilNullSafe.evaluate(m1, m2, o, ExpressionCompareUtilNullSafe::lessThan);
	}
	
	// lessThanEquals

	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThanEquals(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o)  {
		return ExpressionCompareUtilNullSafe.evaluate(m1, m2, o, ExpressionCompareUtilNullSafe::lessThanEquals);
	}

	// contains
	
	public static <T> ComparisonResult contains(Mapper<? extends T> o1, Mapper<? extends T> o2) {
		if (o1.getMulti().isEmpty()) {
			return ComparisonResult.failure("Empty list does not contain all of " +formatMultiError(o2));
		}
		if (o2.getMulti().isEmpty()) {
			return ComparisonResult.failure(formatMultiError(o1) + " does not contain empty list");
		}
		boolean result =  o1.getMulti().containsAll(o2.getMulti());
		if (result) {
			return ComparisonResult.success();
		}
		else {
			return ComparisonResult.failure(formatMultiError(o1) + " does not contain all of " +formatMultiError(o2));
		}
	}
	
	// disjoint
	
	public static <T> ComparisonResult disjoint(Mapper<T> o1, Mapper<T> o2) {
		List<T> multi2 = o2.getMulti();
		List<T> multi1 = o1.getMulti();
		boolean result =  Collections.disjoint(multi1, multi2);
		if (result) {
			return ComparisonResult.success();
		}
		else {
			Collection<T> common = multi1.stream().filter(multi2::contains).collect(Collectors.toSet());
			return ComparisonResult.failure(formatMultiError(o1) + " is not disjoint from " +formatMultiError(o2) + "common items are " + common);
		}
	}
	
	// distinct
	
	public static <T> MapperC<T> distinct(Mapper<T> o) {
		return MapperC.of(o.getMulti()
				.stream()
				.distinct()
				.collect(Collectors.toList()));
	}
	
	public static ComparisonResult checkCardinality(String msgPrefix, int actual, int min, int max) {
		if (actual < min) {
			if(actual == 0){
				return ComparisonResult
						.failure("'" + msgPrefix + "' is a required field but does not exist.");
			}
			else {
				return ComparisonResult
						.failure("Minimum of " + min + " '" + msgPrefix + "' is expected but found " + actual + ".");
			}
		} else if (max > 0 && actual > max) {
			return ComparisonResult
					.failure("Maximum of " + max + " '" + msgPrefix + "' are expected but found " + actual + ".");
		}
		return ComparisonResult.success();
	}

	public static ComparisonResult checkString(String msgPrefix, String value, int minLength, Optional<Integer> maxLength, Optional<Pattern> pattern) {
		if (value == null) {
			return ComparisonResult.success();
		}
		List<String> failures = new ArrayList<>();
		if (value.length() < minLength) {
			failures.add("Field '" + msgPrefix + "' requires a value with minimum length of " + minLength + " characters but value '" + value + "' has length of " + value.length() + " characters.");

		}
		if (maxLength.isPresent()) {
			int m = maxLength.get();
			if (value.length() > m) {
				failures.add("Field '" + msgPrefix + "' must have a value with maximum length of " + m + " characters but value '" + value + "' has length of " + value.length() + " characters.");
			}
		}
		if (pattern.isPresent()) {
			Pattern p = pattern.get();
			Matcher match = p.matcher(value);
			if (!match.matches()) {
				failures.add("Field '" + msgPrefix + "' with value '"+ value + "' does not match the pattern /" + p.toString() + "/.");

			}
		}
		if (failures.isEmpty()) {
			return ComparisonResult.success();
		}
		return ComparisonResult.failure(
					failures.stream().collect(Collectors.joining(" "))
				);
	}
	public static ComparisonResult checkString(String msgPrefix, List<String> values, int minLength, Optional<Integer> maxLength, Optional<Pattern> pattern) {
		if (values == null) {
			return ComparisonResult.success();
		}
		List<String> failures = values.stream()
				.map(v -> checkString(msgPrefix, v, minLength, maxLength, pattern))
				.filter(r -> !r.get())
				.map(r -> r.getError())
				.collect(Collectors.toList());
		if (failures.isEmpty()) {
			return ComparisonResult.success();
		}
		return ComparisonResult.failure(
				failures.stream().collect(Collectors.joining(" - "))
			);
	}
	public static ComparisonResult checkNumber(String msgPrefix, BigDecimal value, Optional<Integer> digits, Optional<Integer> fractionalDigits, Optional<BigDecimal> min, Optional<BigDecimal> max) {
		if (value == null) {
			return ComparisonResult.success();
		}
		List<String> failures = new ArrayList<>();
		if (digits.isPresent()) {
			int d = digits.get();
			BigDecimal normalized = value.stripTrailingZeros();
			int actual = normalized.precision();
			if (normalized.scale() >= normalized.precision()) {
				// case 0.0012 => `actual` should be 5
				actual = normalized.scale() + 1;
			}
			if (normalized.scale() < 0) {
				// case 12000 => `actual` should include unsignificant zeros
				actual -= normalized.scale();
			}
			if (actual > d) {
				failures.add("Expected a maximum of " + d + " digits for '" + msgPrefix + "', but the number " + value + " has " + actual + ".");
			}
		}
		if (fractionalDigits.isPresent()) {
			int f = fractionalDigits.get();
			BigDecimal normalized = value.stripTrailingZeros();
			int actual = normalized.scale();
			if (normalized.scale() < 0) {
				actual = 0;
			}
			if (actual > f) {
				failures.add("Expected a maximum of " + f + " fractional digits for '" + msgPrefix + "', but the number " + value + " has " + actual + ".");
			}
		}
		if (min.isPresent()) {
			BigDecimal m = min.get();
			if (value.compareTo(m) < 0) {
				failures.add("Expected a number greater than or equal to " + m.toPlainString()+ " for '" + msgPrefix + "', but found " + value + ".");
			}
		}
		if (max.isPresent()) {
			BigDecimal m = max.get();
			if (value.compareTo(m) > 0) {
				failures.add("Expected a number less than or equal to " + m.toPlainString() + " for '" + msgPrefix + "', but found " + value + ".");
			}
		}
		if (failures.isEmpty()) {
			return ComparisonResult.success();
		}
		return ComparisonResult.failure(
					failures.stream().collect(Collectors.joining(" "))
				);
	}
	public static ComparisonResult checkNumber(String msgPrefix, Integer value, Optional<Integer> digits, Optional<Integer> fractionalDigits, Optional<BigDecimal> min, Optional<BigDecimal> max) {
		if (value == null) {
			return ComparisonResult.success();
		}
		return checkNumber(msgPrefix, BigDecimal.valueOf(value), digits, fractionalDigits, min, max);
	}
	public static ComparisonResult checkNumber(String msgPrefix, Long value, Optional<Integer> digits, Optional<Integer> fractionalDigits, Optional<BigDecimal> min, Optional<BigDecimal> max) {
		if (value == null) {
			return ComparisonResult.success();
		}
		return checkNumber(msgPrefix, BigDecimal.valueOf(value), digits, fractionalDigits, min, max);
	}
	public static ComparisonResult checkNumber(String msgPrefix, BigInteger value, Optional<Integer> digits, Optional<Integer> fractionalDigits, Optional<BigDecimal> min, Optional<BigDecimal> max) {
		if (value == null) {
			return ComparisonResult.success();
		}
		return checkNumber(msgPrefix, new BigDecimal(value), digits, fractionalDigits, min, max);
	}
	public static ComparisonResult checkNumber(String msgPrefix, List<? extends Number> values, Optional<Integer> digits, Optional<Integer> fractionalDigits, Optional<BigDecimal> min, Optional<BigDecimal> max) {
		if (values == null) {
			return ComparisonResult.success();
		}
		List<String> failures = values.stream()
				.map(v -> {
					if (v instanceof BigDecimal) {
						return checkNumber(msgPrefix, (BigDecimal)v, digits, fractionalDigits, min, max);
					}
					return checkNumber(msgPrefix, v.longValue(), digits, fractionalDigits, min, max);
				})
				.filter(r -> !r.get())
				.map(r -> r.getError())
				.collect(Collectors.toList());
		if (failures.isEmpty()) {
			return ComparisonResult.success();
		}
		return ComparisonResult.failure(
				failures.stream().collect(Collectors.joining(" - "))
			);
	}
	
	private static <T> String formatMultiError(Mapper<T> o) {
		T t = o.getMulti().stream().findAny().orElse(null);
		return t instanceof RosettaModelObject  ? 
				t.getClass().getSimpleName() : // for rosettaModelObjects only log class name otherwise error messages are way too long
				o.getMulti().toString();
	}
	
	// one-of and choice

	@Deprecated // Since 9.7.0
	public static <T> ComparisonResult choice(Mapper<T> mapper, List<String> choiceFieldNames, ValidationResult.ChoiceRuleValidationMethod necessity) {
		return choice(mapper, choiceFieldNames, necessity == ValidationResult.ChoiceRuleValidationMethod.OPTIONAL ? ChoiceRuleValidationMethod.OPTIONAL : ChoiceRuleValidationMethod.REQUIRED);
	}
	public static <T> ComparisonResult choice(Mapper<T> mapper, List<String> choiceFieldNames, ChoiceRuleValidationMethod necessity) {
		T object = mapper.get();
		List<String> populatedFieldNames = new LinkedList<>();
		for (String a: choiceFieldNames) {
			try {
				Method getter = object.getClass().getMethod("get" + StringUtils.capitalize(a));
				if (ExistenceChecker.isSet(getter.invoke(object))) {
					populatedFieldNames.add(a);
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			}
		}
				
		if (necessity.check(populatedFieldNames.size())) {
			return ComparisonResult.success();
		}
		String definition = choiceFieldNames.stream()
			.collect(Collectors.joining("', '", necessity.getDescription() + " of '", "'. "));
		String errorMessage = definition + (populatedFieldNames.isEmpty() ? "No fields are set." :
			populatedFieldNames.stream().collect(Collectors.joining("', '", "Set fields are '", "'.")));
		return ComparisonResult.failure(errorMessage);
	}
}