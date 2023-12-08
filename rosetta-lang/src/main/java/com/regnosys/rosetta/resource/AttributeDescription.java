package com.regnosys.rosetta.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;

import com.regnosys.rosetta.rosetta.simple.Attribute;

public class AttributeDescription extends EObjectDescription {
	public static final String TYPE_CALL = "TYPE_CALL";
	public static final String CARDINALITY = "CARDINALITY";
	// TODO: also include metadata

	public AttributeDescription(QualifiedName qualifiedName, Attribute attribute, String typeCall, String cardinality) {
		super(qualifiedName, attribute, createUserData(typeCall, cardinality));
	}
	
	private static Map<String, String> createUserData(String typeCall, String cardinality) {
		Map<String, String> userData = new HashMap<>();
		if (typeCall != null) {
			userData.put(TYPE_CALL, typeCall);
		}
		if (cardinality != null) {
			userData.put(CARDINALITY, cardinality);
		}
		return userData;
	}
}
