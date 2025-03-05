package com.regnosys.rosetta.utils;

import com.regnosys.rosetta.types.RFeature;

public class PojoPropertyUtil { 
	
	public static String toPojoPropertyNames(RFeature seg) {
		return toPojoPropertyNames(seg.getName());
	}
	
	public static String toPojoPropertyNames(String rosettaName) {
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
