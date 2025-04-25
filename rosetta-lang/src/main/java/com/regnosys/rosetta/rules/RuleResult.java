package com.regnosys.rosetta.rules;

import org.eclipse.emf.ecore.EObject;

import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.RuleReferenceAnnotation;

public class RuleResult {
	private final RosettaRule rule;
	private final EObject origin;
	
	private RuleResult(RosettaRule rule, EObject origin) {
		this.rule = rule;
		this.origin = origin;
	}
	
	public static RuleResult fromAnnotation(RuleReferenceAnnotation annotation) {
		return new RuleResult(annotation.getReportingRule(), annotation);
	}
	public static RuleResult explicitlyEmptyFromMinusInRuleSource(RosettaExternalRegularAttribute minusAttribute) {
		return new RuleResult(null, minusAttribute);
	}
	
	public boolean isExplicitlyEmpty() {
		return rule == null;
	}
	
	public RosettaRule getRule() {
		return rule;
	}
	public EObject getOrigin() {
		return origin;
	}
	
}
