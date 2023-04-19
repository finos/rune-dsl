package com.regnosys.rosetta.types.builtin;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;

import com.regnosys.rosetta.utils.BigDecimalInterval;
import com.regnosys.rosetta.utils.OptionalUtil;

public class RNumberType extends RBasicType {
	private static final String DIGITS_PARAM_NAME = "digits";
	private static final String FRACTIONAL_DIGITS_PARAM_NAME = "fractionalDigits";
	private static final String MIN_PARAM_NAME = "min";
	private static final String MAX_PARAM_NAME = "max";
	private static final String SCALE_PARAM_NAME = "scale";
	
	private static final String INT_NAME = "int";
	
	private final Optional<Integer> digits;
	private final Optional<Integer> fractionalDigits;
	private final BigDecimalInterval interval;
	private final Optional<BigDecimal> scale;
	
	public RNumberType(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			BigDecimalInterval interval, Optional<BigDecimal> scale) {
		super("number");
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
	public RNumberType(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			Optional<BigDecimal> min, Optional<BigDecimal> max, Optional<BigDecimal> scale) {
		this(digits, fractionalDigits, new BigDecimalInterval(min, max), scale);
	}
	
	public static RNumberType from(Map<String, Object> values) {
		return new RNumberType(
				OptionalUtil.typedGet(values, DIGITS_PARAM_NAME, Integer.class),
				OptionalUtil.typedGet(values, FRACTIONAL_DIGITS_PARAM_NAME, Integer.class),
				OptionalUtil.typedGet(values, MIN_PARAM_NAME, BigDecimal.class),
				OptionalUtil.typedGet(values, MAX_PARAM_NAME, BigDecimal.class),
				OptionalUtil.typedGet(values, SCALE_PARAM_NAME, BigDecimal.class)
			);
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
		return new RNumberType(
				OptionalUtil.zipWith(digits, other.digits, Math::max),
				OptionalUtil.zipWith(fractionalDigits, other.fractionalDigits, Math::max),
				interval.minimalCover(other.interval),
				joinedScale
			);
	}
	
	@Override
	public String getName() {
		boolean isInteger = fractionalDigits.map(d -> d == 0).orElse(false);
		if (isInteger) {
			return INT_NAME;
		} else {
			return super.getName();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		boolean isInteger = fractionalDigits.map(d -> d == 0).orElse(false);
		builder.append(getName());
		
		String arguments = Stream.of(
				digits.map(d -> DIGITS_PARAM_NAME + "=" + d),
				isInteger ? Optional.<String>empty() : fractionalDigits.map(d -> FRACTIONAL_DIGITS_PARAM_NAME + "=" + d),
				interval.getMin().map(m -> MIN_PARAM_NAME + "=" + m),
				interval.getMax().map(m -> MAX_PARAM_NAME + "=" + m),
				scale.map(s -> SCALE_PARAM_NAME + "=" + s))
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
		return Objects.hash(getName(), digits, fractionalDigits, interval, scale);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RNumberType other = (RNumberType) obj;
		return Objects.equals(getName(), other.getName())
				&& Objects.equals(digits, other.digits)
				&& Objects.equals(fractionalDigits, other.fractionalDigits)
				&& Objects.equals(interval, other.interval)
				&& Objects.equals(scale, other.scale);
	}
}
