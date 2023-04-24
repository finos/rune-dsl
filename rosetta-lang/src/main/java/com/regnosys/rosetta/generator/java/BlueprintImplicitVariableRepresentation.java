package com.regnosys.rosetta.generator.java;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RDataType;

public class BlueprintImplicitVariableRepresentation {
	private final RDataType type;
	
	public BlueprintImplicitVariableRepresentation(RDataType type) {
		this.type = type;
	}
	
	public RDataType getType() {
		return type;
	}
	
	public boolean match(BlueprintImplicitVariableRepresentation other) {
		if (type.equals(other.type)) {
			return true;
		}
		Data data = type.getData();
		Data otherData = other.type.getData();
		while (otherData.hasSuperType()) {
			otherData = otherData.getSuperType();
			if (data.equals(otherData)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "BlueprintImplicitVariable[" + type.getName() + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getClass(), type);
	}
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        BlueprintImplicitVariableRepresentation other = (BlueprintImplicitVariableRepresentation) object;
        return Objects.equals(type, other.type);
	}
}
