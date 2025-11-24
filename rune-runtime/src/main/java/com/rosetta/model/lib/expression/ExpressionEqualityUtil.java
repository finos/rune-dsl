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

import static com.rosetta.model.lib.expression.ErrorHelper.formatEqualsComparisonResultError;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.ListIterator;

import com.rosetta.model.lib.expression.ExpressionOperators.CompareFunction;
import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;

class ExpressionEqualityUtil {

	/**
	 * Checks whether given mappers are both groupBy functions, if not handles as ungrouped mappers.
	 * @param m1
	 * @param m2
	 * @param func - areEqual or notEquals (or any other appropriate equality function)
	 * @return result of equality comparison, with error messages if failure
	 */
	static <T, U, G> ComparisonResult evaluate(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o, CompareFunction<Mapper<T>, Mapper<U>> func) {
		return func.apply(m1, m2, o);
	}
	
	/**
	 * Checks that all items in the given mappers are equal.
	 * @param m1
	 * @param m2
	 * @return result of equality comparison, with error messages if failure
	 */
	@SuppressWarnings("unchecked")
	static <T, U> ComparisonResult areEqual(Mapper<T> m1, Mapper<U> m2, CardinalityOperator o) {
		if (m1 instanceof ComparisonResult) {
			m1 = (Mapper<T>) ((ComparisonResult) m1).asMapper();
		}
		if (m2 instanceof ComparisonResult) {
			m2 = (Mapper<U>) ((ComparisonResult) m2).asMapper();
		}
					
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
		
		if (multi1.isEmpty() || multi2.isEmpty()) {
            if (multi1.isEmpty() && multi2.isEmpty()) {
                return ComparisonResult.success();
            }
            return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " cannot be compared to " + formatEqualsComparisonResultError(m2));
        }
		
		while (e1.hasNext() && e2.hasNext()) {
			T b1 = e1.next();
			T b2 = e2.next();
			boolean equals;
			if (b1 instanceof Number && b2 instanceof Number || b1 instanceof ZonedDateTime && b2 instanceof ZonedDateTime) {
				int compRes = CompareHelper.compare(b1, b2);
				equals = compRes == 0;
			} else {
				equals = b1 == null ? b2 == null : b1.equals(b2);
			}
			if (!equals && o == CardinalityOperator.All) {
				return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does not equal " + formatEqualsComparisonResultError(m2));
			} else if (equals && o == CardinalityOperator.Any) {
				return ComparisonResult.success();
			}
		}
		
		if (e1.hasNext() || e2.hasNext())
			return ComparisonResult.ofEmpty();
		
		return o == CardinalityOperator.All ? 
				ComparisonResult.success() :
				ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " does not equal " + formatEqualsComparisonResultError(m2));
	}
	
	private static <T, U> ComparisonResult areEqualDifferent(MapperC<T> m1, MapperS<U> m2, CardinalityOperator o) {
		List<T> multi1 = m1.getMulti();
		U b2 = m2.get();
		
		if (multi1.isEmpty())
			return ComparisonResult.ofEmpty();
		
		ListIterator<T> e1 = multi1.listIterator();
		
		while (e1.hasNext()) {
			T b1 = e1.next();
			
			if (b1 instanceof Number && b2 instanceof Number) {
				int compRes = CompareHelper.compare(b1, b2);
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
		    return ComparisonResult.success();

		while (e1.hasNext() && e2.hasNext()) {
			T b1 = e1.next();
			T b2 = e2.next();
			if (b1 instanceof Number && b2 instanceof Number) {
				int compRes = CompareHelper.compare(b1, b2);
				if (compRes != 0 && o == CardinalityOperator.Any) {
					return ComparisonResult.success();
				}
				if (compRes == 0 && o == CardinalityOperator.All) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " should not equal " + formatEqualsComparisonResultError(m2));
				}
			} else {
				boolean equals = b1 == null ? b2 == null : b1.equals(b2);
				if (!equals && o == CardinalityOperator.Any) {
					return ComparisonResult.success();
				}
				if (equals && o == CardinalityOperator.All) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " should not equal " + formatEqualsComparisonResultError(m2));
				}
			}
		}
		
		if (e1.hasNext() || e2.hasNext())
			return ComparisonResult.success();
		
		return o == CardinalityOperator.Any ? 
				ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " should not equal " + formatEqualsComparisonResultError(m2)) :
				ComparisonResult.success();

	}
	
	private static <T, U> ComparisonResult notEqualDifferent(MapperC<T> m1, MapperS<U> m2, CardinalityOperator o) {
		List<T> multi1 = m1.getMulti();
		U b2 = m2.get();

		if (multi1.isEmpty())
		    return ComparisonResult.success();

		ListIterator<T> e1 = multi1.listIterator();
		
		while (e1.hasNext()) {
			T b1 = e1.next();
			
			if (b1 instanceof Number && b2 instanceof Number) {
				int compRes = CompareHelper.compare(b1, b2);
				if (compRes != 0 && o == CardinalityOperator.Any) {
					return ComparisonResult.success();
				}
				if (compRes == 0 && o == CardinalityOperator.All) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " should not equal " + formatEqualsComparisonResultError(m2));
				}
			} else {
				@SuppressWarnings("unlikely-arg-type")
				boolean equals = b1 == null ? b2 == null : b1.equals(b2);
				if (!equals && o == CardinalityOperator.Any) {
					return ComparisonResult.success();
				}
				if (equals && o == CardinalityOperator.All) {
					return ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " should not equal " + formatEqualsComparisonResultError(m2));
				}
			}
				
		}

		return o == CardinalityOperator.Any ? 
				ComparisonResult.failure(formatEqualsComparisonResultError(m1) + " should not equal  " + formatEqualsComparisonResultError(m2)) :
				ComparisonResult.success();
	}
}
