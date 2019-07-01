package com.rosetta.model.lib.validation;

import java.math.BigDecimal;

public class CompareHelper {

	@SuppressWarnings("unchecked")
	static <T extends Comparable<? super T>, X extends Comparable<? super X>> int compare(T o1, X o2) {
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
	
	private static BigDecimal toBigD(Number n) {
		if (n instanceof BigDecimal) return (BigDecimal)n;
		if (n instanceof Long) return new BigDecimal(n.longValue());
		if (n instanceof Integer) return new BigDecimal(n.intValue());
		throw new IllegalArgumentException("can only convert integer and long to bigD");
	}
}