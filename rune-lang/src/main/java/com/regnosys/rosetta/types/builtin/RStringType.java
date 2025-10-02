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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.regnosys.rosetta.interpreter.RosettaNumberValue;
import com.regnosys.rosetta.interpreter.RosettaStringValue;
import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.utils.PositiveIntegerInterval;
import com.rosetta.model.lib.RosettaNumber;

public class RStringType extends RBasicType {
	public static final String MIN_LENGTH_PARAM_NAME = "minLength";
	public static final String MAX_LENGTH_PARAM_NAME = "maxLength";
	public static final String PATTERN_PARAM_NAME = "pattern";

	private final PositiveIntegerInterval interval;
	private final Optional<Pattern> pattern;

	private static LinkedHashMap<String, RosettaValue> createArgumentMap(PositiveIntegerInterval interval,
			Optional<Pattern> pattern) {
		LinkedHashMap<String, RosettaValue> arguments = new LinkedHashMap<>();
		int minBound = interval.getMinBound();
		arguments.put(MIN_LENGTH_PARAM_NAME, minBound == 0 ? RosettaValue.empty() : RosettaNumberValue.of(RosettaNumber.valueOf(minBound)));
		arguments.put(MAX_LENGTH_PARAM_NAME, interval.getMax().<RosettaValue>map(m -> RosettaNumberValue.of(RosettaNumber.valueOf(m)))
				.orElseGet(() -> RosettaValue.empty()));
		arguments.put(PATTERN_PARAM_NAME,
				pattern.<RosettaValue>map(p -> RosettaStringValue.of(p.toString())).orElseGet(() -> RosettaValue.empty()));
		return arguments;
	}

	public RStringType(PositiveIntegerInterval interval, Optional<Pattern> pattern) {
		super("string", createArgumentMap(interval, pattern), true);
		this.interval = interval;
		this.pattern = pattern;
	}

	public RStringType(Optional<Integer> minLength, Optional<Integer> maxLength, Optional<Pattern> pattern) {
		this(new PositiveIntegerInterval(minLength.orElse(0), maxLength), pattern);
	}

	public static RStringType from(Map<String, RosettaValue> values) {
		return new RStringType(values.getOrDefault(MIN_LENGTH_PARAM_NAME, RosettaValue.empty()).getSingle(RosettaNumber.class).map(d -> d.intValue()),
				values.getOrDefault(MAX_LENGTH_PARAM_NAME, RosettaValue.empty()).getSingle(RosettaNumber.class).map(d -> d.intValue()),
				values.getOrDefault(PATTERN_PARAM_NAME, RosettaValue.empty()).getSingle(String.class).map(s -> Pattern.compile(s)));
	}

	public PositiveIntegerInterval getInterval() {
		return interval;
	}

	public Optional<Pattern> getPattern() {
		return pattern;
	}

	public RStringType join(RStringType other) {
		Optional<Pattern> joinedPattern;
		if (pattern.isPresent()) {
			if (other.pattern.isPresent()) {
				if (pattern.get().equals(other.pattern.get())) {
					joinedPattern = pattern;
				} else {
					joinedPattern = Optional.empty();
				}
			} else {
				joinedPattern = pattern;
			}
		} else {
			joinedPattern = other.pattern;
		}
		return new RStringType(interval.minimalCover(other.interval), joinedPattern);
	}
}
