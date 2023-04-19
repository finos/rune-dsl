package com.regnosys.rosetta.types.builtin;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.regnosys.rosetta.utils.OptionalUtil;
import com.regnosys.rosetta.utils.PositiveIntegerInterval;

public class RStringType extends RBasicType {
	private static final String MIN_LENGTH_PARAM_NAME = "minLength";
	private static final String MAX_LENGTH_PARAM_NAME = "maxLength";
	private static final String PATTERN_PARAM_NAME = "pattern";
	
	private final PositiveIntegerInterval interval;
	private final Optional<Pattern> pattern;
	
	public RStringType(PositiveIntegerInterval interval, Optional<Pattern> pattern) {
		super("string");
		this.interval = interval;
		this.pattern = pattern;
	}
	public RStringType(Optional<Integer> minLength, Optional<Integer> maxLength, Optional<Pattern> pattern) {
		this(new PositiveIntegerInterval(minLength.orElse(0), maxLength), pattern);
	}
	
	public static RStringType from(Map<String, Object> values) {
		return new RStringType(
				OptionalUtil.typedGet(values, MIN_LENGTH_PARAM_NAME, Integer.class),
				OptionalUtil.typedGet(values, MAX_LENGTH_PARAM_NAME, Integer.class),
				OptionalUtil.typedGet(values, PATTERN_PARAM_NAME, Pattern.class)
			);
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
		return new RStringType(
				interval.minimalCover(other.interval),
				joinedPattern
			);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getName());
		
		int minBound = interval.getMinBound();
		String arguments = Stream.of(
				minBound == 0 ? Optional.<String>empty() : Optional.of(MIN_LENGTH_PARAM_NAME + "=" + minBound),
				interval.getMax().map(m -> MAX_LENGTH_PARAM_NAME + "=" + m),
				pattern.map(p -> PATTERN_PARAM_NAME + "=" + p))
			.filter(o -> o.isPresent())
			.map(o -> o.get())
			.collect(Collectors.joining(", "));
		if (arguments.length() > 0) {
			builder.append("(")
				.append(arguments)
				.append(")");
		}
		return builder.toString();
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
