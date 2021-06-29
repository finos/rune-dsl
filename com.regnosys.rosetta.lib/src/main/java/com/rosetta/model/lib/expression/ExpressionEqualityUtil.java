package com.rosetta.model.lib.expression;

import static com.rosetta.model.lib.expression.ErrorHelper.formatEqualsComparisonResultError;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.rosetta.model.lib.expression.ExpressionOperators.CompareFunction;
import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperGroupBy;
import com.rosetta.model.lib.mapper.MapperS;

class ExpressionEqualityUtil {

	/**
	 * Checks whether given mappers are both groupBy functions, if not handles as ungrouped mappers.
	 * @param m1
	 * @param m2
	 * @param func - areEqual or notEquals (or any other appropriate equality function)
	 * @return result of equality comparison, with error messages if failure
	 */
	@SuppressWarnings("unchecked")
	static <T, U, G> ComparisonResult evaluate(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o, CompareFunction<Mapper<T>, Mapper<U>> func) {
		if(m1 instanceof MapperGroupBy && m2 instanceof MapperGroupBy) {
			return evaluateGroupBy((MapperGroupBy<T, G>) m1, (MapperGroupBy<U, G>) m2, o, func);
		}
		else if (m1 instanceof MapperGroupBy) {
			return evaluateGroupBy((MapperGroupBy<T, G>) m1, m2, o, func);
		}
		else if (m2 instanceof MapperGroupBy) {
			return evaluateGroupBy(m1, (MapperGroupBy<U, G>) m2, o, func);
		}
		else {
			return func.apply(m1, m2, o);
		}
	}

