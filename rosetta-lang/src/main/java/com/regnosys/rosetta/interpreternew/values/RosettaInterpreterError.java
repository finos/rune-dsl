package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseError;

public class RosettaInterpreterError extends MinimalEObjectImpl implements RosettaInterpreterBaseError {
	private String errorMessage;	
	
	public RosettaInterpreterError(String errorMessage) {
		super();
		this.errorMessage = errorMessage;
	}
	
	public String getError() { return errorMessage; }
	
	@Override
	public String toString() {
		return "RosettaInterpreterError [errorMessage=" + errorMessage + "]";
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
		RosettaInterpreterError other = (RosettaInterpreterError) obj;
		return Objects.equals(errorMessage, other.errorMessage);
	}

	@Override
	public String getMessage() {
		return errorMessage;
	}

	@Override
	public void setMessage(String value) {
		this.errorMessage = value;
	}
}
