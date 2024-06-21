package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;
import java.util.stream.Stream;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.rosetta.simple.impl.FunctionImpl;

public class RosettaInterpreterFunctionValue extends RosettaInterpreterBaseValue {
	
	private FunctionImpl function;
	
	
	/**
	 * Constructor for the function Value wrapper.
	 *
	 * @param f the function implementation to be interpreted
	 */
	public RosettaInterpreterFunctionValue(FunctionImpl f) {
		super();
		function = f;
	}
	
	public FunctionImpl getFunction() {
		return function;
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
		RosettaInterpreterFunctionValue other = (RosettaInterpreterFunctionValue) obj;
		return Objects.equals(function, other.function);
	}
	
	@Override
	public String toString() {
		return "RosettaInterpreterFunctionValue [function=" + function + "]";
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(function);
	}
	

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(function);
	}

	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}
	
	
	
	
}