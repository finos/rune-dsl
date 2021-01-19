package com.rosetta.model.lib.expressions;

import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperGroupBy;
import com.rosetta.model.lib.mapper.MapperS;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

class ExpressionCompareUtil {
	
	/**
	 * Checks whether given mappers are both groupBy functions, if not handles as ungrouped mappers.
	 * @param m1
	 * @param m2
	 * @param func - any comparsions function
	 * @return result of equality comparison, with error messages if failure
	 */
	@SuppressWarnings("unchecked")
	static <T extends Comparable<? super T>,X extends Comparable<? super X>,G> ComparisonResult evaluate(Mapper<T> m1, Mapper<X> m2, BiFunction<Mapper<T>, Mapper<X>, ComparisonResult> func) {
		if(m1 instanceof MapperGroupBy && m2 instanceof MapperGroupBy) {
			return evaluateGroupBy((MapperGroupBy<T, G>) m1, (MapperGroupBy<X, G>) m2, func);
		}
		else {
			return func.apply(m1, m2);
		}
	}

	private static <G, T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult evaluateGroupBy(MapperGroupBy<T, G> g1, MapperGroupBy<X, G> g2, BiFunction<Mapper<T>, Mapper<X>, ComparisonResult> func) {
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
			ComparisonResult newResult = func.apply(map1.get(key), map2.get(key));
			result = result.andIgnoreEmptyOperand(newResult);
		}
		return result;
	}
	
	// Comparison functions
	
	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult greaterThan(Mapper<T> o1, Mapper<X> o2) {
		BinaryOperator<X> op = (a,b) -> a.compareTo(b)>0?a:b;
		return compare(o1, o2,op,(a,b)->CompareHelper.compare(a, b)>0, ">");
	}

	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult greaterThanEquals(Mapper<T> o1, Mapper<X> o2) {
		BinaryOperator<X> op = (a,b) -> a.compareTo(b)>0?a:b;
		return compare(o1, o2,op,(a,b)->CompareHelper.compare(a, b)>=0, ">=");
	}

	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult lessThan(Mapper<T> o1, Mapper<X> o2)  {
		BinaryOperator<X> op = (a,b) -> a.compareTo(b)<0?a:b;
		return compare(o1, o2,op,(a,b)->CompareHelper.compare(a, b)<0, "<");
	}

	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult lessThanEquals(Mapper<T> o1, Mapper<X> o2)  {
		BinaryOperator<X> op = (a,b) -> a.compareTo(b)<0?a:b;
		return compare(o1, o2,op,(a,b)->CompareHelper.compare(a, b)<=0, "<=");
	}
	
	private static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult compare(Mapper<T> o1, Mapper<X> o2, BinaryOperator<X> reducer, BiPredicate<T, X> comparator, String comparatorString) {
		if(o1 == null || o2 == null) {
			return ComparisonResult.failureEmptyOperand("Null operand: [" + o1 + "] " + comparatorString + " [" + o2 + "]");
		}
		
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
}