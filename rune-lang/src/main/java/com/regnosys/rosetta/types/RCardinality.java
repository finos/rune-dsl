package com.regnosys.rosetta.types;

import java.util.Objects;
import java.util.Optional;

import com.regnosys.rosetta.utils.PositiveIntegerInterval;

public class RCardinality {
	/**
	 * (1..1)
	 */
	public static RCardinality SINGLE = bounded(1, 1);
	/**
	 * (0..1)
	 */
	public static RCardinality OPTIONAL = bounded(0, 1);
	/**
	 * (0..*)
	 */
	public static RCardinality UNBOUNDED = unbounded(0);
	
	private final PositiveIntegerInterval interval;
	
	private RCardinality(PositiveIntegerInterval interval) {
		this.interval = interval;
	}
	public static RCardinality bounded(int min, int max) {
		if (max < min) {
			// Invalid cardinality
			return new RCardinality(null) {
				@Override
				public int getMin() {
					return min;
				}
				@Override
				public boolean isMulti() {
					return max > 1;
				}
				@Override
				public boolean isOptional() {
					return min == 0;
				}
				
				@Override
				public boolean includes(int x) {
					return false;
				}
				@Override
				public boolean includes(RCardinality other) {
					return false;
				}
				
				@Override
				public String toString() {
					StringBuilder builder = new StringBuilder();
					builder.append('(');
					builder.append(min);
					builder.append("..");
					builder.append(max);
					builder.append(')');
					return builder.toString();
				}
			};
		}
		return new RCardinality(PositiveIntegerInterval.bounded(min, max));
	}
	public static RCardinality unbounded(int min) {
		return new RCardinality(PositiveIntegerInterval.boundedLeft(min));
	}
	
	public int getMin() {
		return interval.getMinBound();
	}
	public Optional<Integer> getMax() {
		return interval.getMax();
	}
	public boolean isMulti() {
		return interval.getMax().map(m -> m > 1).orElse(true);
	}
	public boolean isOptional() {
		return interval.getMinBound() == 0;
	}
	
	public boolean includes(int x) {
		return interval.includes(x);
	}
	public boolean includes(RCardinality other) {
		return interval.includes(other.interval);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		builder.append(interval.getMinBound());
		builder.append("..");
		interval.getMax().ifPresentOrElse(
				b -> builder.append(b),
				() -> builder.append('*'));
		builder.append(')');
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(interval);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RCardinality other = (RCardinality) obj;
		return Objects.equals(interval, other.interval);
	}
}
