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

public class DateImpl implements Date {
	
	private final int day;
	private final int month;
	private final int year;
	
	protected DateImpl(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	// TODO change to protected
	public DateImpl(LocalDate localDate) {
		this.year = localDate.getYear();
		this.month = localDate.getMonthValue();
		this.day = localDate.getDayOfMonth();
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
		return toLocalDate().toString();
	}

	@Override
	public LocalDate toLocalDate() {
		return LocalDate.of(year, month, day);
	}

	@Override
	public int compareTo(Date o) {
		return toLocalDate().compareTo(o.toLocalDate());
	}
	
	public static Date of(int year, int month, int dayOfMonth) {
		return new DateImpl(year,  month,  dayOfMonth);
	}
}
