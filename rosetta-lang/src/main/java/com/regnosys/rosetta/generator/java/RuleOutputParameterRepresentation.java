package com.regnosys.rosetta.generator.java;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaRule;

public class RuleOutputParameterRepresentation {
	private final RosettaRule rule;
	public RuleOutputParameterRepresentation(RosettaRule rule) {
		this.rule = rule;
	}
	
	@Override
	public String toString() {
		return "RuleInputParameter[" + rule.getName() + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getClass(), rule);
	}
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        RuleOutputParameterRepresentation other = (RuleOutputParameterRepresentation) object;
        return Objects.equals(rule, other.rule);
	}
}
