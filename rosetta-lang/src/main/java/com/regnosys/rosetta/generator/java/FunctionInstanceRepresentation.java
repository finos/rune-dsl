package com.regnosys.rosetta.generator.java;

import java.util.Objects;

import com.regnosys.rosetta.types.RFunction;

public class FunctionInstanceRepresentation {
	private final RFunction func;
	public FunctionInstanceRepresentation(RFunction func) {
		this.func = func;
	}
	
	@Override
	public String toString() {
		return "FunctionInstance[" + func.getQualifiedName().withDots() + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getClass(), func);
	}
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        FunctionInstanceRepresentation other = (FunctionInstanceRepresentation) object;
        return Objects.equals(func, other.func);
	}
}
