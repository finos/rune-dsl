package com.regnosys.rosetta.generator.java;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaType;

public class BlueprintImplicitVariableRepresentation {
	private final RosettaType type;
	
	public BlueprintImplicitVariableRepresentation(RosettaType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getClass(), type);
	}
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        BlueprintImplicitVariableRepresentation other = (BlueprintImplicitVariableRepresentation) object;
        return Objects.equals(type, other.type);
	}
}
