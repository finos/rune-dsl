package com.rosetta.model.lib.expressions;

import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperS;
import java.math.BigDecimal;
import java.math.MathContext;

public class BigDecimalExtensions {

	/// for Mappers
	/**
	 * Add a and b
	 */
	public static Mapper<BigDecimal> add(Mapper<BigDecimal> a, Mapper<BigDecimal> b) {
		return MapperS.of(a.get().add(b.get()));
	}
	
	/**
	 * Subtract b from a
	 */
	public static Mapper<BigDecimal> subtract(Mapper<BigDecimal> a, Mapper<BigDecimal> b) {
		return  MapperS.of(a.get().subtract(b.get()));
	}
	
	/**
	 * Multiply a and b
	 */
	public static Mapper<BigDecimal> multiply(Mapper<BigDecimal> a, Mapper<BigDecimal> b) {
		return  MapperS.of(a.get().multiply(b.get()));
	}
	
	/**
	 * Divide a by b 
	 */
	public static Mapper<BigDecimal> divide(Mapper<BigDecimal> a, Mapper<BigDecimal> b) {
		return MapperS.of(a.get().divide(b.get(), MathContext.DECIMAL128));
	}
	
	/**
	 * Is a close to b, with given error
	 */
	public static boolean closeTo(Mapper<BigDecimal> a, Mapper<BigDecimal> b, Mapper<BigDecimal> error) {
		return a.get().subtract(b.get()).abs().compareTo(error.get()) < 0;
	}
	
	/**
	 * 
	 * @see BigDecimal.valueOf(long);
	 */
	public static Mapper<BigDecimal> valueOf(Mapper<? extends Number> a) {
		Number number = a.get();
		if (number instanceof BigDecimal)
			return MapperS.of(BigDecimal.valueOf(((BigDecimal) number).doubleValue()));
		return MapperS.of(BigDecimal.valueOf(number.longValue()));
	}
	
	/**
	 * 
	 * @see BigDecimal.valueOf(long, int);
	 */
	public static Mapper<BigDecimal> valueOf(Mapper<Integer> a, int scale) {
		return  MapperS.of(BigDecimal.valueOf((long) a.get(),scale));
	}
	
}
