package com.regnosys.rosetta.types;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;
import com.regnosys.rosetta.utils.BigDecimalInterval;
import com.regnosys.rosetta.utils.IntegerInterval;

public class TypeFactory {
	@Inject
	private RBuiltinTypeService builtinTypes;
	
	public final RosettaCardinality single;
	public final RosettaCardinality empty;
	
	public final RListType singleBoolean;
	public final RListType singleDate;
	public final RListType singleTime;
	public final RListType singleUnconstrainedInt;
	public final RListType singleUnconstrainedNumber;
	public final RListType singleUnconstrainedString;
	public final RListType singleDateTime;
	public final RListType singleZonedDateTime;
	public final RListType emptyNothing;
	
	public TypeFactory() {
		this.single = createConstraint(1, 1);
		
		this.empty = createConstraint(0, 0);
		
		this.singleBoolean = createListType(builtinTypes.BOOLEAN, single);
		this.singleDate = createListType(builtinTypes.DATE, single);
		this.singleTime = createListType(builtinTypes.TIME, single);
		this.singleUnconstrainedInt = createListType(builtinTypes.UNCONSTRAINED_INT, single);
		this.singleUnconstrainedNumber = createListType(builtinTypes.UNCONSTRAINED_NUMBER, single);
		this.singleUnconstrainedString = createListType(builtinTypes.UNCONSTRAINED_STRING, single);
		this.singleDateTime = createListType(builtinTypes.DATE_TIME, single);
		this.singleZonedDateTime = createListType(builtinTypes.ZONED_DATE_TIME, single);
		this.emptyNothing = createListType(builtinTypes.NOTHING, empty);
	}
	
	public RListType singleInt(Optional<Integer> digits, Optional<Integer> min, Optional<Integer> max) {
		return createListType(new RNumberType(digits, Optional.empty(), min.map(BigDecimal::valueOf), max.map(BigDecimal::valueOf), Optional.empty()), single);
	}
	public RListType singleNumber(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			Optional<BigDecimal> min, Optional<BigDecimal> max, Optional<BigDecimal> scale) {
		return createListType(new RNumberType(digits, fractionalDigits, min, max, scale), single);
	}
	public RListType singleNumber(Optional<Integer> digits, Optional<Integer> fractionalDigits, 
			BigDecimalInterval interval, Optional<BigDecimal> scale) {
		return createListType(new RNumberType(digits, fractionalDigits, interval, scale), single);
	}
	public RListType singleString(Optional<Integer> minLength, Optional<Integer> maxLength, Optional<Pattern> pattern) {
		return createListType(new RStringType(minLength, maxLength, pattern), single);
	}
	public RListType singleString(IntegerInterval interval, Optional<Pattern> pattern) {
		return createListType(new RStringType(interval, pattern), single);
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
