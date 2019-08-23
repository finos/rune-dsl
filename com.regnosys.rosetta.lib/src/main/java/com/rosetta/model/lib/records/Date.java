package com.rosetta.model.lib.records;

import java.time.LocalDate;

public interface Date extends Comparable<Date>{
	public int getDay();

	public int getMonth();

	public int getYear();
	
	public LocalDate toLocalDate();
}
