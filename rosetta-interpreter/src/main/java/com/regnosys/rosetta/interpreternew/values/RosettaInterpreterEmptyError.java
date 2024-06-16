package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseError;

public class RosettaInterpreterEmptyError extends MinimalEObjectImpl implements RosettaInterpreterBaseError {

	private String errorMessage;
	
	public RosettaInterpreterEmptyError(String message) {
		this.errorMessage = message;
	}
	
	public RosettaInterpreterEmptyError() {
		this.errorMessage = "";
	}
	
	@Override
	public String getMessage() {
		return errorMessage;
	}

	@Override
	public void setMessage(String value) {
		this.errorMessage = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(errorMessage);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RosettaInterpreterEmptyError other = (RosettaInterpreterEmptyError) obj;
		return Objects.equals(errorMessage, other.errorMessage);
	}

	@Override
	public String toString() {
		return errorMessage;
	}
}