	private static <T, U, G> ComparisonResult evaluateGroupBy(MapperGroupBy<T, G> g1, MapperGroupBy<U, G> g2, CardinalityOperator o, CompareFunction<Mapper<T>, Mapper<U>> func) {
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
			ComparisonResult newResult = func.apply(map1.get(key), map2.get(key), o);
			result = result.andIgnoreEmptyOperand(newResult);
		}
		return result;
	}
	
	private static <T, U, G> ComparisonResult evaluateGroupBy(MapperGroupBy<T, G> g1, Mapper<U> m2, CardinalityOperator o, CompareFunction<Mapper<T>, Mapper<U>> func) {
		Map<MapperS<G>, Mapper<T>> map1 = g1.getGroups();
		Set<MapperS<G>> groupByMappers1 = map1.keySet();
		
		ComparisonResult result = ComparisonResult.success();
		for(MapperS<G> key : groupByMappers1) {
			ComparisonResult newResult = func.apply(map1.get(key), m2, o);
			result = result.andIgnoreEmptyOperand(newResult);
		}
		
		return result;
	}
	
	private static <T, U, G> ComparisonResult evaluateGroupBy(Mapper<T> m1, MapperGroupBy<U, G> g2, CardinalityOperator o, CompareFunction<Mapper<T>, Mapper<U>> func) {
		Map<MapperS<G>, Mapper<U>> map2 = g2.getGroups();
		Set<MapperS<G>> groupByMappers2 = map2.keySet();
		
		ComparisonResult result = ComparisonResult.success();
		for(MapperS<G> key : groupByMappers2) {
			ComparisonResult newResult = func.apply(m1, map2.get(key), o);
			result = result.andIgnoreEmptyOperand(newResult);
		}
		
		return result;
	}
	
	/**
	 * Checks that all items in the given mappers are equal.
	 * @param m1
	 * @param m2
	 * @return result of equality comparison, with error messages if failure
	 */
	@SuppressWarnings("unchecked")
	static <T, U> ComparisonResult areEqual(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
		if(m1.getClass().equals(m2.getClass())) {
			return areEqualSame(m1, (Mapper<T>)m2, o);
		}
		else if(m1 instanceof MapperS) {
			return areEqualDifferent((MapperC<U>)m2, (MapperS<T>)m1, o);
		}
		else {
			return areEqualDifferent((MapperC<T>)m1, (MapperS<U>)m2, o);
		}
	}
	
	private static <T> ComparisonResult areEqualSame(Mapper<T> m1, Mapper<T> m2, CardinalityOperator o) {
		List<T> multi1 = m1.getMulti();
		List<T> multi2 = m2.getMulti();
		
		ListIterator<T> e1 = multi1.listIterator();
		ListIterator<T> e2 = multi2.listIterator();
		
		if (multi1.isEmpty() || multi2.isEmpty())
			return ComparisonResult.failureEmptyOperand(formatEqualsComparisonResultError(m1) + " cannot be compared to " + formatEqualsComparisonResultError(m2));
		
		while (e1.hasNext() && e2.hasNext()) {
			T b1 = e1.next();
			T b2 = e2.next();
			if (b1 instanceof Number && b2 instanceof Number) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				int compRes = CompareHelper.compare((Comparable) b1, (Comparable) b2);
				if (compRes != 0 && o == CardinalityOperator.All) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does not equal " + formatEqualsComparisonResultError(m2));
				}
				if (compRes == 0 && o == CardinalityOperator.Any) {
					return ComparisonResult.success();
				}
			} else {
				boolean equals = b1 == null ? b2 == null : b1.equals(b2);
				if (!equals && o == CardinalityOperator.All) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does not equal " + formatEqualsComparisonResultError(m2));
				}
				if (equals && o == CardinalityOperator.Any) {
					return ComparisonResult.success();
				}
			}	
		}
		
		if (e1.hasNext() || e2.hasNext())
			return ComparisonResult.failureEmptyOperand(formatEqualsComparisonResultError(m1) + " cannot be compared to " + formatEqualsComparisonResultError(m2));
		
		return o == CardinalityOperator.All ? 
				ComparisonResult.success() :
				ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does not equal " + formatEqualsComparisonResultError(m2));
	}
	
	private static <T, U> ComparisonResult areEqualDifferent(MapperC<T> m1, MapperS<U> m2, CardinalityOperator o) {
		List<T> multi1 = m1.getMulti();
		U b2 = m2.get();
		
		if (multi1.isEmpty())
			return ComparisonResult.failureEmptyOperand(formatEqualsComparisonResultError(m1) + " cannot be compared to " + formatEqualsComparisonResultError(m2));
		
		ListIterator<T> e1 = multi1.listIterator();
		
		while (e1.hasNext()) {
			T b1 = e1.next();
			
			if (b1 instanceof Number && b2 instanceof Number) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				int compRes = CompareHelper.compare((Comparable) b1, (Comparable) b2);
				if (compRes != 0 && o == CardinalityOperator.All) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does not equal " + formatEqualsComparisonResultError(m2));
				}
				if (compRes == 0 && o == CardinalityOperator.Any) {
					return ComparisonResult.success();
				}
			} else {
				@SuppressWarnings("unlikely-arg-type")
				boolean equals = b1 == null ? b2 == null : b1.equals(b2);
				if (!equals && o == CardinalityOperator.All) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does not equal " + formatEqualsComparisonResultError(m2));
				}
				if (equals && o == CardinalityOperator.Any) {
					return ComparisonResult.success();
				}
			}
		}
		
		return o == CardinalityOperator.All ? 
				ComparisonResult.success() :
				ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does not equal " + formatEqualsComparisonResultError(m2));
	}
	
	/**
	 * Checks that the given mappers have no equal items.
	 * @param m1
	 * @param m2
	 * @return result of equality comparison, with error messages if failure
	 */
	@SuppressWarnings("unchecked")
	static <T, U> ComparisonResult notEqual(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
		if(m1.getClass().equals(m2.getClass())) {
			return notEqualSame(m1, (Mapper<T>)m2, o);
		}
		else if(m1 instanceof MapperS) {
			return notEqualDifferent((MapperC<U>)m2, (MapperS<T>)m1, o);
		}
		else {
			return notEqualDifferent((MapperC<T>)m1, (MapperS<U>)m2, o);
		}
	}	
	
	private static <T> ComparisonResult notEqualSame(Mapper<T> m1, Mapper<T> m2, CardinalityOperator o) {
		List<T> multi1 = m1.getMulti();
		List<T> multi2 = m2.getMulti();

		ListIterator<T> e1 = multi1.listIterator();
		ListIterator<T> e2 = multi2.listIterator();
		
		if (multi1.isEmpty() || multi2.isEmpty())
			return ComparisonResult.successEmptyOperand(formatEqualsComparisonResultError(m1) + " cannot be compared to " + formatEqualsComparisonResultError(m2));
		
		while (e1.hasNext() && e2.hasNext()) {
			T b1 = e1.next();
			T b2 = e2.next();
			if (b1 instanceof Number && b2 instanceof Number) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				int compRes = CompareHelper.compare((Comparable) b1, (Comparable) b2);
				if (compRes != 0 && o == CardinalityOperator.Any) {
					return ComparisonResult.success();
				}
				if (compRes == 0 && o == CardinalityOperator.All) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does equal " + formatEqualsComparisonResultError(m2));
				}
			} else {
				boolean equals = b1 == null ? b2 == null : b1.equals(b2);
				if (!equals && o == CardinalityOperator.Any) {
					return ComparisonResult.success();
				}
				if (equals && o == CardinalityOperator.All) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does equal " + formatEqualsComparisonResultError(m2));
				}
			}
		}
		
		if (e1.hasNext() || e2.hasNext())
			return ComparisonResult.success();
		
		return o == CardinalityOperator.Any ? 
				ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does equal " + formatEqualsComparisonResultError(m2)) :
				ComparisonResult.success();

	}
	
	private static <T, U> ComparisonResult notEqualDifferent(MapperC<T> m1, MapperS<U> m2, CardinalityOperator o) {
		List<T> multi1 = m1.getMulti();
		U b2 = m2.get();

		if (multi1.isEmpty())
			return ComparisonResult.successEmptyOperand(formatEqualsComparisonResultError(m1) + " cannot be compared to " + formatEqualsComparisonResultError(m2));
		
		ListIterator<T> e1 = multi1.listIterator();
		
		while (e1.hasNext()) {
			T b1 = e1.next();
			
			if (b1 instanceof Number && b2 instanceof Number) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				int compRes = CompareHelper.compare((Comparable) b1, (Comparable) b2);
				if (compRes != 0 && o == CardinalityOperator.Any) {
					return ComparisonResult.success();
				}
				if (compRes == 0 && o == CardinalityOperator.All) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does equal " + formatEqualsComparisonResultError(m2));
				}
			} else {
				@SuppressWarnings("unlikely-arg-type")
				boolean equals = b1 == null ? b2 == null : b1.equals(b2);
				if (!equals && o == CardinalityOperator.Any) {
					return ComparisonResult.success();
				}
				if (equals && o == CardinalityOperator.All) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does equal " + formatEqualsComparisonResultError(m2));
				}
			}
				
		}

		return o == CardinalityOperator.Any ? 
				ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does equal " + formatEqualsComparisonResultError(m2)) :
				ComparisonResult.success();
	}
}
