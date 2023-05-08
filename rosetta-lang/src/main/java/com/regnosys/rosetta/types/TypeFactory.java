package com.regnosys.rosetta.types;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;
import com.regnosys.rosetta.utils.BigDecimalInterval;
import com.regnosys.rosetta.utils.PositiveIntegerInterval;

public class TypeFactory {
	private final RBuiltinTypeService builtinTypes;
	
	public final RosettaCardinality single;
	public final RosettaCardinality empty;
	
	public final RListType singleBoolean;
	public final RListType singleDate;
	public final RListType singleTime;
	public final RListType singlePattern;
	public final RListType singleUnconstrainedInt;
	public final RListType singleUnconstrainedNumber;
	public final RListType singleUnconstrainedString;
	public final RListType singleDateTime;
	public final RListType singleZonedDateTime;
	public final RListType emptyNothing;
	
	@Inject
	public TypeFactory(RBuiltinTypeService builtinTypes) {
		this.builtinTypes = builtinTypes;
		
		this.single = createConstraint(1, 1);
		
		this.empty = createConstraint(0, 0);
		
		this.singleBoolean = createListType(builtinTypes.BOOLEAN, single);
		this.singleDate = createListType(builtinTypes.DATE, single);
		this.singleTime = createListType(builtinTypes.TIME, single);
		this.singlePattern = createListType(builtinTypes.PATTERN, single);
		this.singleUnconstrainedInt = createListType(builtinTypes.UNCONSTRAINED_INT, single);
		this.singleUnconstrainedNumber = createListType(builtinTypes.UNCONSTRAINED_NUMBER, single);
		this.singleUnconstrainedString = createListType(builtinTypes.UNCONSTRAINED_STRING, single);
		this.singleDateTime = createListType(builtinTypes.DATE_TIME, single);
		this.singleZonedDateTime = createListType(builtinTypes.ZONED_DATE_TIME, single);
		this.emptyNothing = createListType(builtinTypes.NOTHING, empty);
	}
	
	public RListType singleInt(Optional<Integer> digits, Optional<Integer> min, Optional<Integer> max) {
		return createListType(constrainedInt(digits, min, max), single);
	}
	public RListType singleInt(int digits, int min, int max) {
		return createListType(constrainedInt(digits, min, max), single);
	}
	public RAliasType constrainedInt(Optional<Integer> digits, Optional<Integer> min, Optional<Integer> max) {
		RNumberType refersTo = constrainedNumber(digits, Optional.of(0), min.map(BigDecimal::valueOf), max.map(BigDecimal::valueOf), Optional.empty());
		LinkedHashMap<String, RosettaValue> args = new LinkedHashMap<>(refersTo.getArguments());
		args.remove(RNumberType.FRACTIONAL_DIGITS_PARAM_NAME);
		args.remove(RNumberType.SCALE_PARAM_NAME);
		return new RAliasType(builtinTypes.INT_FUNCTION, args, refersTo);
	}
	public RAliasType constrainedInt(int digits, int min, int max) {
		return constrainedInt(Optional.of(digits), Optional.of(min), Optional.of(max));
	}
	
	public RListType singleNumber(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			Optional<BigDecimal> min, Optional<BigDecimal> max, Optional<BigDecimal> scale) {
		return createListType(constrainedNumber(digits, fractionalDigits, min, max, scale), single);
	}
	public RListType singleNumber(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			BigDecimalInterval interval, Optional<BigDecimal> scale) {
		return createListType(constrainedNumber(digits, fractionalDigits, interval, scale), single);
	}
	public RListType singleNumber(int digits, int fractionalDigits, BigDecimal min, BigDecimal max) {
		return createListType(constrainedNumber(digits, fractionalDigits, min, max), single);
	}
	public RListType singleNumber(int digits, int fractionalDigits, String min, String max) {
		return createListType(constrainedNumber(digits, fractionalDigits, min, max), single);
	}
	public RNumberType constrainedNumber(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			Optional<BigDecimal> min, Optional<BigDecimal> max, Optional<BigDecimal> scale) {
		return new RNumberType(digits, fractionalDigits, min, max, scale);
	}
	public RNumberType constrainedNumber(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			BigDecimalInterval interval, Optional<BigDecimal> scale) {
		return new RNumberType(digits, fractionalDigits, interval, scale);
	}
	public RNumberType constrainedNumber(int digits, int fractionalDigits, BigDecimal min, BigDecimal max) {
		return constrainedNumber(Optional.of(digits), Optional.of(fractionalDigits), Optional.of(min), Optional.of(max), Optional.empty());
	}
	public RNumberType constrainedNumber(int digits, int fractionalDigits, String min, String max) {
		return constrainedNumber(Optional.of(digits), Optional.of(fractionalDigits), Optional.of(new BigDecimal(min)), Optional.of(new BigDecimal(max)), Optional.empty());
	}
	
	public RListType singleString(Optional<Integer> minLength, Optional<Integer> maxLength, Optional<Pattern> pattern) {
		return createListType(constrainedString(minLength, maxLength, pattern), single);
	}
	public RListType singleString(PositiveIntegerInterval interval, Optional<Pattern> pattern) {
		return createListType(constrainedString(interval, pattern), single);
	}
	public RListType singleString(int minLength, int maxLength) {
		return createListType(constrainedString(minLength, maxLength), single);
	}
	public RStringType constrainedString(Optional<Integer> minLength, Optional<Integer> maxLength, Optional<Pattern> pattern) {
		return new RStringType(minLength, maxLength, pattern);
	}
	public RStringType constrainedString(PositiveIntegerInterval interval, Optional<Pattern> pattern) {
		return new RStringType(interval, pattern);
	}
	public RStringType constrainedString(int minLength, int maxLength) {
		return new RStringType(Optional.of(minLength), Optional.of(maxLength), Optional.empty());
	}
	
	public RosettaCardinality createConstraint(int inf, int sup) {
		RosettaCardinality c = RosettaFactory.eINSTANCE.createRosettaCardinality();
		c.setInf(inf);
		c.setSup(sup);
		return c;
	}
	public RosettaCardinality createConstraint(int inf) {
		RosettaCardinality c = RosettaFactory.eINSTANCE.createRosettaCardinality();
		c.setInf(inf);
		c.setUnbounded(true);
		return c;
	}
	
	public RListType createListType(RType itemType, RosettaCardinality constraint) {
		return new RListType(itemType, constraint);
	}
	public RListType createListType(RType itemType, int inf, int sup) {
		return createListType(itemType, createConstraint(inf, sup));
	}
	public RListType createListType(RType itemType, int inf) {
		return createListType(itemType, createConstraint(inf));
	}
}
