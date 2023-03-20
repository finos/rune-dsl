package com.regnosys.rosetta.generator;

public class GeneratedIdentifier {
	private final GeneratorScope<?> scope;
	private final String desiredName;
	
	public GeneratedIdentifier(GeneratorScope<?> scope, String desiredName) {
		this.scope = scope;
		this.desiredName = desiredName;
	}
	
	public String getDesiredName() {
		return this.desiredName;
	}
	
	@Override
	public String toString() {
		return this.scope.getActualName(this);
	}
}
