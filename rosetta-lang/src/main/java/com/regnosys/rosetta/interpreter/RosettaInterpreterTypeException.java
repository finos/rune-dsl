package com.regnosys.rosetta.interpreter;

/**
 * An exception to indicate a type error at runtime.
 */
public class RosettaInterpreterTypeException extends RosettaInterpreterException {
	private static final long serialVersionUID = 1L;

	public RosettaInterpreterTypeException(String errorMessage) {
		super(errorMessage);
	}
}
