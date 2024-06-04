package com.regnosys.rosetta.interpreternew.values;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
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

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterEnvironment implements RosettaInterpreterBaseEnvironment {
	private Map<String, RosettaInterpreterValue> environment;
	
	public RosettaInterpreterEnvironment() {
		this.setEnvironment(new HashMap<>());
	}
	
	public RosettaInterpreterEnvironment(Map<String, RosettaInterpreterValue> el) {
		this.setEnvironment(el);
	}

	public Map<String, RosettaInterpreterValue> getEnvironment() {
		return environment;
	}

	public void setEnvironment(Map<String, RosettaInterpreterValue> env) {
		this.environment = env;
	}
	
	/**
	 * Find a value, by name, in the environment.
	 *
	 * @param name - name of the variable you search for
	 * @return - the value iff variable exists in environment
	 * 		   error otherwise
	 */
	public RosettaInterpreterValue findValue(String name) {
		if (environment.containsKey(name)) { 
			return environment.get(name);
		}
		else {
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
							name 
							+ " does not exist in the environment"));
		}
		
	}
	
	/**
	 * Add a variable and its value to the environment.
	 *
	 * @param name - name of the variable
	 * @param val - value of the variable
	 */
	public RosettaInterpreterValue addValue(String name, 
			RosettaInterpreterValue val) {
		
		if (environment.containsKey(name)) { 
			//update env
			return environment.replace(name, val);
		}
		else {
			return environment.put(name, val);
		}
		
	}

	
	
	@Override
	public int hashCode() {
		return Objects.hash(environment);
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
		RosettaInterpreterEnvironment other = (RosettaInterpreterEnvironment) obj;
		return Objects.equals(environment, other.environment);
	}
	

	@Override
	public String toString() {
		return "RosettaInterpreterEnvironment [environment=" + environment + "]";
	}

	// womp womppppp
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
	public Object eInvoke(EOperation operation, EList<?> arguments) throws InvocationTargetException {
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
