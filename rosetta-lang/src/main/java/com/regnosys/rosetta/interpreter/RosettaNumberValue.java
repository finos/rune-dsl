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

package com.regnosys.rosetta.interpreter;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.rosetta.model.lib.RosettaNumber;

public class RosettaNumberValue extends RosettaValueWithNaturalOrder<RosettaNumber> {
	private final RosettaNumber scale;
	
	public RosettaNumberValue(List<RosettaNumber> items, RosettaNumber scale) {
		super(items, RosettaNumber.class);
		Validate.isTrue(scale.compareTo(RosettaNumber.ZERO) > 0);
		this.scale = scale;
	}
	public RosettaNumberValue(List<RosettaNumber> items) {
		this(items, RosettaNumber.ONE);
	}
	
	public static RosettaNumberValue of(RosettaNumber... items) {
		return withScale(RosettaNumber.ONE, items);
	}
	public static RosettaNumberValue withScale(RosettaNumber scale, RosettaNumber... items) {
		return new RosettaNumberValue(List.of(items), scale);
	}
	
	@Override
	public String toString() {
		if (scale.equals(RosettaNumber.ONE)) {
			return super.toString();
		}
		return "(scaled x" + scale + ")[" 
				+ getItems().stream().map(i -> i.multiply(scale).toString()).collect(Collectors.joining(", "))
				+ "]";
	}
}
