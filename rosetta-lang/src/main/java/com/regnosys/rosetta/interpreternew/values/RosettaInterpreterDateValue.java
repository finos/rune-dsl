package com.regnosys.rosetta.interpreternew.values;

import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterDateValue extends RosettaInterpreterBaseValue {
	
	private RosettaInterpreterIntegerValue day;
	private RosettaInterpreterIntegerValue month;
	private RosettaInterpreterIntegerValue year;
	
	public RosettaInterpreterDateValue(RosettaInterpreterIntegerValue day, 
			RosettaInterpreterIntegerValue month, RosettaInterpreterIntegerValue year) {
		this.day = day;
		this.month = month;
		this.year = year;
	}

	public RosettaInterpreterIntegerValue getDay() {
		return day;
	}

	public RosettaInterpreterIntegerValue getMonth() {
		return month;
	}

	public RosettaInterpreterIntegerValue getYear() {
		return year;
	}


	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(day, month, year);
	}
	
	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}
}
