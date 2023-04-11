package com.regnosys.rosetta.types.builtin;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import com.regnosys.rosetta.utils.IntegerInterval;
import com.regnosys.rosetta.utils.OptionalUtil;

public class RStringType extends RBasicType {
	private final IntegerInterval interval;
	private final Optional<Pattern> pattern;
	
	public RStringType(IntegerInterval interval, Optional<Pattern> pattern) {
		super("string");
		this.interval = interval;
		this.pattern = pattern;
	}
	public RStringType(Optional<Integer> minLength, Optional<Integer> maxLength, Optional<Pattern> pattern) {
		this(new IntegerInterval(minLength, maxLength), pattern);
	}
	
	public static RStringType from(Map<String, Object> values) {
		return new RStringType(
				OptionalUtil.typedGet(values, "minLength", Integer.class),
				OptionalUtil.typedGet(values, "maxLength", Integer.class),
				OptionalUtil.typedGet(values, "pattern", Pattern.class)
			);
	}
	
	public IntegerInterval getInterval() {
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
		return new RStringType(
				interval.minimalCover(other.interval),
				joinedPattern
			);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), interval, pattern);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RStringType other = (RStringType) obj;
		return Objects.equals(getName(), other.getName())
				&& Objects.equals(interval, other.interval)
				&& Objects.equals(pattern, other.pattern);
	}
}
