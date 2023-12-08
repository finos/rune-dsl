package com.regnosys.rosetta.resource;

import java.util.Map;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;

import com.regnosys.rosetta.rosetta.RosettaRule;

public class RuleDescription extends EObjectDescription {
	public static final String TYPE_CALL = "TYPE_CALL";

	public RuleDescription(QualifiedName qualifiedName, RosettaRule rule, String typeCall) {
		super(qualifiedName, rule, Map.of(TYPE_CALL, typeCall));
	}
}
