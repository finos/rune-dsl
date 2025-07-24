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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;

public class CompareHelper {

	@SuppressWarnings("unchecked")
	static int compare(Object o1, Object o2) {
		if (o1 instanceof ZonedDateTime && o2 instanceof ZonedDateTime) {
			return ((ZonedDateTime)o1).toInstant().compareTo(((ZonedDateTime)o2).toInstant());
		}
		if (o1.getClass() == o2.getClass() && o1 instanceof Comparable) {
			return ((Comparable<Object>)o1).compareTo((Comparable<Object>)o2);
		}
		if (!(o1 instanceof Number && o2 instanceof Number)) {
			throw new IllegalArgumentException("I only know how to compare identical comparable types and numbers not " + 
						o1.getClass().getSimpleName() + " and " + o2.getClass().getSimpleName());
		}
		BigDecimal b1 = toBigD((Number)o1);
		BigDecimal b2 = toBigD((Number)o2);
		return b1.compareTo(b2);
	}
	
	private static BigDecimal toBigD(Number n) {
		if (n instanceof BigDecimal) return setScale((BigDecimal)n);
		if (n instanceof Long) return setScale(new BigDecimal(n.longValue()));
		if (n instanceof Integer) return setScale(new BigDecimal(n.intValue()));
		throw new IllegalArgumentException("can only convert integer and long to bigD");
	}
	
	/*
	 * Set consistent scale so comparisons work. 
	 */
	private static BigDecimal setScale(BigDecimal d) {
		return d.setScale(9, RoundingMode.HALF_UP);
	}
}