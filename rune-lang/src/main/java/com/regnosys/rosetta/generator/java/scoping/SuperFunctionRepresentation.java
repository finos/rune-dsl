package com.regnosys.rosetta.generator.java.scoping;

import java.util.Objects;

import com.regnosys.rosetta.types.RFunction;

public record SuperFunctionRepresentation(RFunction superFunction) {
	@Override
	public String toString() {
		return "SuperFunctionRepresentation[" + superFunction.getAlphanumericName() + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getClass(), superFunction);
	}
}
