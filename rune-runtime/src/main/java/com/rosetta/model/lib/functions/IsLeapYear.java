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

package com.rosetta.model.lib.functions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.chrono.IsoChronology;

public class IsLeapYear {
	public Boolean execute(Integer year) {
		if (year == null) {
			return null;
		}
		return isLeapYear(year.longValue());
	}
	
	public Boolean execute(Long year) {
		if (year == null) {
			return null;
		}
		return isLeapYear(year.longValue());
	}
	
	public Boolean execute(BigInteger year) {
		if (year == null) {
			return null;
		}
		return isLeapYear(year.longValue());
	}

	public Boolean execute(BigDecimal year) {
		if (year == null) {
			return null;
		}
		return isLeapYear(year.longValue());
	}
	
	private boolean isLeapYear(long year) {
		return IsoChronology.INSTANCE.isLeapYear(year);
	}
}
