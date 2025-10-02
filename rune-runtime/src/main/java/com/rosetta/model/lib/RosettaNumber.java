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

package com.rosetta.model.lib;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Objects;

/**
 * An implementation of the IEEE754 decimal128 standard. Under the hood, this class uses
 * BigDecimal. Also see {@link MathContext#DECIMAL128}.
 * (Actually, *almost* an implementation of the decimal128 standard. 
 *  See section "Relation to IEEE 754 Decimal Arithmetic" in the
 *  BigDecimal documentation for edge cases:
 *  https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/math/BigDecimal.html
 * )
 * 
 * Note that this class circumvents problems caused by a
 * per-instance precision, like in BigDecimal.
 * E.g., for equality, see https://stackoverflow.com/q/6787142/3083982
 */
public class RosettaNumber extends Number implements Comparable<RosettaNumber> {
	public static final MathContext DECIMAL_PRECISION = MathContext.DECIMAL128;
	public static final RosettaNumber ZERO = new RosettaNumber(BigDecimal.ZERO);
	public static final RosettaNumber ONE = new RosettaNumber(BigDecimal.ONE);
	
	private static final long serialVersionUID = 1L;

	private final BigDecimal value;
	
	private RosettaNumber(BigDecimal value) {
		// Strip trailing zeros to make equality checks and hash codes work.
		this.value = value.stripTrailingZeros();
	}
	public RosettaNumber(String repr) {
		this(new BigDecimal(repr, DECIMAL_PRECISION));
	}
	
	public static RosettaNumber valueOf(double value) {
		return new RosettaNumber(Double.toString(value));
	}
	public static RosettaNumber valueOf(long value) {
		return new RosettaNumber(new BigDecimal(value, DECIMAL_PRECISION));
	}
	public static RosettaNumber valueOf(BigInteger value) {
		return new RosettaNumber(new BigDecimal(value, DECIMAL_PRECISION));
	}
	public static RosettaNumber valueOf(BigDecimal value) {
		return new RosettaNumber(value.toString());
	}
	
	public RosettaNumber add(RosettaNumber other) {
		return new RosettaNumber(this.value.add(other.value));
	}
	public RosettaNumber subtract(RosettaNumber other) {
		return new RosettaNumber(this.value.subtract(other.value));
	}
	public RosettaNumber multiply(RosettaNumber other) {
		return new RosettaNumber(this.value.multiply(other.value, DECIMAL_PRECISION));
	}
	public RosettaNumber divide(RosettaNumber other) {
		return new RosettaNumber(this.value.divide(other.value, DECIMAL_PRECISION));
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        final RosettaNumber other = (RosettaNumber)object;
        return Objects.equals(value, other.value);
	}
	
	@Override
	public int hashCode() {
		return this.value.hashCode();
	}
	
	@Override
	public String toString() {
		return this.value.toPlainString();
	}
	
	@Override
	public int intValue() {
		return value.intValue();
	}

	@Override
	public long longValue() {
		return value.longValue();
	}

	@Override
	public float floatValue() {
		return value.floatValue();
	}

	@Override
	public double doubleValue() {
		return value.doubleValue();
	}
	
	public BigInteger bigIntegerValue() {
		return value.toBigInteger();
	}
	
	public BigDecimal bigDecimalValue() {
		return value;
	}

	@Override
	public int compareTo(RosettaNumber o) {
		return value.compareTo(o.value);
	}
}
