package com.regnosys.rosetta.generator.java;

import java.util.Objects;

import com.regnosys.rosetta.generator.ImplicitVariableRepresentation;
import com.regnosys.rosetta.rosetta.simple.Data;

public class BlueprintImplicitVariableRepresentation extends ImplicitVariableRepresentation {
	public BlueprintImplicitVariableRepresentation(Data definingObject) {
		super(definingObject);
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        BlueprintImplicitVariableRepresentation other = (BlueprintImplicitVariableRepresentation) object;
        return Objects.equals(getDefiningContainer(), other.getDefiningContainer());
	}
}
