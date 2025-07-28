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

package com.regnosys.rosetta.types.builtin;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import com.regnosys.rosetta.interpreter.RosettaNumberValue;
import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.utils.BigDecimalInterval;
import com.regnosys.rosetta.utils.OptionalUtil;
import com.rosetta.model.lib.RosettaNumber;

public class RNumberType extends RBasicType {
	public static final String DIGITS_PARAM_NAME = "digits";
	public static final String FRACTIONAL_DIGITS_PARAM_NAME = "fractionalDigits";
	public static final String MIN_PARAM_NAME = "min";
	public static final String MAX_PARAM_NAME = "max";
	public static final String SCALE_PARAM_NAME = "scale";

	private final Optional<Integer> digits;
	private final Optional<Integer> fractionalDigits;
	private final BigDecimalInterval interval;
	private final Optional<BigDecimal> scale;

	private static LinkedHashMap<String, RosettaValue> createArgumentMap(Optional<Integer> digits,
			Optional<Integer> fractionalDigits, BigDecimalInterval interval, Optional<BigDecimal> scale) {
		LinkedHashMap<String, RosettaValue> arguments = new LinkedHashMap<>();
		arguments.put(
				DIGITS_PARAM_NAME, 
				digits.<RosettaValue>map(d -> RosettaNumberValue.of(RosettaNumber.valueOf(d)))
					.orElseGet(() -> RosettaValue.empty()));
		arguments.put(
				FRACTIONAL_DIGITS_PARAM_NAME, 
				fractionalDigits.<RosettaValue>map(d -> RosettaNumberValue.of(RosettaNumber.valueOf(d)))
					.orElseGet(() -> RosettaValue.empty()));
		arguments.put(
				MIN_PARAM_NAME, 
				interval.getMin().<RosettaValue>map(m -> RosettaNumberValue.of(RosettaNumber.valueOf(m)))
					.orElseGet(() -> RosettaValue.empty()));
		arguments.put(
				MAX_PARAM_NAME, 
				interval.getMax().<RosettaValue>map(m -> RosettaNumberValue.of(RosettaNumber.valueOf(m)))
					.orElseGet(() -> RosettaValue.empty()));
		arguments.put(
				SCALE_PARAM_NAME, 
				scale.<RosettaValue>map(s -> RosettaNumberValue.of(RosettaNumber.valueOf(s)))
					.orElseGet(() -> RosettaValue.empty()));
		return arguments;
	}

	public RNumberType(Optional<Integer> digits, Optional<Integer> fractionalDigits, BigDecimalInterval interval,
			Optional<BigDecimal> scale) {
		super("number", createArgumentMap(digits, fractionalDigits, interval, scale), true);
		if (digits.isPresent()) {
			Validate.isTrue(digits.get() > 0);
		}
		if (fractionalDigits.isPresent()) {
			Validate.isTrue(fractionalDigits.get() >= 0);
		}
		if (digits.isPresent() && fractionalDigits.isPresent()) {
			Validate.isTrue(fractionalDigits.get() < digits.get());
		}

		this.digits = digits;
		this.fractionalDigits = fractionalDigits;
		this.interval = interval;
		this.scale = scale;
	}

	public RNumberType(Optional<Integer> digits, Optional<Integer> fractionalDigits, Optional<BigDecimal> min,
			Optional<BigDecimal> max, Optional<BigDecimal> scale) {
		this(digits, fractionalDigits, new BigDecimalInterval(min, max), scale);
	}

	public static RNumberType from(Map<String, RosettaValue> values) {
		return new RNumberType(values.getOrDefault(DIGITS_PARAM_NAME, RosettaValue.empty()).getSingle(RosettaNumber.class).map(n -> n.intValue()),
				values.getOrDefault(FRACTIONAL_DIGITS_PARAM_NAME, RosettaValue.empty()).getSingle(RosettaNumber.class).map(n -> n.intValue()),
				values.getOrDefault(MIN_PARAM_NAME, RosettaValue.empty()).getSingle(RosettaNumber.class)
						.map(RosettaNumber::bigDecimalValue),
				values.getOrDefault(MAX_PARAM_NAME, RosettaValue.empty()).getSingle(RosettaNumber.class)
						.map(RosettaNumber::bigDecimalValue),
				values.getOrDefault(SCALE_PARAM_NAME, RosettaValue.empty()).getSingle(RosettaNumber.class)
						.map(RosettaNumber::bigDecimalValue));
	}

	public Optional<Integer> getDigits() {
		return digits;
	}

	public Optional<Integer> getFractionalDigits() {
		return fractionalDigits;
	}

	public BigDecimalInterval getInterval() {
		return interval;
	}

	public Optional<BigDecimal> getScale() {
		return scale;
	}

	public boolean isInteger() {
		return fractionalDigits.map(f -> f.equals(0)).orElse(false);
	}

	public RNumberType join(RNumberType other) {
		Optional<BigDecimal> joinedScale;
		if (scale.isPresent()) {
			if (other.scale.isPresent()) {
				if (scale.get().compareTo(other.scale.get()) == 0) {
					joinedScale = scale;
				} else {
					joinedScale = Optional.empty();
				}
			} else {
				joinedScale = scale;
			}
		} else {
			joinedScale = other.scale;
		}
		return new RNumberType(OptionalUtil.zipWith(digits, other.digits, Math::max),
				OptionalUtil.zipWith(fractionalDigits, other.fractionalDigits, Math::max),
				interval.minimalCover(other.interval), joinedScale);
	}
}
