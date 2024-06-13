package com.regnosys.rosetta.interpreternew.values;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import com.regnosys.rosetta.interpreternew.RosettaInterpreterNewException;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public abstract class RosettaInterpreterBaseValue extends MinimalEObjectImpl implements RosettaInterpreterValue {
	
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
}
