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
	
	/**
	 * Method to check if the time value is a valid time.
	 *
	 * @return	true if it's valid, false otherwise
	 */
	public boolean valid() {
		if (hours.getValue().intValue() > 24 || hours.getValue().intValue() < 0) {
			return false;
		} else if (minutes.getValue().intValue() > 59 || minutes.getValue().intValue() < 0) {
			return false;
		} else if (seconds.getValue().intValue() > 59 || seconds.getValue().intValue() < 0) {
			return false;
		}
		return true;
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
