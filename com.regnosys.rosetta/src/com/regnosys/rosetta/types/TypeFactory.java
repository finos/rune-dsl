package com.regnosys.rosetta.types;

import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaFactory;

public class TypeFactory {
	public final RosettaCardinality single;
	public final RosettaCardinality empty;
	
	public final RListType singleBoolean;
	public final RListType singleString;
	public final RListType singleNumber;
	public final RListType singleInt;
	public final RListType singleDate;
	public final RListType singleTime;
	public final RListType singleDateTime;
	public final RListType singleZonedDateTime;
	public final RListType emptyNothing;
	
	public TypeFactory() {
		this.single = createConstraint(1, 1);
		
		this.empty = createConstraint(0, 0);
		
		this.singleBoolean = createListType(RBuiltinType.BOOLEAN, single);
		this.singleString = createListType(RBuiltinType.STRING, single);
		this.singleNumber = createListType(RBuiltinType.NUMBER, single);
		this.singleInt = createListType(RBuiltinType.INT, single);
		this.singleDate = createListType(RBuiltinType.DATE, single);
		this.singleTime = createListType(RBuiltinType.TIME, single);
		this.singleDateTime = createListType(RBuiltinType.DATE_TIME, single);
		this.singleZonedDateTime = createListType(RBuiltinType.ZONED_DATE_TIME, single);
		this.emptyNothing = createListType(RBuiltinType.NOTHING, empty);
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
