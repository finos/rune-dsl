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

import org.apache.commons.lang3.Validate;

public class PositiveIntegerInterval extends IntegerInterval {
	private final int min;
	
	public PositiveIntegerInterval(int min, Optional<Integer> max) {
		super(Optional.of(min), max);
		Validate.isTrue(min >= 0);
		
		this.min = min;
	}
	
	public static PositiveIntegerInterval bounded(int min, int max) {
		return new PositiveIntegerInterval(min, Optional.of(max));
	}
	public static PositiveIntegerInterval boundedLeft(int min) {
		return new PositiveIntegerInterval(min, Optional.empty());
	}

	public int getMinBound() {
		return this.min;
	}
	
	public PositiveIntegerInterval minimalCover(PositiveIntegerInterval other) {
		return new PositiveIntegerInterval(
				Math.min(getMinBound(), other.getMinBound()),
				OptionalUtil.zipWith(getMax(), other.getMax(), Math::max)
			);
	}
	public PositiveIntegerInterval add(PositiveIntegerInterval other) {
		return new PositiveIntegerInterval(
				getMinBound() + other.getMinBound(),
				OptionalUtil.zipWith(getMax(), other.getMax(), (a, b) -> a + b)
			);
	}
	public PositiveIntegerInterval multiply(PositiveIntegerInterval other) {
		return new PositiveIntegerInterval(
				getMinBound() * other.getMinBound(),
				OptionalUtil.zipWith(getMax(), other.getMax(), (a, b) -> a * b)
			);
	}
}
