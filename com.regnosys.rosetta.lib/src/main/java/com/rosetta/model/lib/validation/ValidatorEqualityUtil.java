package com.rosetta.model.lib.validation;

import static com.rosetta.model.lib.validation.ErrorHelper.formatEqualsComparisonResultError;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.rosetta.model.lib.functions.Mapper;
import com.rosetta.model.lib.functions.MapperC;
import com.rosetta.model.lib.functions.MapperGroupBy;
import com.rosetta.model.lib.functions.MapperS;

class ValidatorEqualityUtil {

	/**
	 * Checks whether given mappers are both groupBy functions, if not handles as ungrouped mappers.
	 * @param m1
	 * @param m2
	 * @param func - areEqual or notEquals (or any other appropriate equality function)
	 * @return result of equality comparison, with error messages if failure
	 */
	@SuppressWarnings("unchecked")
	static <T, U, G> ComparisonResult evaluate(Mapper<T> m1, Mapper<U> m2, BiFunction<Mapper<T>, Mapper<U>, ComparisonResult> func) {
		if(m1 instanceof MapperGroupBy && m2 instanceof MapperGroupBy) {
			return evaluateGroupBy((MapperGroupBy<T, G>) m1, (MapperGroupBy<U, G>) m2, func);
		}
		else if (m1 instanceof MapperGroupBy) {
			return evaluateGroupBy((MapperGroupBy<T, G>) m1, m2, func);
		}
		else if (m2 instanceof MapperGroupBy) {
			return evaluateGroupBy(m1, (MapperGroupBy<U, G>) m2, func);
		}
		else {
			return func.apply(m1, m2);
		}
	}

	private static <T, U, G> ComparisonResult evaluateGroupBy(MapperGroupBy<T, G> g1, MapperGroupBy<U, G> g2, BiFunction<Mapper<T>, Mapper<U>, ComparisonResult> func) {
		Map<MapperS<G>, Mapper<T>> map1 = g1.getGroups();
		Set<MapperS<G>> groupByMappers1 = map1.keySet();
		Set<G> groupByObjects1 = groupByMappers1.stream()
				.map(Mapper::get)
				.collect(Collectors.toSet());
		
		Map<MapperS<G>, Mapper<U>> map2 = g2.getGroups();
		Set<MapperS<G>> groupByMappers2 = map2.keySet();
		Set<G> groupByObjects2 = groupByMappers2.stream()
				.map(Mapper::get)
				.collect(Collectors.toSet());
		
		if (!groupByObjects1.equals(groupByObjects2)) {
			return ComparisonResult.failureEmptyOperand(ErrorHelper.formatGroupByMismatchError(g1, g2));
		}
		
		ComparisonResult result = ComparisonResult.success();
		for(MapperS<G> key : groupByMappers1) {
			ComparisonResult newResult = func.apply(map1.get(key), map2.get(key));
			result = result.andIgnoreEmptyOperand(newResult);
		}
		return result;
	}
	
	private static <T, U, G> ComparisonResult evaluateGroupBy(MapperGroupBy<T, G> g1, Mapper<U> m2, BiFunction<Mapper<T>, Mapper<U>, ComparisonResult> func) {
		Map<MapperS<G>, Mapper<T>> map1 = g1.getGroups();
		Set<MapperS<G>> groupByMappers1 = map1.keySet();
		
		ComparisonResult result = ComparisonResult.success();
		for(MapperS<G> key : groupByMappers1) {
			ComparisonResult newResult = func.apply(map1.get(key), m2);
			result = result.andIgnoreEmptyOperand(newResult);
		}
		
		return result;
	}
	
	private static <T, U, G> ComparisonResult evaluateGroupBy(Mapper<T> m1, MapperGroupBy<U, G> g2, BiFunction<Mapper<T>, Mapper<U>, ComparisonResult> func) {
		Map<MapperS<G>, Mapper<U>> map2 = g2.getGroups();
		Set<MapperS<G>> groupByMappers2 = map2.keySet();
		
		ComparisonResult result = ComparisonResult.success();
		for(MapperS<G> key : groupByMappers2) {
			ComparisonResult newResult = func.apply(m1, map2.get(key));
			result = result.andIgnoreEmptyOperand(newResult);
		}
		
		return result;
	}
	
