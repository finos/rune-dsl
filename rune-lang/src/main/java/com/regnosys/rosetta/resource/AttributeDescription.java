package com.regnosys.rosetta.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;

import com.regnosys.rosetta.rosetta.simple.Attribute;

public class AttributeDescription extends EObjectDescription {
	public static final String TYPE_CALL = "TYPE_CALL";
	public static final String CARDINALITY = "CARDINALITY";
	public static final String RULE_REFERENCES = "RULE_REFERENCES";
	public static final String LABELS = "LABELS";
	// TODO: also include metadata

	public AttributeDescription(QualifiedName qualifiedName, Attribute attribute, String typeCall, String cardinality, String ruleReferences, String labels) {
		super(qualifiedName, attribute, createUserData(typeCall, cardinality, ruleReferences, labels));
	}
	
	private static Map<String, String> createUserData(String typeCall, String cardinality, String ruleReferences, String labels) {
		Map<String, String> userData = new HashMap<>();
		if (typeCall != null) {
			userData.put(TYPE_CALL, typeCall);
		}
		if (cardinality != null) {
			userData.put(CARDINALITY, cardinality);
		}
		if (ruleReferences != null) {
			userData.put(RULE_REFERENCES, ruleReferences);
		}
		if (labels != null) {
			userData.put(LABELS, labels);
		}
		return userData;
	}
}
