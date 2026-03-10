package com.regnosys.rosetta.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;

import com.regnosys.rosetta.rosetta.RosettaRule;

public class RuleDescription extends RosettaDescription {
	public static final String INPUT = "INPUT";
	public static final String EXPRESSION = "EXPRESSION";

	public RuleDescription(QualifiedName qualifiedName, RosettaRule rule, String input, String expression, boolean isInOverriddenNamespace) {
		super(qualifiedName, rule, createUserData(input, expression), isInOverriddenNamespace);
	}
	
	private static Map<String, String> createUserData(String input, String expression) {
		Map<String, String> userData = new HashMap<>();
		if (input != null) {
			userData.put(INPUT, input);
		}
		if (expression != null) {
			userData.put(EXPRESSION, expression);
		}
		return userData;
	}
}
