package com.rosetta.model.lib.records;

import java.time.LocalDate;

public class DateImpl implements Date {
	private final int day;
	private final int month;
	private final int year;
	
	public DateImpl(int day, int month, int year) {
		this.day = day;
		this.month = month;
		this.year = year;
	}
	
	public DateImpl(LocalDate date) {
		this.day = date.getDayOfMonth();
		this.month = date.getMonthValue();
		this.year = date.getYear();
	}

	@Override
	public int getDay() {
		return day;
	}

	@Override
	public int getMonth() {
		return month;
	}

	@Override
	public int getYear() {
		return year;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + day;
		result = prime * result + month;
		result = prime * result + year;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DateImpl other = (DateImpl) obj;
		if (day != other.day)
			return false;
		if (month != other.month)
			return false;
		if (year != other.year)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DateImpl [day=" + day + ", month=" + month + ", year=" + year + "]";
	}

}
