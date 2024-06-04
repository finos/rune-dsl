package com.regnosys.rosetta.interpreternew.values;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.regnosys.rosetta.interpreternew.RosettaInterpreterNewException;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public abstract class RosettaInterpreterBaseValue implements RosettaInterpreterValue {
	
	/**
	 * Converts a Value to a Stream of the elements it contains.
	 * This is done this way and not defined inside of RosettaInterpreterValue
	 * because xcore does not seem to have a notion of streams
	 *
	 * @return stream of elements of this value
	 */
	public abstract Stream<Object> toElementStream();
	
	/**
	 * Converts a Value to a Stream of itself or the values it contains.
	 * This is done this way and not defined inside of RosettaInterpreterValue
	 * because xcore does not seem to have a notion of streams
	 *
	 * @return stream of this value or values it contains
	 */
	public abstract Stream<RosettaInterpreterValue> toValueStream();
	
	/**
	 * Converts a value to a stream of its elements.
	 *
	 * @param val - value to extract stream out of
	 * @return a stream of elements iff val is a RosettaInterpreterBaseValue
	 */
	public static Stream<Object> elementStream(RosettaInterpreterValue val) {
		if (!(val instanceof RosettaInterpreterBaseValue)) {
			throw new RosettaInterpreterNewException("Cannot take element stream"
					+ "of RosettaInterpreterValue");
		}
		return ((RosettaInterpreterBaseValue)val).toElementStream();
	}
	
	/**
	 * Converts a value to a stream of itself or its contained values.
	 *
	 * @param val - value to convert
	 * @return stream of value or its contained values
	 */
	public static Stream<RosettaInterpreterValue> valueStream(RosettaInterpreterValue val) {
		if (!(val instanceof RosettaInterpreterBaseValue)) {
			throw new RosettaInterpreterNewException("Cannot take value stream"
					+ "of RosettaInterpreterValue");
			}
		return ((RosettaInterpreterBaseValue)val).toValueStream();
	}
	
	/**
	 * Converts a rosetta value to a list of itself or values it contains.
	 *
	 * @param val - value to convert
	 * @return - list of value or its contained values
	 */
	public static List<RosettaInterpreterValue> toValueList(RosettaInterpreterValue val) {
		if (!(val instanceof RosettaInterpreterBaseValue)) {
			throw new RosettaInterpreterNewException("Cannot take value stream"
					+ "of RosettaInterpreterValue");
			}
		return valueStream(val).collect(Collectors.toList());
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
	
}
