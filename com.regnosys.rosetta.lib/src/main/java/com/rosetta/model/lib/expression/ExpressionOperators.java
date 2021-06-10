package com.rosetta.model.lib.expression;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.mapper.MapperTree;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
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
	
	public static <T> ComparisonResult notExists(MapperTree<T> t) {
		return ExpressionsMapperTreeUtil.evaluateTree(t, ExpressionOperators::notExists);
	}
	
	// exists
	
	public static <T> ComparisonResult exists(Mapper<T> o, boolean only) {
		if (o.resultCount()>0) {
			return only ? onlyExists(o) : ComparisonResult.success();
		}
		return ComparisonResult.failure(o.getErrorPaths() + " does not exist");
	}
	
	public static <T> ComparisonResult exists(MapperTree<T> t, boolean only) {
		return ExpressionsMapperTreeUtil.evaluateTree(t, only, ExpressionOperators::exists);
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
		return ExpressionsMapperTreeUtil.evaluateTree(t, only, ExpressionOperators::singleExists);
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
		return ExpressionsMapperTreeUtil.evaluateTree(t, only, ExpressionOperators::multipleExists);
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
			if (onlyExistsValidator != null) {
				ValidationResult<? extends RosettaModelObject> validationResult = onlyExistsValidator.validate(path, parent, field);
				// Translate validationResult into comparisonResult
				result = result.and(validationResult.isSuccess() ? ComparisonResult.success() : ComparisonResult.failure(validationResult.getFailureReason().orElse("")));
			} else {
				result = result.and(ComparisonResult.success());
			}
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
	
	// areEqual
	
	public static <T, U> ComparisonResult areEqual(Mapper<T> m1, Mapper<U> m2) {
		return ExpressionEqualityUtil.evaluate(m1, m2, ExpressionEqualityUtil::areEqual);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult areEqual(MapperTree<T> t1, MapperTree<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, t2, ExpressionOperators::areEqual);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult areEqual(Mapper<T> t1, MapperTree<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, t2, ExpressionOperators::areEqual);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult areEqual(MapperTree<T> t1, Mapper<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, MapperTree.of(t2), ExpressionOperators::areEqual);
	}
	
	// notEqual
		
	public static <T, U> ComparisonResult notEqual(Mapper<T> m1, Mapper<U> m2) {
		return ExpressionEqualityUtil.evaluate(m1, m2, ExpressionEqualityUtil::notEqual);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult notEqual(MapperTree<T> t1, MapperTree<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, t2, ExpressionOperators::notEqual);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult notEqual(Mapper<T> t1, MapperTree<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, t2, ExpressionOperators::notEqual);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult notEqual(MapperTree<T> t1, Mapper<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, MapperTree.of(t2), ExpressionOperators::notEqual);
	}
	
	public static <T extends Comparable<? super T>> ComparisonResult notEqual(ComparisonResult r1, ComparisonResult r2) {
		return r1.get() != r2.get() ? ComparisonResult.success() : ComparisonResult.failure("Results are not equal");
	}
	
	// greaterThan
		
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThan(Mapper<T> o1, Mapper<U> o2) {
		return ExpressionCompareUtil.evaluate(o1, o2, ExpressionCompareUtil::greaterThan);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThan(MapperTree<T> t1, MapperTree<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, t2, ExpressionOperators::greaterThan);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThan(Mapper<T> t1, MapperTree<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, t2, ExpressionOperators::greaterThan);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThan(MapperTree<T> t1, Mapper<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, MapperTree.of(t2), ExpressionOperators::greaterThan);
	}

	// greaterThanEquals
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThanEquals(Mapper<T> o1, Mapper<U> o2) {
		return ExpressionCompareUtil.evaluate(o1, o2, ExpressionCompareUtil::greaterThanEquals);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThanEquals(MapperTree<T> t1, MapperTree<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, t2, ExpressionOperators::greaterThanEquals);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThanEquals(Mapper<T> t1, MapperTree<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, t2, ExpressionOperators::greaterThanEquals);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult greaterThanEquals(MapperTree<T> t1, Mapper<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, MapperTree.of(t2), ExpressionOperators::greaterThanEquals);
	}
	
	// lessThan

	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThan(Mapper<T> o1, Mapper<U> o2)  {
		return ExpressionCompareUtil.evaluate(o1, o2, ExpressionCompareUtil::lessThan);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThan(MapperTree<T> t1, MapperTree<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, t2, ExpressionOperators::lessThan);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThan(Mapper<T> t1, MapperTree<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, t2, ExpressionOperators::lessThan);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThan(MapperTree<T> t1, Mapper<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, MapperTree.of(t2), ExpressionOperators::lessThan);
	}
	
	// lessThanEquals

	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThanEquals(Mapper<T> o1, Mapper<U> o2)  {
		return ExpressionCompareUtil.evaluate(o1, o2, ExpressionCompareUtil::lessThanEquals);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThanEquals(MapperTree<T> t1, MapperTree<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, t2, ExpressionOperators::lessThanEquals);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThanEquals(Mapper<T> t1, MapperTree<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, t2, ExpressionOperators::lessThanEquals);
	}
	
	public static <T extends Comparable<? super T>, U extends Comparable<? super U>> ComparisonResult lessThanEquals(MapperTree<T> t1, Mapper<U> t2) {
		return ExpressionsMapperTreeUtil.evaluateTrees(t1, MapperTree.of(t2), ExpressionOperators::lessThanEquals);
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