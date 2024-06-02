package com.regnosys.rosetta.interpreternew.values;

import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterDateTimeValue extends RosettaInterpreterBaseValue {

	private RosettaInterpreterDateValue date;
	private RosettaInterpreterTimeValue time;
	
	public RosettaInterpreterDateTimeValue(RosettaInterpreterDateValue date, RosettaInterpreterTimeValue time) {
		this.date = date;
		this.time = time;
	}
	
	public RosettaInterpreterDateValue getDate() {
		return date;
	}

	public RosettaInterpreterTimeValue getTime() {
		return time;
	}

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(date, time);
	}
	
	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}
}
