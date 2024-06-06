package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterTimeValue extends RosettaInterpreterBaseValue {

	private RosettaInterpreterNumberValue hours;
	private RosettaInterpreterNumberValue minutes;
	private RosettaInterpreterNumberValue seconds;
	
	/**
	 * Constructor for time value.
	 *
	 * @param hours		hour value
	 * @param minutes	minute value
	 * @param seconds	second value
	 */
	public RosettaInterpreterTimeValue(RosettaInterpreterNumberValue hours, RosettaInterpreterNumberValue minutes,
			RosettaInterpreterNumberValue seconds) {
		super();
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

	@Override
	public int hashCode() {
		return Objects.hash(hours, minutes, seconds);
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
		RosettaInterpreterTimeValue other = (RosettaInterpreterTimeValue) obj;
		return Objects.equals(hours, other.hours) && Objects.equals(minutes, other.minutes)
				&& Objects.equals(seconds, other.seconds);
	}

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(hours, minutes, seconds);
	}
	
	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}

	@Override
	public String toString() {
		return "RosettaInterpreterTimeValue [hours=" + hours 
				+ ", minutes=" + minutes + ", seconds=" + seconds + "]";
	}
}
