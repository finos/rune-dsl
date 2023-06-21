package com.regnosys.rosetta.generator.java;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaBlueprint;

public class RuleOutputParameterRepresentation {
	private final RosettaBlueprint rule;
	public RuleOutputParameterRepresentation(RosettaBlueprint rule) {
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
