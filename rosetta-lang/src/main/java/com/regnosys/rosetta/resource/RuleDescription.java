package com.regnosys.rosetta.resource;

import java.util.Map;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;

import com.regnosys.rosetta.rosetta.RosettaRule;

public class RuleDescription extends EObjectDescription {
	public static final String INPUT = "INPUT";

	public RuleDescription(QualifiedName qualifiedName, RosettaRule rule, String input) {
		super(qualifiedName, rule, Map.of(INPUT, input));
	}
}
