package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseError;

public class RosettaInterpreterError extends MinimalEObjectImpl implements RosettaInterpreterBaseError {	
	private String errorMessage;
	private EObject associatedObject;
	
	@Override
	public int hashCode() {
		return Objects.hash(associatedObject, errorMessage);
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

	public RosettaInterpreterError(String errorMessage, EObject obj) {
		this.associatedObject = obj;
		this.errorMessage = errorMessage;
	}
	
	public String getError() { return errorMessage; }
	
	public EObject getEobject() { return associatedObject; }
	
	/**
	 * Gives a parsed error message associated with this error.
	 * Gets the INode associated in order to provide details of where the erorr ocurred.
	 *
	 * @return Error message with code information
	 */
	public String properErrorMessage() {
		if (associatedObject == null) {
			return errorMessage;
		}
		
		INode node = NodeModelUtils.findActualNodeFor(associatedObject);
		int startLine = node.getStartLine();
	    int offset = node.getOffset();
	    String text = node.getText().trim();
		String message = "Error at line " + startLine + ", position " + offset + ": "
	    + "\"" + text + "\". " + errorMessage;
		return message;
	}
	
	@Override
	public String toString() {
		return properErrorMessage();
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
