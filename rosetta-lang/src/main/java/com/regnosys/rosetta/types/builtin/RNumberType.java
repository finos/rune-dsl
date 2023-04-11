package com.regnosys.rosetta.types.builtin;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import com.regnosys.rosetta.utils.BigDecimalInterval;
import com.regnosys.rosetta.utils.OptionalUtil;

public class RNumberType extends RBasicType {
	private final Optional<Integer> digits;
	private final Optional<Integer> fractionalDigits;
	private final BigDecimalInterval interval;
	private final Optional<BigDecimal> scale;
	
	public RNumberType(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			BigDecimalInterval interval, Optional<BigDecimal> scale) {
		super("number");
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
				OptionalUtil.typedGet(values, "digits", Integer.class),
				OptionalUtil.typedGet(values, "fractionalDigits", Integer.class),
				OptionalUtil.typedGet(values, "min", BigDecimal.class),
				OptionalUtil.typedGet(values, "max", BigDecimal.class),
				OptionalUtil.typedGet(values, "scale", BigDecimal.class)
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
