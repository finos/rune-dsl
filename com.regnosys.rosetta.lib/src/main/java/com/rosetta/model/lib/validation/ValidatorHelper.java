package com.rosetta.model.lib.validation;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.functions.Mapper;
import com.rosetta.model.lib.functions.MapperS;
import com.rosetta.model.lib.functions.MapperTree;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;

public class ValidatorHelper {
	
	// notExists
	
	public static <T> ComparisonResult notExists(Mapper<T> o) {
		if (o.resultCount()==0) {
			return ComparisonResult.success();
		}
		return ComparisonResult.failure(o.getPaths() + " does exist and is " + formatMultiError(o));
	}
	
	public static <T> ComparisonResult notExists(MapperTree<T> t) {
		return ValidatorMapperTreeUtil.evaluateTree(t, ValidatorHelper::notExists);
	}
	
	// exists
	
	public static <T> ComparisonResult exists(Mapper<T> o, boolean only) {
		if (o.resultCount()>0) {
			return only ? onlyExists(o) : ComparisonResult.success();
		}
		return ComparisonResult.failure(o.getErrorPaths() + " does not exist");
	}
	
	public static <T> ComparisonResult exists(MapperTree<T> t, boolean only) {
		return ValidatorMapperTreeUtil.evaluateTree(t, only, ValidatorHelper::exists);
	}

	// singleExists
	
	public static <T> ComparisonResult singleExists(Mapper<T> o, boolean only) {
		if (o.resultCount()==1) {
			return only ? onlyExists(o) : ComparisonResult.success();
		}
		
		String error = o.resultCount() > 0 ?
				String.format("Expected single %s but found %s [%s]", o.getPaths(), o.resultCount(), formatMultiError(o)) :
				String.format("Expected single %s but found zero", o.getErrorPaths());
		
		return ComparisonResult.failure(error);
	}
	
	public static <T> ComparisonResult singleExists(MapperTree<T> t, boolean only) {
		return ValidatorMapperTreeUtil.evaluateTree(t, only, ValidatorHelper::singleExists);
	}
	
	// multipleExists
	
	public static <T> ComparisonResult multipleExists(Mapper<T> o, boolean only) {
		if (o.resultCount()>1) {
			return only ? onlyExists(o) : ComparisonResult.success();
		}
		
		String error = o.resultCount() > 0 ?
				String.format("Expected multiple %s but only one [%s]", o.getPaths(), formatMultiError(o)) :
				String.format("Expected multiple %s but found zero", o.getErrorPaths());
				
		return ComparisonResult.failure(error);
	}
	
	public static <T> ComparisonResult multipleExists(MapperTree<T> t, boolean only) {
		return ValidatorMapperTreeUtil.evaluateTree(t, only, ValidatorHelper::multipleExists);
	}
	
	// onlyExists
	
	private static <T> ComparisonResult onlyExists(Mapper<T> o) {
		// Ensure all objects are of type RosettaModelObject
		List<? extends RosettaModelObject> parents = o.getParentMulti().stream()
			    .filter(p -> p instanceof RosettaModelObject)
			    .map (p -> (RosettaModelObject) p)
			    .collect(Collectors.toList());
		// Get the function name
		Optional<String> field = o.getPaths().stream().map(p -> p.getLastName()).distinct().findFirst();
		// Check with onlyExistsValidator
		//TODO work out what the correct rosettPath is here
		return field.map(f -> validateOnlyExists(null, parents, f))
				.orElseThrow(() -> new IllegalArgumentException("Error occurred while checking only exists on " + o));
	}
	
