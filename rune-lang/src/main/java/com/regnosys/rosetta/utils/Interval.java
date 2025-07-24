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

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

/**
 * A class representing an interval between two comparable objects.
 * 
 * The bounds are inclusive, an may be unbounded. The following forms are allowed:
 * - [min, max]
 * - ]-infinity, max]
 * - [min, +infinity[
 * - ]-infinity, +infinity[
 */
public abstract class Interval<T extends Comparable<? super T>> {
	private final Optional<T> min;
	private final Optional<T> max;
	
	public Interval(Optional<T> min, Optional<T> max) {
		if (min.isPresent() && max.isPresent()) {
			Validate.isTrue(min.get().compareTo(max.get()) <= 0, "The minimum (" + min.get() + ") must be less than the maximum (" + max.get() + ")");
		}
		this.min = min;
		this.max = max;
	}
	
	public Optional<T> getMin() {
		return this.min;
	}
	public Optional<T> getMax() {
		return this.max;
	}
	public boolean isUnbounded() {
		return min.isEmpty() && max.isEmpty();
	}
	public boolean isUnboundedLeft() {
		return min.isEmpty();
	}
	public boolean isUnboundedRight() {
		return max.isEmpty();
	}
	
	public boolean includes(T x) {
		if (min.map(b -> b.compareTo(x) > 0).orElse(false)) {
			return false;
		}
		if (max.map(b -> b.compareTo(x) < 0).orElse(false)) {
			return false;
		}
		return true;
	}
	public boolean includes(Interval<T> other) {
		if (min.map(b -> other.min.map(ob -> b.compareTo(ob) > 0).orElse(true)).orElse(false)) {
			return false;
		}
		if (max.map(b -> other.max.map(ob -> b.compareTo(ob) < 0).orElse(true)).orElse(false)) {
			return false;
		}
		return true;
	}
	public boolean strictlyIncludes(T x) {
		if (min.map(b -> b.compareTo(x) >= 0).orElse(false)) {
			return false;
		}
		if (max.map(b -> b.compareTo(x) <= 0).orElse(false)) {
			return false;
		}
		return true;
	}
	public boolean strictlyIncludes(Interval<T> other) {
		if (min.map(b -> other.min.map(ob -> b.compareTo(ob) >= 0).orElse(true)).orElse(false)) {
			return false;
		}
		if (max.map(b -> other.max.map(ob -> b.compareTo(ob) <= 0).orElse(true)).orElse(false)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		min.ifPresentOrElse(
				b -> builder.append("[").append(b),
				() -> builder.append("]-inf"));
		builder.append(", ");
		max.ifPresentOrElse(
				b -> builder.append(b).append("]"),
				() -> builder.append("+inf["));
		return builder.toString();
	}
	@Override
	public int hashCode() {
		return Objects.hash(min, max);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Interval<?> other = (Interval<?>) obj;
		return Objects.equals(max, other.max) && Objects.equals(min, other.min);
	}
}
