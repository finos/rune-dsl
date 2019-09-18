package com.rosetta.model.lib.math;

import java.math.BigDecimal;
import java.math.MathContext;

import com.rosetta.model.lib.functions.Mapper;
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
	/**
	 * 
	 * @see BigDecimal.valueOf(double);
	 */
	public static BigDecimal valueOf(double a) {
		return BigDecimal.valueOf(a);
	}
	/**
	 * 
	 * @see BigDecimal.valueOf(long);
	 */
	public static BigDecimal valueOf(long a) {
		return BigDecimal.valueOf(a);
	}
	/**
	 * 
	 * @see BigDecimal.valueOf(long, int);
	 */
	public static BigDecimal valueOf(long a, int scale) {
		return BigDecimal.valueOf(a,scale);
	}
	
	/// for Mappers
	/**
	 * Add a and b
	 */
	public static Mapper<BigDecimal> add(Mapper<BigDecimal> a, Mapper<BigDecimal> b) {
		return MapperS.of(add(a.get(),b.get()));
	}
	
	/**
	 * Subtract b from a
	 */
	public static Mapper<BigDecimal> subtract(Mapper<BigDecimal> a, Mapper<BigDecimal> b) {
		return  MapperS.of(subtract(a.get(),b.get()));
	}
	
	/**
	 * Multiply a and b
	 */
	public static Mapper<BigDecimal> multiply(Mapper<BigDecimal> a, Mapper<BigDecimal> b) {
		return  MapperS.of(multiply(a.get(),b.get()));
	}
	
	/**
	 * Divide a by b 
	 */
	public static Mapper<BigDecimal> divide(Mapper<BigDecimal> a, Mapper<BigDecimal> b) {
		return MapperS.of(divide(a.get(),b.get()));
	}
	
	/**
	 * Is a close to b, with given error
	 */
	public static boolean closeTo(Mapper<BigDecimal> a, Mapper<BigDecimal> b, Mapper<BigDecimal> error) {
		return closeTo(a.get(), b.get(), error.get());
	}
	
	/**
	 * 
	 * @see BigDecimal.valueOf(long);
	 */
	public static Mapper<BigDecimal> valueOf(Mapper<Number> a) {
		Number number = a.get();
		if (number instanceof BigDecimal)
			return MapperS.of(valueOf(((BigDecimal) number).doubleValue()));
		return MapperS.of(valueOf(number.longValue()));
	}
	
	/**
	 * 
	 * @see BigDecimal.valueOf(long, int);
	 */
	public static Mapper<BigDecimal> valueOf(Mapper<Integer> a, int scale) {
		return  MapperS.of(valueOf(a.get(), scale));
	}
	
}
