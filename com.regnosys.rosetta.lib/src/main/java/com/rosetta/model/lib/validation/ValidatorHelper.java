package com.rosetta.model.lib.validation;

import java.math.BigDecimal;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.functions.Mapper;
import com.rosetta.model.lib.functions.MapperC;
import com.rosetta.model.lib.functions.MapperS;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;

public class ValidatorHelper {
	
	public static <T> ComparisonResult notExists(Mapper<T> o) {
		if (o.resultCount()==0) {
			return ComparisonResult.success();
		}
		return ComparisonResult.failure(o.getPaths() + " does exist and is " + formatMultiError(o));
	}
	
	public static <T> ComparisonResult exists(Mapper<T> o, boolean only) {
		if (o.resultCount()>0) {
			return only ? onlyExists(o) : ComparisonResult.success();
		}
		return ComparisonResult.failure(o.getErrorPaths() + " does not exist");
	}

	public static <T> ComparisonResult singleExists(Mapper<T> o, boolean only) {
		if (o.resultCount()==1) {
			return only ? onlyExists(o) : ComparisonResult.success();
		}
		
		String error = o.resultCount() > 0 ?
				String.format("Expected single %s but found %s [%s]", o.getPaths(), o.resultCount(), formatMultiError(o)) :
				String.format("Expected single %s but found zero", o.getErrorPaths());
		
		return ComparisonResult.failure(error);
	}
	
	public static <T> ComparisonResult multipleExists(Mapper<T> o, boolean only) {
		if (o.resultCount()>1) {
			return only ? onlyExists(o) : ComparisonResult.success();
		}
		
		String error = o.resultCount() > 0 ?
				String.format("Expected multiple %s but only one [%s]", o.getPaths(), formatMultiError(o)) :
				String.format("Expected multiple %s but found zero", o.getErrorPaths());
				
		return ComparisonResult.failure(error);
	}
	
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
	
	public static <T> Mapper<T> doIf(Mapper<Boolean> test, Mapper<T> ifthen, Mapper<T> elsethen) {
		boolean testResult = test.getMulti().stream().allMatch(b->b.booleanValue());
		if (testResult) return ifthen;
		else return elsethen;
	}
	
	public static ComparisonResult doIf(ComparisonResult test, ComparisonResult ifthen, ComparisonResult elsethen) {
		if (test.get()) return ifthen;
		else return elsethen;
	}

	public static <T> Mapper<T> doIf(ComparisonResult test, Mapper<T> ifthen, Mapper<T> elsethen) {	
		if (test.get()) return ifthen;
		else return elsethen;
	}
	
	public static <T> ComparisonResult doWhenPresent(Mapper<T> whenPresent, ComparisonResult compare) {
		if(exists(whenPresent, false).get())
			return compare;
		else 
			return ComparisonResult.success();
	}
	
	public static <T, U> ComparisonResult areEqual(Mapper<T> m1, Mapper<U> m2) {
				return EqualityValidatorHelper.evaluate(m1, m2, EqualityValidatorHelper::areEqual);
	}
		
	@SuppressWarnings("unchecked")
	public static <T, U> ComparisonResult notEqual(Mapper<T> o1, Mapper<U> o2) {
		if(o1.getClass().equals(o2.getClass())) {
			return notEqualSame(o1, (Mapper<T>)o2);
		}
		else if(o1 instanceof MapperS) {
			return notEqualDifferent((MapperC<U>)o2, (MapperS<T>)o1);
		}
		else {
			return notEqualDifferent((MapperC<T>)o1, (MapperS<U>)o2);
		}
	}	
	
	private static <T> ComparisonResult notEqualSame(Mapper<T> o1, Mapper<T> o2) {
		List<T> multi1 = o1.getMulti();
		List<T> multi2 = o2.getMulti();

		ListIterator<T> e1 = multi1.listIterator();
		ListIterator<T> e2 = multi2.listIterator();
		
		if (multi1.isEmpty() || multi2.isEmpty())
			return ComparisonResult.successEmptyOperand(formatEqualsComparisonResultError(o1) + " cannot be compared to " + formatEqualsComparisonResultError(o2));
		
		while (e1.hasNext() && e2.hasNext()) {
			T b1 = e1.next();
			T b2 = e2.next();
			if (b1 instanceof Number && b2 instanceof Number) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				int compRes = comp.compare((Comparable) b1, (Comparable) b2);
				if (compRes != 0) {
					return ComparisonResult.success();
				}
			} else if (!(b1 == null ? b2 == null : b1.equals(b2)))
				return ComparisonResult.success();
		}
		
		if (e1.hasNext() || e2.hasNext())
			return ComparisonResult.success();
		
