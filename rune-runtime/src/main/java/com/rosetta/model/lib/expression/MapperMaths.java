package com.rosetta.model.lib.expression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperS;

public class MapperMaths {

	@SuppressWarnings("unchecked")
	public static <R, A, B> MapperS<R> add(Mapper<? extends A> in1, Mapper<? extends B> in2) {
		if (in1.resultCount() == 1 && in2.resultCount() == 1) {
			A arg1 = in1.get();
			B arg2 = in2.get();
			return MapperS.of((R) operatorPlus(arg1, arg2));
		}
		return MapperS.ofNull();
	}

	@SuppressWarnings("unchecked")
	public static <R, A, B> MapperS<R> subtract(Mapper<? extends A> in1, Mapper<? extends B> in2) {
		if (in1.resultCount() == 1 && in2.resultCount() == 1) {
			A arg1 = in1.get();
			B arg2 = in2.get();
			return MapperS.of((R) operatorMinus(arg1, arg2));
		}
		return MapperS.ofNull();
	}

	@SuppressWarnings("unchecked")
	public static <R, A, B> MapperS<R> multiply(Mapper<? extends A> in1, Mapper<? extends B> in2) {
		if (in1.resultCount() == 1 && in2.resultCount() == 1) {
			A arg1 = in1.get();
			B arg2 = in2.get();
			return MapperS.of((R) operatorMultiply(arg1, arg2));
		}
		return MapperS.ofNull();
	}

	@SuppressWarnings("unchecked")
	public static <R, A, B> MapperS<R> divide(Mapper<? extends A> in1, Mapper<? extends B> in2) {
		if (in1.resultCount() == 1 && in2.resultCount() == 1) {
			A arg1 = in1.get();
			B arg2 = in2.get();
			return MapperS.of((R) operatorDivide(arg1, arg2));
		}
		return MapperS.ofNull();
	}

	// Plus
	private static Object operatorPlus(Object a, Object b) {
		if (a instanceof LocalDate && b instanceof LocalDate) {
			throw new RuntimeException("Cant add two dates together");
		} else if (a instanceof LocalDate && b instanceof LocalTime) {
			return LocalDateTime.of((LocalDate) a, (LocalTime) b);
		} else if (a instanceof Integer && b instanceof Integer) {
			return ((Integer) a).intValue() + ((Integer) b).intValue();
		} else if (a instanceof Long && b instanceof Long) {
			return ((Long) a).longValue() + ((Long) b).longValue();
		} else if (a instanceof BigInteger && b instanceof BigInteger) {
			return ((BigInteger) a).add((BigInteger) b);
		} else if (a instanceof Number && b instanceof Number) {
			return toBigD((Number) a).add(toBigD((Number) b));
		} else if (a instanceof String && b instanceof String) {
			return (String) a + (String) b;
		} else if (a != null && b != null) {
			throw new RuntimeException("Cant add two random (" + a.getClass().getSimpleName() + ", "
					+ b.getClass().getSimpleName() + ") together");
		} else {
			throw new IllegalArgumentException("Unhandled parameter types: " + Arrays.asList(a, b));
		}
	}

	// Minus
	private static Object operatorMinus(Object a, Object b) {
		if (a instanceof LocalDate && b instanceof LocalDate) {
			return Long.valueOf(ChronoUnit.DAYS.between((LocalDate) b, (LocalDate) a)).intValue();
		} else if (a instanceof LocalDate && b instanceof LocalTime) {
			throw new RuntimeException("Cant subtract time from date");
		} else if (a instanceof Integer && b instanceof Integer) {
			return ((Integer) a).intValue() - ((Integer) b).intValue();
		} else if (a instanceof Long && b instanceof Long) {
			return ((Long) a).longValue() - ((Long) b).longValue();
		} else if (a instanceof BigInteger && b instanceof BigInteger) {
			return ((BigInteger) a).subtract((BigInteger) b);
		} else if (a instanceof Number && b instanceof Number) {
			return toBigD((Number) a).subtract(toBigD((Number) b));
		} else if (a instanceof String && b instanceof String) {
			throw new RuntimeException("Cant subtract two strings together");
		} else if (a != null && b != null) {
			throw new RuntimeException("Cant subtract two random (" + a.getClass().getSimpleName() + ", "
					+ b.getClass().getSimpleName() + ") together");
		} else {
			throw new IllegalArgumentException("Unhandled parameter types: " + Arrays.asList(a, b));
		}
	}

	// Multiply
	private static Object operatorMultiply(Object a, Object b) {
		if (a instanceof LocalDate && b instanceof LocalDate) {
			throw new RuntimeException("Cant multiply date and date");
		} else if (a instanceof LocalDate && b instanceof LocalTime) {
			throw new RuntimeException("Cant multiply time and date");
		} else if (a instanceof Integer && b instanceof Integer) {
			return ((Integer) a).intValue() * ((Integer) b).intValue();
		} else if (a instanceof Long && b instanceof Long) {
			return ((Long) a).longValue() * ((Long) b).longValue();
		} else if (a instanceof BigInteger && b instanceof BigInteger) {
			return ((BigInteger) a).multiply((BigInteger) b);
		} else if (a instanceof Number && b instanceof Number) {
			return toBigD((Number) a).multiply(toBigD((Number) b));
		} else if (a instanceof String && b instanceof String) {
			throw new RuntimeException("Cant multiply two strings together");
		} else if (a != null && b != null) {
			throw new RuntimeException("Cant multiply two random (" + a.getClass().getSimpleName() + ", "
					+ b.getClass().getSimpleName() + ") together");
		} else {
			throw new IllegalArgumentException("Unhandled parameter types: " + Arrays.asList(a, b));
		}
	}

	// Divide
	private static Object operatorDivide(Object a, Object b) {
		if (a instanceof LocalDate && b instanceof LocalDate) {
			throw new RuntimeException("Cant divide date and date");
		} else if (a instanceof LocalDate && b instanceof LocalTime) {
			throw new RuntimeException("Cant divide time and date");
		} else if (a instanceof Number && b instanceof Number) {
			return toBigD((Number) a).divide(toBigD((Number) b), MathContext.DECIMAL128);
		} else if (a instanceof String && b instanceof String) {
			throw new RuntimeException("Cant divide two strings");
		} else if (a != null && b != null) {
			throw new RuntimeException("Cant divide two random (" + a.getClass().getSimpleName() + ", "
					+ b.getClass().getSimpleName() + ")");
		} else {
			throw new IllegalArgumentException("Unhandled parameter types: " + Arrays.asList(a, b));
		}
	}

	public static BigDecimal toBigD(Number n) {
		if (n instanceof BigDecimal) {
			return (BigDecimal) n;
		} else if (n instanceof BigInteger) {
			return new BigDecimal((BigInteger) n);
		} else if (n instanceof Long) {
			return new BigDecimal(((Long) n).longValue());
		} else if (n instanceof Integer) {
			return new BigDecimal(((Integer) n).intValue());
		}
		return null;
	}
}
