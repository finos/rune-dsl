package com.regnosys.rosetta.blueprints.runner.data;

import java.util.Optional;

public class RuleIdentifier extends StringIdentifier {
	
	private final Class<?> ruleType;
	private final boolean repeatable;
	private final Optional<Integer> repeatableIndex;
	
	public RuleIdentifier(String label, Class<?> ruleType, boolean repeatable) {
		super(label);
		this.ruleType = ruleType;
		this.repeatable = repeatable;
		this.repeatableIndex = Optional.empty();
	}
	
	public RuleIdentifier(RuleIdentifier rule, int repeatableIndex) {
		// add index to label
		super(rule.getId().contains("$") ? 
				rule.getId().replace("$", String.valueOf(repeatableIndex)) : 
				String.format("%s (%d)", rule.getId(), repeatableIndex));
		this.ruleType = rule.ruleType;
		this.repeatable = rule.repeatable;
		this.repeatableIndex = Optional.of(repeatableIndex);
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
		int result = super.hashCode();
		result = prime * result + (repeatable ? 1231 : 1237);
		result = prime * result + ((repeatableIndex == null) ? 0 : repeatableIndex.hashCode());
		result = prime * result + ((ruleType == null) ? 0 : ruleType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RuleIdentifier other = (RuleIdentifier) obj;
		if (repeatable != other.repeatable)
			return false;
		if (repeatableIndex == null) {
			if (other.repeatableIndex != null)
				return false;
		} else if (!repeatableIndex.equals(other.repeatableIndex))
			return false;
		if (ruleType == null) {
			if (other.ruleType != null)
				return false;
		} else if (!ruleType.equals(other.ruleType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RuleIdentifier [ruleType=" + ruleType + ", repeatable=" + repeatable + ", repeatableIndex="
				+ repeatableIndex + ", id=" + getId() + "]";
	}
}