	private static <T extends RosettaModelObject> ComparisonResult validateOnlyExists(RosettaPath path, List<T> parents, String field) {
		ComparisonResult result = ComparisonResult.success();
		for(T parent : parents) {
			@SuppressWarnings("unchecked")
			RosettaMetaData<T> meta = (RosettaMetaData<T>) parent.metaData();
			ValidatorWithArg<? super T, String> onlyExistsValidator = meta.onlyExistsValidator();
			ValidationResult<? extends RosettaModelObject> validationResult = onlyExistsValidator.validate(path, parent, field);
			// Translate validationResult into comparisonResult
			result = result.and(validationResult.isSuccess() ? ComparisonResult.success() : ComparisonResult.failure(validationResult.getFailureReason().orElse("")));
		}
		return result;
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
	 s*/
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
	
	public static <T> ComparisonResult doWhenPresent(Mapper<T> whenPresent, ComparisonResult compare) {
		if(exists(whenPresent, false).get())
			return compare;
		else 
			return ComparisonResult.success();
	}
	
	// areEqual
	
	public static <T, U> ComparisonResult areEqual(Mapper<T> m1, Mapper<U> m2) {
		return ValidatorEqualityUtil.evaluate(m1, m2, ValidatorEqualityUtil::areEqual);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult areEqual(MapperTree<T> t1, MapperTree<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, t2, ValidatorHelper::areEqual);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult areEqual(Mapper<T> t1, MapperTree<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, t2, ValidatorHelper::areEqual);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult areEqual(MapperTree<T> t1, Mapper<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, MapperTree.of(t2), ValidatorHelper::areEqual);
	}
	
	// notEqual
		
	public static <T, U> ComparisonResult notEqual(Mapper<T> m1, Mapper<U> m2) {
		return ValidatorEqualityUtil.evaluate(m1, m2, ValidatorEqualityUtil::notEqual);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult notEqual(MapperTree<T> t1, MapperTree<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, t2, ValidatorHelper::notEqual);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult notEqual(Mapper<T> t1, MapperTree<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, t2, ValidatorHelper::notEqual);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult notEqual(MapperTree<T> t1, Mapper<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, MapperTree.of(t2), ValidatorHelper::notEqual);
	}
	
	public static <T extends Comparable<? super T>> ComparisonResult notEqual(ComparisonResult r1, ComparisonResult r2) {
		return r1.get() != r2.get() ? ComparisonResult.success() : ComparisonResult.failure("Results are not equal");
	}
	
	// greaterThan
		
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThan(Mapper<T> o1, Mapper<U> o2) {
		return ValidatorCompareUtil.evaluate(o1, o2, ValidatorCompareUtil::greaterThan);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThan(MapperTree<T> t1, MapperTree<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, t2, ValidatorHelper::greaterThan);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThan(Mapper<T> t1, MapperTree<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, t2, ValidatorHelper::greaterThan);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThan(MapperTree<T> t1, Mapper<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, MapperTree.of(t2), ValidatorHelper::greaterThan);
	}

	// greaterThanEquals
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThanEquals(Mapper<T> o1, Mapper<U> o2) {
		return ValidatorCompareUtil.evaluate(o1, o2, ValidatorCompareUtil::greaterThanEquals);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThanEquals(MapperTree<T> t1, MapperTree<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, t2, ValidatorHelper::greaterThanEquals);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThanEquals(Mapper<T> t1, MapperTree<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, t2, ValidatorHelper::greaterThanEquals);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThanEquals(MapperTree<T> t1, Mapper<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, MapperTree.of(t2), ValidatorHelper::greaterThanEquals);
	}
	
	// lessThan

	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThan(Mapper<T> o1, Mapper<U> o2)  {
		return ValidatorCompareUtil.evaluate(o1, o2, ValidatorCompareUtil::lessThan);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThan(MapperTree<T> t1, MapperTree<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, t2, ValidatorHelper::lessThan);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThan(Mapper<T> t1, MapperTree<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, t2, ValidatorHelper::lessThan);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThan(MapperTree<T> t1, Mapper<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, MapperTree.of(t2), ValidatorHelper::lessThan);
	}
	
	// lessThanEquals

	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThanEquals(Mapper<T> o1, Mapper<U> o2)  {
		return ValidatorCompareUtil.evaluate(o1, o2, ValidatorCompareUtil::lessThanEquals);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThanEquals(MapperTree<T> t1, MapperTree<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, t2, ValidatorHelper::lessThanEquals);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThanEquals(Mapper<T> t1, MapperTree<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, t2, ValidatorHelper::lessThanEquals);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThanEquals(MapperTree<T> t1, Mapper<U> t2) {
		return ValidatorMapperTreeUtil.evaluateTrees(t1, MapperTree.of(t2), ValidatorHelper::lessThanEquals);
	}

	// contains
	
	public static <T> ComparisonResult contains(Mapper<T> o1, Mapper<T> o2) {
		boolean result =  o1.getMulti().containsAll(o2.getMulti());
		if (result) {
			return ComparisonResult.success();
		}
		else {
			return ComparisonResult.failure(formatMultiError(o1) + " does not contain all of " +formatMultiError(o2));
		}
	}
	
	public static ComparisonResult checkCardinality(String msgPrefix, int actual, int min, int max) {
		if (actual < min) {
			return ComparisonResult
					.failure(msgPrefix + " - Expected cardinality lower bound of [" + min + "] found [" + actual + "]");
		} else if (max > 0 && actual > max) {
			return ComparisonResult
					.failure(msgPrefix + " - Expected cardinality upper bound of [" + max + "] found [" + actual + "]");
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