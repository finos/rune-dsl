package com.regnosys.rosetta.utils;

import com.regnosys.rosetta.types.RFeature;

public class PojoPropertyUtil { 
	
	public static String toPojoPropertyName(RFeature seg) {
		return toPojoPropertyName(seg.getName());
	}
	
	public static String toPojoPropertyName(String rosettaName) {
	    switch (rosettaName) {
	        case "reference":
	            return "externalReference";
	        case "id":
	        case "key":
	            return "externalKey";
	        case "address":
	            return "reference";
	        default:
	            return rosettaName;
	    }
	}

}
