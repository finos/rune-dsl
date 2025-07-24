/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rosetta.model.lib.records;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

// TODO: this class can better be replaced by Java's LocalDate?
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
