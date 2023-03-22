package com.regnosys.rosetta.generator;

import java.util.NoSuchElementException;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

public class GeneratedIdentifier implements TargetLanguageRepresentation {
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
		return this.getClass().getSimpleName() + " (desired name=\"" + desiredName + "\")";
	}

	@Override
	public void appendTo(TargetStringConcatenation target) {
		String actualName = getActualName();
		target.append(actualName);
	}
	
	protected String getActualName() {
		return this.scope.getActualName(this)
				.orElseThrow(() -> new NoSuchElementException("No actual name for " + this.toString() + " in scope.\n" + scope));
	}
}
