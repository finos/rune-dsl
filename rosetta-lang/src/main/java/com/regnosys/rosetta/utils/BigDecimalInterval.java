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

package com.regnosys.rosetta.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;
import java.util.Optional;

public class BigDecimalInterval extends Interval<BigDecimal> {

	public BigDecimalInterval(Optional<BigDecimal> min, Optional<BigDecimal> max) {
		super(min, max);
	}

	public static BigDecimalInterval bounded(BigDecimal min, BigDecimal max) {
		return new BigDecimalInterval(Optional.of(min), Optional.of(max));
	}
	public static BigDecimalInterval boundedLeft(BigDecimal min) {
		return new BigDecimalInterval(Optional.of(min), Optional.empty());
	}
	public static BigDecimalInterval boundedRight(BigDecimal max) {
		return new BigDecimalInterval(Optional.empty(), Optional.of(max));
	}
	public static BigDecimalInterval unbounded() {
		return new BigDecimalInterval(Optional.empty(), Optional.empty());
	}
	
	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null) return false;
		if (getClass() != object.getClass()) return false;
		
		BigDecimalInterval other = (BigDecimalInterval)object;
		return boundEquals(getMin(), other.getMin())
				&& boundEquals(getMax(), other.getMax());
	}
	private boolean boundEquals(Optional<BigDecimal> a, Optional<BigDecimal> b) {
		// When checking equality between `BigDecimal`s, we should ignore precision,
		// so we check equality using `x.compareTo(y) == 0` instead.
		return Objects.equals(a, b) || OptionalUtil.zipWith(a, b, (x, y) -> x.compareTo(y) == 0).orElse(false);
	}

	public BigDecimalInterval minimalCover(BigDecimalInterval other) {
		return new BigDecimalInterval(
				OptionalUtil.zipWith(getMin(), other.getMin(), BigDecimal::min),
				OptionalUtil.zipWith(getMax(), other.getMax(), BigDecimal::max)
			);
	}
	public BigDecimalInterval add(BigDecimalInterval other) {
		return new BigDecimalInterval(
				OptionalUtil.zipWith(getMin(), other.getMin(), BigDecimal::add),
				OptionalUtil.zipWith(getMax(), other.getMax(), BigDecimal::add)
			);
	}
	public BigDecimalInterval subtract(BigDecimalInterval other) {
		return new BigDecimalInterval(
				OptionalUtil.zipWith(getMin(), other.getMin(), BigDecimal::subtract),
				OptionalUtil.zipWith(getMax(), other.getMax(), BigDecimal::subtract)
			);
	}
	public BigDecimalInterval multiply(BigDecimalInterval other) {
		BigDecimalOrInfinite mina = withSignedInfinite(getMin(), false);
		BigDecimalOrInfinite maxa = withSignedInfinite(getMax(), true);
		BigDecimalOrInfinite minb = withSignedInfinite(other.getMin(), false);
		BigDecimalOrInfinite maxb = withSignedInfinite(other.getMax(), true);
		
		BigDecimalOrInfinite mult1 = mina.multiply(minb);
		BigDecimalOrInfinite mult2 = mina.multiply(maxb);
		BigDecimalOrInfinite mult3 = maxa.multiply(minb);
		BigDecimalOrInfinite mult4 = maxa.multiply(maxb);
		
		BigDecimalOrInfinite min = mult1.min(mult2).min(mult3).min(mult4);
		BigDecimalOrInfinite max = mult1.max(mult2).max(mult3).max(mult4);
		
		return new BigDecimalInterval(
				min.finiteValue,
				max.finiteValue
			);
	}
	public BigDecimalInterval divide(BigDecimalInterval other) {
		return multiply(other.invert());
	}
	public BigDecimalInterval invert() {
		if (this.strictlyIncludes(BigDecimal.ZERO)) {
			return unbounded();
		}
		BigDecimalOrInfinite min = invert(getMax(), false);
		BigDecimalOrInfinite max = invert(getMin(), true);
		return new BigDecimalInterval(min.finiteValue, max.finiteValue);
	}
	
	private static BigDecimalOrInfinite invert(Optional<BigDecimal> x, boolean defaultSign) {
		return x.map(v -> {
			if (v.compareTo(BigDecimal.ZERO) == 0) {
				return BigDecimalOrInfinite.infinite(defaultSign);
			} else {
				return BigDecimalOrInfinite.of(BigDecimal.ONE.divide(v, MathContext.DECIMAL128));
			}
		}).orElse(BigDecimalOrInfinite.of(BigDecimal.ZERO));
	}
	private static BigDecimalOrInfinite withSignedInfinite(Optional<BigDecimal> finiteValue, boolean defaultSign) {
		return finiteValue.map(v -> BigDecimalOrInfinite.of(v)).orElseGet(() -> BigDecimalOrInfinite.infinite(defaultSign));
	}
	private static class BigDecimalOrInfinite {
		private final Optional<BigDecimal> finiteValue;
		private final boolean sign;
		
		private BigDecimalOrInfinite(Optional<BigDecimal> finiteValue, boolean sign) {
			this.finiteValue = finiteValue;
			this.sign = sign;
		}
		
		public static BigDecimalOrInfinite of(BigDecimal finiteValue) {
			return new BigDecimalOrInfinite(Optional.of(finiteValue), finiteValue.compareTo(BigDecimal.ZERO) >= 0);
		}
		public static BigDecimalOrInfinite infinite(boolean sign) {
			return new BigDecimalOrInfinite(Optional.empty(), sign);
		}
		
		public boolean isFinite() {
			return finiteValue.isPresent();
		}
		
		public BigDecimalOrInfinite multiply(BigDecimalOrInfinite other) {
			Optional<BigDecimal> resultFiniteValue = OptionalUtil.zipWith(finiteValue, other.finiteValue, BigDecimal::multiply)
					.or(() -> {
						if (isZero()) {
							return finiteValue;
						}
						if (other.isZero()) {
							return other.finiteValue;
						}
						return Optional.empty();
					});
			boolean resultSign = sign == other.sign;
			return new BigDecimalOrInfinite(resultFiniteValue, resultSign);
		}
		public boolean isZero() {
			return finiteValue.map(v -> v.compareTo(BigDecimal.ZERO) == 0).orElse(false);
		}
		public BigDecimalOrInfinite min(BigDecimalOrInfinite other) {
			Optional<BigDecimal> resultFiniteValue = OptionalUtil.zipWith(finiteValue, other.finiteValue, BigDecimal::min)
					.or(() -> {
						if (isFinite()) {
							if (other.sign) {
								return finiteValue;
							} else {
								return Optional.empty();
							}
						}
						if (other.isFinite()) {
							if (sign) {
								return other.finiteValue;
							} else {
								return Optional.empty();
							}
						}
						return Optional.empty();
					});
			boolean resultSign = sign && other.sign;
			return new BigDecimalOrInfinite(resultFiniteValue, resultSign);
		}
		public BigDecimalOrInfinite max(BigDecimalOrInfinite other) {
			Optional<BigDecimal> resultFiniteValue = OptionalUtil.zipWith(finiteValue, other.finiteValue, BigDecimal::max)
					.or(() -> {
						if (isFinite()) {
							if (other.sign) {
								return Optional.empty();
							} else {
								return finiteValue;
							}
						}
						if (other.isFinite()) {
							if (sign) {
								return Optional.empty();
							} else {
								return other.finiteValue;
							}
						}
						return Optional.empty();
					});
			boolean resultSign = sign || other.sign;
			return new BigDecimalOrInfinite(resultFiniteValue, resultSign);
		}
	}
}
