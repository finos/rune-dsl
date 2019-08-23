package com.rosetta.model.lib.records;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface Date extends Comparable<Date>{
	public int getDay();

	public int getMonth();

	public int getYear();
	
	public LocalDate toLocalDate();
	
	public static LocalDateTime of(Date date, LocalTime time) {
		return LocalDateTime.of(date.toLocalDate(), time);
	}
}
