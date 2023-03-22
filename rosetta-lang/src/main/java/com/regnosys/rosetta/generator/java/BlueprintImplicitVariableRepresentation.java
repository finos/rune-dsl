package com.regnosys.rosetta.generator.java;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RType;

public class BlueprintImplicitVariableRepresentation {
	private final RType type;
	
	public BlueprintImplicitVariableRepresentation(RType type) {
		this.type = type;
	}
	
	public RType getType() {
		return type;
	}
	
	public boolean match(BlueprintImplicitVariableRepresentation other) {
		if (type.equals(other.type)) {
			return true;
		}
		if (!(type instanceof RDataType) || !(other.type instanceof RDataType)) {
			return false;
		}
		Data data = ((RDataType)type).getData();
		Data otherData = ((RDataType)other.type).getData();
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
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        BlueprintImplicitVariableRepresentation other = (BlueprintImplicitVariableRepresentation) object;
        return Objects.equals(type, other.type);
	}
}
