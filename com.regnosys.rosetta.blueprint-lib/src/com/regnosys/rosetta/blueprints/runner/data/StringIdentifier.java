package com.regnosys.rosetta.blueprints.runner.data;

import java.util.Comparator;

public class StringIdentifier implements DataIdentifier, Comparable<StringIdentifier> {
	private final String s;

	public StringIdentifier(String s) {
		this.s = s;
	}

	public String getS() {
		return s;
	}
	@Override
	public String toString() {
		return s;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (s == null) {
			if (other.s != null)
				return false;
		} else if (!s.equals(other.s))
			return false;
		return true;
	}

	@Override
	public int compareTo(StringIdentifier o) {
		return Comparator.comparing(StringIdentifier::getS).compare(this, o);
	}	
}
