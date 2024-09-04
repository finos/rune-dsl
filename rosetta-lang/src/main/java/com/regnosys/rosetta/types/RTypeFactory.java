package com.regnosys.rosetta.types;

import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.simple.Data;

public class RTypeFactory {
	public RDataType dataToType(Data data) {
		return new RDataType(data);
	}
	public REnumType enumToType(RosettaEnumeration enumeration) {
		return new REnumType(enumeration);
	}
}
