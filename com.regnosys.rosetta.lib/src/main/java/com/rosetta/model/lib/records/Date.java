package com.rosetta.model.lib.records;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public interface Date extends Comparable<Date> {
	
	int getDay();

	int getMonth();

	int getYear();
	
	LocalDate toLocalDate();
	
	public static Date of(int year, int month, int dayOfMonth) {
		return new DateImpl(year,  month,  dayOfMonth);
	}
	
	public static Date of(LocalDate localDate) {
		return of(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
	}
	
	/**
     * Obtains an instance of {@code Date} from a text string such as {@code 2007-12-03}.
     * <p>
     * The string must represent a valid date and is parsed using
     * {@link java.time.format.DateTimeFormatter#ISO_LOCAL_DATE}.
     *
     * @param text  the text to parse such as "2007-12-03", not null
     * @return the parsed local date, not null
     * @throws DateTimeParseException if the text cannot be parsed
     */
	public static Date parse(String text) {
		return of(LocalDate.parse(text));
	}
	
	public static LocalDateTime of(Date date, LocalTime time) {
		return LocalDateTime.of(date.toLocalDate(), time);
	}
}
