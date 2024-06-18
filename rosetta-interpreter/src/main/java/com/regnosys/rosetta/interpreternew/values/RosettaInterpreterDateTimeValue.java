package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterDateTimeValue extends RosettaInterpreterBaseValue {

	private RosettaInterpreterDateValue date;
	private RosettaInterpreterTimeValue time;
	
	/**
	 * Constructor for dateTime value.
	 *
	 * @param date		date value
	 * @param time		time value
	 */
	public RosettaInterpreterDateTimeValue(RosettaInterpreterDateValue date, RosettaInterpreterTimeValue time) {
		super();
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
	public int hashCode() {
		return Objects.hash(date, time);
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
		RosettaInterpreterDateTimeValue other = (RosettaInterpreterDateTimeValue) obj;
		return Objects.equals(date, other.date) && Objects.equals(time, other.time);
	}

	@Override
	public String toString() {
		return "RosettaInterpreterDateTimeValue [date=" + date + ", time=" + time + "]";
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