		return ComparisonResult.failure(formatEqualsComparisonResultError(o1) + " does equal " + formatEqualsComparisonResultError(o2));
	}
	
	private static <T, U> ComparisonResult notEqualDifferent(MapperC<T> o1, MapperS<U> o2) {
		List<T> multi1 = o1.getMulti();
		U b2 = o2.get();

		if (multi1.isEmpty())
			return ComparisonResult.successEmptyOperand(formatEqualsComparisonResultError(o1) + " cannot be compared to " + formatEqualsComparisonResultError(o2));
		
		ListIterator<T> e1 = multi1.listIterator();
		
		while (e1.hasNext()) {
			T b1 = e1.next();
			
			if (b1 instanceof Number && b2 instanceof Number) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				int compRes = comp.compare((Comparable) b1, (Comparable) b2);
				if (compRes != 0) {
					return ComparisonResult.success();
				}
			} else if (!(b1 == null ? b2 == null : b1.equals(b2)))
				return ComparisonResult.success();
		}

		return ComparisonResult.failure(formatEqualsComparisonResultError(o1) + " does equal " + formatEqualsComparisonResultError(o2));
	}
	
	private static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult compare(Mapper<T> o1, Mapper<X> o2, BinaryOperator<X> reducer, BiPredicate<T, X> comparator, String comparatorString) {

		if(o2.resultCount() == 0 || o1.resultCount() == 0) {
			return ComparisonResult.failureEmptyOperand("Null operand: [" + o1.getPaths() + " : " + o1.get() + "] " + comparatorString + " [" + o2.getPaths() + " : " + o2.get() + "]");
		}

		Optional<X> compareValue = o2.getMulti().stream().reduce(reducer);

		boolean result= o1.getMulti().stream()
				.allMatch(a->comparator.test(a,compareValue.get()));

		if (result) {
			return ComparisonResult.success();
		}
		else {
			return ComparisonResult.failure(
					"all elements of paths " + o1.getPaths() + " values " + o1.getMulti() + " " +
					"are not " + comparatorString + " than " +
					"all elements of paths " + o2.getPaths() + " values " + o2.getMulti());
		}
	}
	
	public static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult greaterThan(Mapper<T> o1, Mapper<X> o2) {
		return CompareValidatorHelper.evaluate(o1, o2, CompareValidatorHelper::greaterThan);
	}

	public static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult greaterThanEquals(Mapper<T> o1, Mapper<X> o2) {
		return CompareValidatorHelper.evaluate(o1, o2, CompareValidatorHelper::greaterThanEquals);
	}

	public static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult lessThan(Mapper<T> o1, Mapper<X> o2)  {
		return CompareValidatorHelper.evaluate(o1, o2, CompareValidatorHelper::lessThan);
	}

	public static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult lessThanEquals(Mapper<T> o1, Mapper<X> o2)  {
		return CompareValidatorHelper.evaluate(o1, o2, CompareValidatorHelper::lessThanEquals);
	}

	public static <T> ComparisonResult contains(Mapper<T> o1, Mapper<T> o2) {
		boolean result =  o2.getMulti().containsAll(o2.getMulti());
		if (result) {
			return ComparisonResult.success();
		}
		else {
			return ComparisonResult.failure(o1.getMulti() + " does not contain all of "+o2.getMulti());
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
	
	private static <T> String formatEqualsComparisonResultError(Mapper<T> o) {
		return o.resultCount() > 0 ? String.format("%s %s", o.getPaths(), formatMultiError(o)) : o.getErrorPaths().toString();
	}

	private static <T> String formatMultiError(Mapper<T> o) {
		T t = o.getMulti().stream().findAny().orElse(null);
		return t instanceof RosettaModelObject  ? 
				t.getClass().getSimpleName() : // for rosettaModelObjects only log class name otherwise error messages are way too long
				o.getMulti().toString();
	}
	
	private final static MagicComparator comp = new MagicComparator();
	
	private static class MagicComparator {

		@SuppressWarnings("unchecked")
		public <T extends Comparable<? super T>, X extends Comparable<? super X>> int compare(T o1, X o2) {
			if (o1.getClass() == o2.getClass()) {
				return (o1).compareTo((T)o2);
			}
			if (!(o1 instanceof Number && o2 instanceof Number)) {
				throw new IllegalArgumentException("I only know how to compare identical comparable types and numbers not " + 
							o1.getClass().getSimpleName() + " and " + o2.getClass().getSimpleName());
			}
			BigDecimal b1 = toBigD((Number)o1);
			BigDecimal b2 = toBigD((Number)o2);
			return b1.compareTo(b2);
		}
		
		private BigDecimal toBigD(Number n) {
			if (n instanceof BigDecimal) return (BigDecimal)n;
			if (n instanceof Long) return new BigDecimal(n.longValue());
			if (n instanceof Integer) return new BigDecimal(n.intValue());
			throw new IllegalArgumentException("can only convert integer and long to bigD");
		}
	}
}