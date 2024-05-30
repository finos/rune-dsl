package com.regnosys.rosetta.interpreternew.values;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseError;

public class RosettaInterpreterError implements RosettaInterpreterBaseError {
	
	private String errorMessage;
	private EObject associatedExpression;
	
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

	
	@Deprecated
	public RosettaInterpreterError(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public RosettaInterpreterError(EObject obj) {
		this.associatedExpression = obj;
		this.errorMessage = "";
	}
	
	public RosettaInterpreterError(String errorMessage, EObject obj) {
		this.associatedExpression = obj;
		this.errorMessage = errorMessage;
	}
	
	public String getError() { return errorMessage; }
	
	public EObject getEobject() { return associatedExpression; }
	
	/**
	 * Gives a parsed error message associated with this error.
	 * Gets the INode associated in order to provide details of where the erorr ocurred.
	 *
	 * @return Error message with code information
	 */
	public String properErrorMessage() {
		INode node = NodeModelUtils.findActualNodeFor(associatedExpression);
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
		//return "RosettaInterpreterError [errorMessage=" + errorMessage + "]";
	}

	@Override
	public EClass eClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource eResource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EObject eContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EStructuralFeature eContainingFeature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EReference eContainmentFeature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EList<EObject> eContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeIterator<EObject> eAllContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean eIsProxy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EList<EObject> eCrossReferences() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object eGet(EStructuralFeature feature) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object eGet(EStructuralFeature feature, boolean resolve) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void eSet(EStructuralFeature feature, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean eIsSet(EStructuralFeature feature) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void eUnset(EStructuralFeature feature) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object eInvoke(EOperation operation, EList<?> arguments)
			throws InvocationTargetException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EList<Adapter> eAdapters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean eDeliver() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void eSetDeliver(boolean deliver) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eNotify(Notification notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getMessage() {
		return errorMessage;
	}

	@Override
	public void setMessage(String value) {
		// TODO Auto-generated method stub
		
	}
}
