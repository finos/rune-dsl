package com.regnosys.rosetta.interpreternew.values;

import java.time.LocalTime;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterTimeValue extends RosettaInterpreterBaseValue {

	private RosettaInterpreterNumberValue hours;
	private RosettaInterpreterNumberValue minutes;
	private RosettaInterpreterNumberValue seconds;
	
	
	public RosettaInterpreterTimeValue(RosettaInterpreterNumberValue hours, 
			RosettaInterpreterNumberValue minutes, RosettaInterpreterNumberValue seconds) {
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
	}
	

	public RosettaInterpreterNumberValue getHours() {
		return hours;
	}


	public RosettaInterpreterNumberValue getMinutes() {
		return minutes;
	}


	public RosettaInterpreterNumberValue getSeconds() {
		return seconds;
	}


	public void setHours(RosettaInterpreterNumberValue hours) {
		this.hours = hours;
	}


	public void setMinutes(RosettaInterpreterNumberValue minutes) {
		this.minutes = minutes;
	}


	public void setSeconds(RosettaInterpreterNumberValue seconds) {
		this.seconds = seconds;
	}


	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(hours, minutes, seconds);
	}
	
	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}
}
