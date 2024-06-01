package com.regnosys.rosetta.interpreternew.values;

import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterDateValue extends RosettaInterpreterBaseValue {
	
	private Integer day;
	private Integer month;
	private Integer year;

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(day, month, year);
	}
	
	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}
}
