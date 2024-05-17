package com.regnosys.rosetta.interpreternew.values;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseError;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterErrorValue extends RosettaInterpreterBaseValue{
	List<RosettaInterpreterBaseError> errors;
	
	public RosettaInterpreterErrorValue() {
		this.errors = new ArrayList<>();
	}
	
	public RosettaInterpreterErrorValue(RosettaInterpreterBaseError error) {
		this.errors = new ArrayList<>();
		errors.add(error);
	}
	
	public RosettaInterpreterErrorValue(List<RosettaInterpreterBaseError> errors) {
		this.errors = new ArrayList<>();
		errors.addAll(errors);
	}
	
	public EList<RosettaInterpreterBaseError> getErrors(){
		return new BasicEList<RosettaInterpreterBaseError>(errors);
	}
	
	public boolean addError(RosettaInterpreterBaseError error) {
		return errors.add(error);
	}

	public boolean addAllErrors(RosettaInterpreterErrorValue other) {
		return errors.addAll(other.getErrors());
	}

	public boolean addAllErrors(EList<RosettaInterpreterBaseError> errors) {
		return errors.addAll(errors);
	}
}