	/**
	 * Checks that all items in the given mappers are equal.
	 * @param o1
	 * @param o2
	 * @return result of equality comparison, with error messages if failure
	 */
	@SuppressWarnings("unchecked")
	static <T, U> ComparisonResult areEqual(Mapper<T> o1, Mapper<U> o2) {
		if(o1.getClass().equals(o2.getClass())) {
			return areEqualSame(o1, (Mapper<T>)o2);
		}
		else if(o1 instanceof MapperS) {
			return areEqualDifferent((MapperC<U>)o2, (MapperS<T>)o1);
		}
		else {
			return areEqualDifferent((MapperC<T>)o1, (MapperS<U>)o2);
		}
	}
	
	private static <T> ComparisonResult areEqualSame(Mapper<T> o1, Mapper<T> o2) {
		List<T> multi1 = o1.getMulti();
		List<T> multi2 = o2.getMulti();
		
		ListIterator<T> e1 = multi1.listIterator();
		ListIterator<T> e2 = multi2.listIterator();
		
		if (multi1.isEmpty() || multi2.isEmpty())
			return ComparisonResult.failureEmptyOperand(formatEqualsComparisonResultError(o1) + " cannot be compared to " + formatEqualsComparisonResultError(o2));
		
		
		while (e1.hasNext() && e2.hasNext()) {
			T b1 = e1.next();
			T b2 = e2.next();
			if (b1 instanceof Number && b2 instanceof Number) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				int compRes = CompareHelper.compare((Comparable) b1, (Comparable) b2);
				if (compRes != 0) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(o1) + " does not equal " + formatEqualsComparisonResultError(o2));
				}
			} else if (!(b1 == null ? b2 == null : b1.equals(b2)))
				return ComparisonResult.failure(formatEqualsComparisonResultError(o1) + " does not equal " + formatEqualsComparisonResultError(o2));
		}
		
		if (e1.hasNext() || e2.hasNext())
			return ComparisonResult.failureEmptyOperand(formatEqualsComparisonResultError(o1) + " cannot be compared to " + formatEqualsComparisonResultError(o2));
		
		return ComparisonResult.success();
	}
	
	private static <T, U> ComparisonResult areEqualDifferent(MapperC<T> o1, MapperS<U> o2) {
		List<T> multi1 = o1.getMulti();
		U b2 = o2.get();
		
		if (multi1.isEmpty())
			return ComparisonResult.failureEmptyOperand(formatEqualsComparisonResultError(o1) + " cannot be compared to " + formatEqualsComparisonResultError(o2));
		
		ListIterator<T> e1 = multi1.listIterator();
		
		while (e1.hasNext()) {
			T b1 = e1.next();
			
			if (b1 instanceof Number && b2 instanceof Number) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				int compRes = CompareHelper.compare((Comparable) b1, (Comparable) b2);
				if (compRes != 0) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(o1) + " does not equal " + formatEqualsComparisonResultError(o2));
				}
			} else if (!(b1 == null ? b2 == null : b1.equals(b2)))
				return ComparisonResult.failure(formatEqualsComparisonResultError(o1) + " does not equal " + formatEqualsComparisonResultError(o2));
		}
		
		return ComparisonResult.success();
	}
	
	/**
	 * Checks that the given mappers have no equal items.
	 * @param o1
	 * @param o2
	 * @return result of equality comparison, with error messages if failure
	 */
	@SuppressWarnings("unchecked")
	static <T, U> ComparisonResult notEqual(Mapper<T> o1, Mapper<U> o2) {
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
				int compRes = CompareHelper.compare((Comparable) b1, (Comparable) b2);
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
				int compRes = CompareHelper.compare((Comparable) b1, (Comparable) b2);
				if (compRes != 0) {
					return ComparisonResult.success();
				}
			} else if (!(b1 == null ? b2 == null : b1.equals(b2)))
				return ComparisonResult.success();
		}

		return ComparisonResult.failure(formatEqualsComparisonResultError(o1) + " does equal " + formatEqualsComparisonResultError(o2));
	}
}
