package com.regnosys.rosetta.interpreternew.values;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import com.regnosys.rosetta.interpreternew.RosettaInterpreterNewException;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseError;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterErrorValue extends RosettaInterpreterBaseValue {
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
	
	public EList<RosettaInterpreterBaseError> getErrors() {
		return new BasicEList<RosettaInterpreterBaseError>(errors);
	}
	
	public boolean addError(RosettaInterpreterBaseError error) {
		return errors.add(error);
	}

	public boolean addAllErrors(RosettaInterpreterErrorValue other) {
		return errors.addAll(other.getErrors());
	}
	
	/**
	 * Adds all errors from another value if its an error.
	 *
	 * @param other - value to add errors from
	 * @return true iff other is an error and errors were added successfully
	 */
	public boolean addAllErrors(RosettaInterpreterValue other) {
		if (!(other instanceof RosettaInterpreterErrorValue)) {
			return false;
		}
		return errors.addAll(((RosettaInterpreterErrorValue)other).getErrors());
	}

	public boolean addAllErrors(List<RosettaInterpreterBaseError> errors) {
		return this.errors.addAll(errors);
	}
	
	/**
	 * Checks if there is at least one error within the supplied values.
	 *
	 * @param val1 - first value to check
	 * @param val2 - second value to check
	 * @return true iff at least one of the values is an error value
	 */
	public static boolean errorsExist(RosettaInterpreterValue val1,
			RosettaInterpreterValue val2) {
		return val1 instanceof RosettaInterpreterErrorValue
				|| val2 instanceof RosettaInterpreterErrorValue;
	}
	
	/**
	 * Checks if the supplied value is an error.
	 *
	 * @param val1 - value to check
	 * @return true iff value is an error value
	 */
	public static boolean errorsExist(RosettaInterpreterValue val1) {
		return val1 instanceof RosettaInterpreterErrorValue;
	}
	
	/**
	 * Checks if there is at least one error within the supplied values.
	 *
	 * @param vals - list of values to check
	 * @return true iff at least one of the values is an error
	 */
	public static boolean errorsExist(List<RosettaInterpreterValue> vals) {
		for (RosettaInterpreterValue v : vals) {
			if (v instanceof RosettaInterpreterErrorValue) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Merges all errors existing within a list into one error value.
	 *
	 * @param vals - list of values with at least one error value
	 * @return error value with all error messages
	 */
	public static RosettaInterpreterErrorValue merge(List<RosettaInterpreterValue> vals) {
		if (!errorsExist(vals)) {
			throw new IllegalArgumentException("None of the values are errors");
		}
		RosettaInterpreterErrorValue baseVal = new RosettaInterpreterErrorValue();
		
		for (RosettaInterpreterValue val : vals) {
			baseVal.addAllErrors(val);
		}
		
		return baseVal;
	}
	
	public static RosettaInterpreterErrorValue merge(RosettaInterpreterValue val) {
		return merge(List.of(val));
	}
	
	public static RosettaInterpreterErrorValue merge(RosettaInterpreterValue val1,
			RosettaInterpreterValue val2) {
		return merge(List.of(val1, val2));
	}
	
	@Override
	public String toString() {
		return "RosettaInterpreterErrorValue [errors=" + errors + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(errors);
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
		RosettaInterpreterErrorValue other = (RosettaInterpreterErrorValue) obj;
		return Objects.equals(errors, other.errors);
	}

	@Override
	public Stream<Object> toElementStream() {
		throw new RosettaInterpreterNewException("You should not be trying to take"
				+ " a stream of element of an error value");
	}

	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		throw new RosettaInterpreterNewException("You should not be trying to take"
				+ " a stream of an error value");
	}
	
}
