package com.regnosys.rosetta.generator.java.scoping;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.types.RFunction;

public class SuperFunctionRepresentation {
    private final RFunction superFunction;
    public SuperFunctionRepresentation(RFunction superFunction) {
        this.superFunction = superFunction;
    }
    
	@Override
	public String toString() {
		return "SuperFunctionRepresentation[" + superFunction.getAlphanumericName() + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getClass(), superFunction);
	}
    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        SuperFunctionRepresentation other = (SuperFunctionRepresentation) object;
        return Objects.equals(superFunction, other.superFunction);
    }
}
