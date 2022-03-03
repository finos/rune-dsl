package com.regnosys.rosetta.types;

import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaFactory;

public class TypeFactory {
	private final RosettaCardinality single;
	private final RosettaCardinality empty;
	
	public final RListType singleBoolean;
	public final RListType singleString;
	public final RListType singleNumber;
	public final RListType singleInt;
	public final RListType emptyNothing;
	
	public TypeFactory() {
		this.single = RosettaFactory.eINSTANCE.createRosettaCardinality();
		this.single.setInf(1);
		this.single.setSup(1);
		
		this.empty = RosettaFactory.eINSTANCE.createRosettaCardinality();
		this.empty.setInf(0);
		this.empty.setSup(0);
		
		this.singleBoolean = new RListType(RBuiltinType.BOOLEAN, single);
		this.singleString = new RListType(RBuiltinType.STRING, single);
		this.singleNumber = new RListType(RBuiltinType.NUMBER, single);
		this.singleInt = new RListType(RBuiltinType.INT, single);
		this.emptyNothing = new RListType(RBuiltinType.NOTHING, empty);
	}
}
