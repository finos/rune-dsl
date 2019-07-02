package com.rosetta.model.lib.math;

import java.math.BigDecimal;
import java.math.MathContext;

public class BigDecimalExtensions {

	/**
	 * Add a and b
	 */
	public static BigDecimal add(BigDecimal a, BigDecimal b) {
		return a.add(b);
	}
	
	/**
	 * Subtract b from a
	 */
	public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
		return a.subtract(b);
	}

	/**
	 * Multiply a and b
	 */
	public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
		return a.multiply(b);
	}

	/**
	 * Divide a by b 
	 */
	public static BigDecimal divide(BigDecimal a, BigDecimal b) {
		return a.divide(b, MathContext.DECIMAL128);
	}

	/**
	 * Is a close to b, with given error
	 */
	public static boolean closeTo(BigDecimal a, BigDecimal b, BigDecimal error) {
		return a.subtract(b).abs().compareTo(error) < 0;
	}
}
