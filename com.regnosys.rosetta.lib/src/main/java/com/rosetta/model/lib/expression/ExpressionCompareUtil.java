package com.rosetta.model.lib.expression;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import com.rosetta.model.lib.expression.ExpressionOperators.CompareFunction;
import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperGroupBy;
import com.rosetta.model.lib.mapper.MapperS;

class ExpressionCompareUtil {
	
	/**
	 * Checks whether given mappers are both groupBy functions, if not handles as ungrouped mappers.
	 * @param m1
	 * @param m2
	 * @param func - any comparsions function
	 * @return result of equality comparison, with error messages if failure
	 */
	@SuppressWarnings("unchecked")
	static <T extends Comparable<? super T>,X extends Comparable<? super X>,G> ComparisonResult evaluate(Mapper<T> m1, Mapper<X> m2, CardinalityOperator o, CompareFunction<Mapper<T>, Mapper<X>> func) {
		if(m1 instanceof MapperGroupBy && m2 instanceof MapperGroupBy) {
			return evaluateGroupBy((MapperGroupBy<T, G>) m1, (MapperGroupBy<X, G>) m2, o, func);
		}
		else {
			return func.apply(m1, m2, o);
		}
	}

	private static <G, T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult evaluateGroupBy(MapperGroupBy<T, G> g1, MapperGroupBy<X, G> g2, CardinalityOperator o, CompareFunction<Mapper<T>, Mapper<X>> func) {
		Map<MapperS<G>, Mapper<T>> map1 = g1.getGroups();
		Set<MapperS<G>> groupByMappers1 = map1.keySet();
		Set<G> groupByObjects1 = groupByMappers1.stream()
				.map(Mapper::get)
				.collect(Collectors.toSet());
		
		Map<MapperS<G>, Mapper<X>> map2 = g2.getGroups();
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
	
	// Comparison functions
	
	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult greaterThan(Mapper<T> m1, Mapper<X> m2, CardinalityOperator o) {
		BinaryOperator<X> op = o == CardinalityOperator.All ? 
				(a,b) -> a.compareTo(b)>0?a:b : // for All, reduce to the biggest number
				(a,b) -> a.compareTo(b)<0?a:b;  // for Any, reduce to the smallest number
		return compare(m1, m2, o, op, (a,b)->CompareHelper.compare(a, b)>0, ">");
	}

	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult greaterThanEquals(Mapper<T> m1, Mapper<X> m2, CardinalityOperator o) {
		BinaryOperator<X> op =  o == CardinalityOperator.All ? 
				(a,b) -> a.compareTo(b)>0?a:b :
				(a,b) -> a.compareTo(b)<0?a:b;
		return compare(m1, m2, o, op, (a,b)->CompareHelper.compare(a, b)>=0, ">=");
	}

	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult lessThan(Mapper<T> m1, Mapper<X> m2, CardinalityOperator o)  {
		BinaryOperator<X> op =  o == CardinalityOperator.All ? 
				(a,b) -> a.compareTo(b)<0?a:b : // for All, reduce to the smallest number
				(a,b) -> a.compareTo(b)>0?a:b;  // for Any, reduce to the biggest number
		return compare(m1, m2, o, op, (a,b)->CompareHelper.compare(a, b)<0, "<");
	}

	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult lessThanEquals(Mapper<T> m1, Mapper<X> m2, CardinalityOperator o)  {
		BinaryOperator<X> op =  o == CardinalityOperator.All ? 
				(a,b) -> a.compareTo(b)<0?a:b :
				(a,b) -> a.compareTo(b)>0?a:b;
		return compare(m1, m2, o, op, (a,b)->CompareHelper.compare(a, b)<=0, "<=");
	}
	
	private static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult compare(Mapper<T> m1, Mapper<X> m2, CardinalityOperator o, BinaryOperator<X> reducer, BiPredicate<T, X> comparator, String comparatorString) {
		if(m1 == null || m2 == null) {
			return ComparisonResult.failureEmptyOperand("Null operand: [" + m1 + "] " + comparatorString + " [" + m2 + "]");
		}
		
		if(m2.resultCount() == 0 || m1.resultCount() == 0) {
			return ComparisonResult.failureEmptyOperand("Null operand: [" + m1.getPaths() + " : " + m1.get() + "] " + comparatorString + " [" + m2.getPaths() + " : " + m2.get() + "]");
		}

		Optional<X> compareValue = m2.getMulti().stream().reduce(reducer);

		boolean result = o == CardinalityOperator.All ? 
				m1.getMulti().stream().allMatch(a->comparator.test(a,compareValue.get())) :
				m1.getMulti().stream().anyMatch(a->comparator.test(a,compareValue.get()));

		if (result) {
			return ComparisonResult.success();
		}
		else {
			return ComparisonResult.failure(
					"all elements of paths " + m1.getPaths() + " values " + m1.getMulti() + " " +
					"are not " + comparatorString + " than " +
					"all elements of paths " + m2.getPaths() + " values " + m2.getMulti());
		}
	}
}