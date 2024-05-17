package com.regnosys.rosetta.interpreternew.values;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseError;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

<<<<<<< HEAD
public class RosettaInterpreterBaseValue implements RosettaInterpreterValue {
	List<RosettaInterpreterBaseError> errors;
	//private List<RosettaInterpreterBaseValue> items;
	
	@Override
	public EList<RosettaInterpreterBaseError> getErrors() {
		return new BasicEList<RosettaInterpreterBaseError>(errors);
	}
	
	@Override
	public boolean addError(RosettaInterpreterBaseError error) {
		return errors.add(error);
	}

	@Override
	public boolean addAllErrors(RosettaInterpreterValue other) {
		return errors.addAll(other.getErrors());
	}

	@Override
	public boolean addAllErrors(EList<RosettaInterpreterBaseError> errors) {
		return errors.addAll(errors);
	}
	
	
	public RosettaInterpreterBaseValue() {
		this.errors = new ArrayList<>();
	}
	
	public RosettaInterpreterBaseValue(RosettaInterpreterError error) {
		this.errors = new ArrayList<>();
		errors.add(error);
	}
	
=======
public abstract class RosettaInterpreterBaseValue implements RosettaInterpreterValue{	
>>>>>>> dev
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
}
