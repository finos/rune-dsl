package com.regnosys.rosetta.generator.java;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.simple.Function;
import com.rosetta.util.types.JavaClass;

public class FunctionInstanceRepresentation {
	private final JavaClass func;
	public FunctionInstanceRepresentation(JavaClass func) {
		this.func = func;
	}
	
	@Override
	public String toString() {
		return "FunctionInstance[" + func.getCanonicalName() + "]";
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
