package com.regnosys.rosetta.blueprints.runner.data;

import java.util.Optional;

public class RuleIdentifier implements DataIdentifier {
	
	private final String label;
	private final Class<?> ruleType;
	private final boolean repeatable;
	private final Optional<Integer> repeatableIndex;
	
	public RuleIdentifier(String label, Class<?> ruleType, boolean repeatable) {
		this.label = label;
		this.ruleType = ruleType;
		this.repeatable = repeatable;
		this.repeatableIndex = Optional.empty();
	}
	
	public RuleIdentifier(RuleIdentifier rule, int repeatableIndex) {
		this.label = rule.label.contains("$") ? 
				rule.label.replace("$", String.valueOf(repeatableIndex)) : 
				String.format("%s (%d)", rule.label, repeatableIndex);
		this.ruleType = rule.ruleType;
		this.repeatable = rule.repeatable;
		this.repeatableIndex = Optional.of(repeatableIndex);
	}
	
	public String getLabel() {
		return label;
	}

	public Class<?> getRuleType() {
		return ruleType;
	}
	
	public boolean isRepeatable() {
		return repeatable;
	}

	public Optional<Integer> getRepeatableIndex() {
		return repeatableIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ruleType == null) ? 0 : ruleType.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((repeatableIndex == null) ? 0 : repeatableIndex.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RuleIdentifier other = (RuleIdentifier) obj;
		if (ruleType == null) {
			if (other.ruleType != null)
				return false;
		} else if (!ruleType.equals(other.ruleType))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (repeatableIndex == null) {
			if (other.repeatableIndex != null)
				return false;
		} else if (!repeatableIndex.equals(other.repeatableIndex))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RuleIdentifier [label=" + label + ", ruleType=" + ruleType + ", repeatableIndex=" + repeatableIndex + "]";
	}
}
