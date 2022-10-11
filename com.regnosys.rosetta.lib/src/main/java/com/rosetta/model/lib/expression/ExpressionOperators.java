package com.rosetta.model.lib.expression;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.Mapper.Path;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.ValidatorWithArg;

public class ExpressionOperators {
	
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
				.map(ExpressionOperators::getAttributeName)
				.collect(Collectors.toSet());
		
		// The number of attributes to check, should equal the number of mappers
		if (fields.size() != o.size()) {
			return ComparisonResult.failure("All required fields not set.");
		}
		
		// Run validation then and results together 
		return parents.stream()
			.map(p -> validateOnlyExists(p, fields))
			.reduce(ComparisonResult.success(), (a, b) -> a.and(b));
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
		return ExpressionEqualityUtil.evaluate(m1, m2, o, ExpressionEqualityUtil::areEqual);
	}
	
	// notEqual
		
	public static <T, U> ComparisonResult notEqual(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
		return ExpressionEqualityUtil.evaluate(m1, m2, o, ExpressionEqualityUtil::notEqual);
	}
	
	public static <T extends Comparable<? super T>> ComparisonResult notEqual(ComparisonResult r1, ComparisonResult r2) {
		return r1.get() != r2.get() ? ComparisonResult.success() : ComparisonResult.failure("Results are not equal");
	}
	
	// greaterThan
		
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThan(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
		return ExpressionCompareUtil.evaluate(m1, m2, o, ExpressionCompareUtil::greaterThan);
	}
	
	// greaterThanEquals
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThanEquals(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
		return ExpressionCompareUtil.evaluate(m1, m2, o, ExpressionCompareUtil::greaterThanEquals);
	}
	
	// lessThan

	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThan(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o)  {
		return ExpressionCompareUtil.evaluate(m1, m2, o, ExpressionCompareUtil::lessThan);
	}
	
	// lessThanEquals

	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThanEquals(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o)  {
		return ExpressionCompareUtil.evaluate(m1, m2, o, ExpressionCompareUtil::lessThanEquals);
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
			return ComparisonResult
					.failure("Minimum of " + min + " '" + msgPrefix + "' is expected but found " + actual + "");
		} else if (max > 0 && actual > max) {
			return ComparisonResult
					.failure("Maximum of " + max + " '" + msgPrefix + "' are expected but found " + actual + "");
		}
		return ComparisonResult.success();
	}
	
	private static <T> String formatMultiError(Mapper<T> o) {
		T t = o.getMulti().stream().findAny().orElse(null);
		return t instanceof RosettaModelObject  ? 
				t.getClass().getSimpleName() : // for rosettaModelObjects only log class name otherwise error messages are way too long
				o.getMulti().toString();
	}
}