package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterDateValue extends RosettaInterpreterBaseValue {
	
	private RosettaInterpreterIntegerValue day;
	private RosettaInterpreterIntegerValue month;
	private RosettaInterpreterIntegerValue year;

	/**
	 * Constructor for date value.
	 *
	 * @param day 		day value
	 * @param month		month value
	 * @param year		year value
	 */
	public RosettaInterpreterDateValue(RosettaInterpreterIntegerValue day, RosettaInterpreterIntegerValue month,
			RosettaInterpreterIntegerValue year) {
		super();
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
	public int hashCode() {
		return Objects.hash(day, month, year);
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
		RosettaInterpreterDateValue other = (RosettaInterpreterDateValue) obj;
		return Objects.equals(day, other.day) && Objects.equals(month, other.month) 
				&& Objects.equals(year, other.year);
	}

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(day, month, year);
	}
	
	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}

	@Override
	public String toString() {
		return "RosettaInterpreterDateValue [day=" + day + ", month=" + month + ", year=" + year + "]";
	}
}
