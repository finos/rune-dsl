package com.rosetta.model.lib.math;

import java.math.BigDecimal;
import java.math.MathContext;

import com.rosetta.model.lib.functions.MapperBuilder;
import com.rosetta.model.lib.functions.MapperS;

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
	
	/// for builders
	/**
	 * Add a and b
	 */
	public static MapperBuilder<BigDecimal> add(MapperBuilder<BigDecimal> a, MapperBuilder<BigDecimal> b) {
		return MapperS.of(add(a.get(),b.get()));
	}
	
	/**
	 * Subtract b from a
	 */
	public static MapperBuilder<BigDecimal> subtract(MapperBuilder<BigDecimal> a, MapperBuilder<BigDecimal> b) {
		return  MapperS.of(subtract(a.get(),b.get()));
	}
	
	/**
	 * Multiply a and b
	 */
	public static MapperBuilder<BigDecimal> multiply(MapperBuilder<BigDecimal> a, MapperBuilder<BigDecimal> b) {
		return  MapperS.of(multiply(a.get(),b.get()));
	}
	
	/**
	 * Divide a by b 
	 */
	public static MapperBuilder<BigDecimal> divide(MapperBuilder<BigDecimal> a, MapperBuilder<BigDecimal> b) {
		return MapperS.of(divide(a.get(),b.get()));
	}
	
	/**
	 * Is a close to b, with given error
	 */
	public static boolean closeTo(MapperBuilder<BigDecimal> a, MapperBuilder<BigDecimal> b, MapperBuilder<BigDecimal> error) {
		return closeTo(a.get(), b.get(), error.get());
	}
}
