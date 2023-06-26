package com.regnosys.rosetta.generator.java;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaBlueprint;

public class RuleInstanceRepresentation {
	private final RosettaBlueprint rule;
	public RuleInstanceRepresentation(RosettaBlueprint rule) {
		this.rule = rule;
	}
	
	@Override
	public String toString() {
		return "RuleInstance[" + rule.getName() + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getClass(), rule);
	}
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        RuleInstanceRepresentation other = (RuleInstanceRepresentation) object;
        return Objects.equals(rule, other.rule);
	}
}
