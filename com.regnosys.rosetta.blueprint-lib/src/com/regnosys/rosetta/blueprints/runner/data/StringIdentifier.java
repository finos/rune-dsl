package com.regnosys.rosetta.blueprints.runner.data;

public class StringIdentifier implements DataIdentifier {
	private final String s;
	private final Class<?> rule;

	public StringIdentifier(String s) {
		this(s, null);
	}
	
	public StringIdentifier(String s, Class<?> rule) {
		this.s = s;
		this.rule = rule;
	}

	public String getS() {
		return s;
	}
	
	public Class<?> getRule() {
		return rule;
	}

	@Override
	public String toString() {
		return "StringIdentifier [s=" + s + ", rule=" + rule + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
//		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
		result = prime * result + ((s == null) ? 0 : s.hashCode());
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
		StringIdentifier other = (StringIdentifier) obj;
//		if (rule == null) {
//			if (other.rule != null)
//				return false;
//		} else if (!rule.equals(other.rule))
//			return false;
		if (s == null) {
			if (other.s != null)
				return false;
		} else if (!s.equals(other.s))
			return false;
		return true;
	}
	
	
}
