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

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;

import com.rosetta.model.lib.expression.ExpressionOperators.CompareFunction;
import com.rosetta.model.lib.mapper.Mapper;

class ExpressionCompareUtil {
//
//	/**
//	 * Checks whether given mappers are both groupBy functions, if not handles as ungrouped mappers.
//	 * @param m1
//	 * @param m2
//	 * @param func - any comparsions function
//	 * @return result of equality comparison, with error messages if failure
//	 */
//	static <T extends Comparable<? super T>,X extends Comparable<? super X>,G> ComparisonResult evaluate(Mapper<T> m1, Mapper<X> m2, CardinalityOperator o, CompareFunction<Mapper<T>, Mapper<X>> func) {
//		return func.apply(m1, m2, o);
//	}
//
//	// Comparison functions
//
//	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult greaterThan(Mapper<T> m1, Mapper<X> m2, CardinalityOperator o) {
//		BinaryOperator<X> op = o == CardinalityOperator.All ?
//				(a,b) -> a.compareTo(b)>0?a:b : // for All, reduce to the biggest number
//				(a,b) -> a.compareTo(b)<0?a:b;  // for Any, reduce to the smallest number
//		return compare(m1, m2, o, op, (a,b)->CompareHelper.compare(a, b)>0, ">");
//	}
//
//	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult greaterThanEquals(Mapper<T> m1, Mapper<X> m2, CardinalityOperator o) {
//		BinaryOperator<X> op =  o == CardinalityOperator.All ?
//				(a,b) -> a.compareTo(b)>0?a:b :
//				(a,b) -> a.compareTo(b)<0?a:b;
//		return compare(m1, m2, o, op, (a,b)->CompareHelper.compare(a, b)>=0, ">=");
//	}
//
//	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult lessThan(Mapper<T> m1, Mapper<X> m2, CardinalityOperator o)  {
//		BinaryOperator<X> op =  o == CardinalityOperator.All ?
//				(a,b) -> a.compareTo(b)<0?a:b : // for All, reduce to the smallest number
//				(a,b) -> a.compareTo(b)>0?a:b;  // for Any, reduce to the biggest number
//		return compare(m1, m2, o, op, (a,b)->CompareHelper.compare(a, b)<0, "<");
//	}
//
//	static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult lessThanEquals(Mapper<T> m1, Mapper<X> m2, CardinalityOperator o)  {
//		BinaryOperator<X> op =  o == CardinalityOperator.All ?
//				(a,b) -> a.compareTo(b)<0?a:b :
//				(a,b) -> a.compareTo(b)>0?a:b;
//		return compare(m1, m2, o, op, (a,b)->CompareHelper.compare(a, b)<=0, "<=");
//	}
//
//	private static <T extends Comparable<? super T>, X extends Comparable<? super X>> ComparisonResult compare(Mapper<T> m1, Mapper<X> m2, CardinalityOperator o, BinaryOperator<X> reducer, BiPredicate<T, X> comparator, String comparatorString) {
//		if(m1 == null || m2 == null) {
//			return ComparisonResult.failure("Null operand: [" + m1 + "] " + comparatorString + " [" + m2 + "]");
//		}
//
//		if(m2.resultCount() == 0 || m1.resultCount() == 0) {
//			return ComparisonResult.failure("Null operand: [" + m1.getPaths() + " : " + m1.get() + "] " + comparatorString + " [" + m2.getPaths() + " : " + m2.get() + "]");
//		}
//
//		Optional<X> compareValue = m2.getMulti().stream().reduce(reducer);
//
//		boolean result = o == CardinalityOperator.All ?
//				m1.getMulti().stream().allMatch(a->comparator.test(a,compareValue.get())) :
//				m1.getMulti().stream().anyMatch(a->comparator.test(a,compareValue.get()));
//
//		if (result) {
//			return ComparisonResult.success();
//		}
//		else {
//			return ComparisonResult.failure(
//					"all elements of paths " + m1.getPaths() + " values " + m1.getMulti() + " " +
//					"are not " + comparatorString + " than " +
//					"all elements of paths " + m2.getPaths() + " values " + m2.getMulti());
//		}
//	}
}