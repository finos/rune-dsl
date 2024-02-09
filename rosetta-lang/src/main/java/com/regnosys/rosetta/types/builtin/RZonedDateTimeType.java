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

package com.regnosys.rosetta.types.builtin;

import java.util.Collection;
import java.util.List;

public class RZonedDateTimeType extends RRecordType {
	private final RRecordFeature dateFeature;
	private final RRecordFeature timeFeature;
	private final RRecordFeature timezoneFeature;

	public RZonedDateTimeType() {
		super("zonedDateTime");
		this.dateFeature = new RRecordFeature("date");
		this.timeFeature = new RRecordFeature("time");
		this.timezoneFeature = new RRecordFeature("timezone");
	}

	@Override
	public Collection<RRecordFeature> getFeatures() {
		return List.of(dateFeature, timeFeature, timezoneFeature);
	}
}
