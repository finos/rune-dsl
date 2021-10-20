package com.regnosys.rosetta.blueprints.runner.data;

import java.util.Optional;

public class RuleIdentifier implements DataIdentifier {
	
	private final String label;
	private final Class<?> clazz;
	private final Optional<Integer> repeatableIndex;
	
	public RuleIdentifier(String label, Class<?> clazz) {
		this.label = label;
		this.clazz = clazz;
		this.repeatableIndex = Optional.empty();
	}
	
	public RuleIdentifier(String label, Class<?> clazz, int repeatableIndex) {
		this.label = label;
		this.clazz = clazz;
		this.repeatableIndex = Optional.of(repeatableIndex);
	}
	
	public String getLabel() {
		return label;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Optional<Integer> getRepeatableIndex() {
		return repeatableIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
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
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
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
		return "RuleIdentifier [label=" + label + ", clazz=" + clazz + ", repeatableIndex=" + repeatableIndex + "]";
	}
}
