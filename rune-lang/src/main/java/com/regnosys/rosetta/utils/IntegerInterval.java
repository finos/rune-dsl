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

import java.util.Optional;

public class IntegerInterval extends Interval<Integer> {

	public IntegerInterval(Optional<Integer> min, Optional<Integer> max) {
		super(min, max);
	}

	public static IntegerInterval bounded(int min, int max) {
		return new IntegerInterval(Optional.of(min), Optional.of(max));
	}
	public static IntegerInterval boundedLeft(int min) {
		return new IntegerInterval(Optional.of(min), Optional.empty());
	}
	public static IntegerInterval boundedRight(int max) {
		return new IntegerInterval(Optional.empty(), Optional.of(max));
	}
	public static IntegerInterval unbounded() {
		return new IntegerInterval(Optional.empty(), Optional.empty());
	}

	public IntegerInterval minimalCover(IntegerInterval other) {
		return new IntegerInterval(
				OptionalUtil.zipWith(getMin(), other.getMin(), Math::min),
				OptionalUtil.zipWith(getMax(), other.getMax(), Math::max)
			);
	}
	public IntegerInterval add(IntegerInterval other) {
		return new IntegerInterval(
				OptionalUtil.zipWith(getMin(), other.getMin(), (a, b) -> a + b),
				OptionalUtil.zipWith(getMax(), other.getMax(), (a, b) -> a + b)
			);
	}
	public IntegerInterval subtract(IntegerInterval other) {
		return new IntegerInterval(
				OptionalUtil.zipWith(getMin(), other.getMin(), (a, b) -> a - b),
				OptionalUtil.zipWith(getMax(), other.getMax(), (a, b) -> a - b)
			);
	}
	public IntegerInterval multiply(IntegerInterval other) {
		IntegerOrInfinite mina = withSignedInfinite(getMin(), false);
		IntegerOrInfinite maxa = withSignedInfinite(getMax(), true);
		IntegerOrInfinite minb = withSignedInfinite(other.getMin(), false);
		IntegerOrInfinite maxb = withSignedInfinite(other.getMax(), true);
		
		IntegerOrInfinite mult1 = mina.multiply(minb);
		IntegerOrInfinite mult2 = mina.multiply(maxb);
		IntegerOrInfinite mult3 = maxa.multiply(minb);
		IntegerOrInfinite mult4 = maxa.multiply(maxb);
		
		IntegerOrInfinite min = mult1.min(mult2).min(mult3).min(mult4);
		IntegerOrInfinite max = mult1.max(mult2).max(mult3).max(mult4);
		
		return new IntegerInterval(
				min.finiteValue,
				max.finiteValue
			);
	}

	private static IntegerOrInfinite withSignedInfinite(Optional<Integer> finiteValue, boolean defaultSign) {
		return finiteValue.map(v -> IntegerOrInfinite.of(v)).orElseGet(() -> IntegerOrInfinite.infinite(defaultSign));
	}
	private static class IntegerOrInfinite {
		private final Optional<Integer> finiteValue;
		private final boolean sign;
		
		private IntegerOrInfinite(Optional<Integer> finiteValue, boolean sign) {
			this.finiteValue = finiteValue;
			this.sign = sign;
		}
		
		public static IntegerOrInfinite of(int finiteValue) {
			return new IntegerOrInfinite(Optional.of(finiteValue), finiteValue >= 0);
		}
		public static IntegerOrInfinite infinite(boolean sign) {
			return new IntegerOrInfinite(Optional.empty(), sign);
		}
		
		public boolean isFinite() {
			return finiteValue.isPresent();
		}
		
		public IntegerOrInfinite multiply(IntegerOrInfinite other) {
			Optional<Integer> resultFiniteValue = OptionalUtil.zipWith(finiteValue, other.finiteValue, (a, b) -> a * b)
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
			return new IntegerOrInfinite(resultFiniteValue, resultSign);
		}
		public boolean isZero() {
			return finiteValue.map(v -> v == 0).orElse(false);
		}
		public IntegerOrInfinite min(IntegerOrInfinite other) {
			Optional<Integer> resultFiniteValue = OptionalUtil.zipWith(finiteValue, other.finiteValue, Math::min)
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
			return new IntegerOrInfinite(resultFiniteValue, resultSign);
		}
		public IntegerOrInfinite max(IntegerOrInfinite other) {
			Optional<Integer> resultFiniteValue = OptionalUtil.zipWith(finiteValue, other.finiteValue, Math::max)
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
			return new IntegerOrInfinite(resultFiniteValue, resultSign);
		}
	}
}
