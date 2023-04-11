package com.regnosys.rosetta.types.builtin;

import java.util.Objects;

public class RRecordFeature {
	private final RRecordType recordType;
	private final String name;

	public RRecordFeature(RRecordType recordType, String name) {
		this.recordType = recordType;
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(recordType, name);
	}
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RRecordFeature other = (RRecordFeature) obj;
		return Objects.equals(recordType, other.recordType)
				&& Objects.equals(name, other.name);
	}
}
