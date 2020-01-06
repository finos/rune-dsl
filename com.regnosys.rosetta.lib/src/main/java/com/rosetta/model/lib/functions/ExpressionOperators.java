package com.rosetta.model.lib.functions;

import java.math.BigDecimal;
import java.util.Collection;

public class ExpressionOperators {

	public static boolean exists(final Object o) {
		return (o != null);
	}

	public static boolean notExists(final Object o) {
		return (o == null);
	}

	public static BigDecimal count(final Mapper<?> container) {
		if (container == null)
			return BigDecimal.ZERO;
		return BigDecimal.valueOf(container.getMulti().size());
	}

	public static BigDecimal count(final Collection<?> container) {
		if (container == null)
			return BigDecimal.ZERO;
		return BigDecimal.valueOf(container.size());
	}

	public static boolean contains(final Mapper<?> container, final Object ele) {
		return container.getMulti().contains(ele);
	}

	public static boolean contains(final Collection<?> container, final Object ele) {
		return container != null && container.contains(ele);
	}

	public static boolean contains(final Collection<?> container, final Collection<?> ele) {
		return container != null && container.containsAll(ele);
	}
}
