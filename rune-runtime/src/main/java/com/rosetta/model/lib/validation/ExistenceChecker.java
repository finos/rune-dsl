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

package com.rosetta.model.lib.validation;

import java.util.List;
import java.util.Objects;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

public class ExistenceChecker {
	public static boolean isSet(Object field) {
		if (field == null) {
			return false;
		}
		if (field instanceof List) {
			@SuppressWarnings("unchecked")
			List<? extends Object> l = (List<? extends Object>)field;
			return l.size() > 0 && l.stream().anyMatch(Objects::nonNull);
		} else if (field instanceof RosettaModelObjectBuilder) {
			return ((RosettaModelObjectBuilder)field).hasData();
		}
		return true;
	}
	
	// @Compat. Older models are compiled against these method overloads.
	public static boolean isSet(RosettaModelObjectBuilder field) {
		return isSet((Object)field);
	}
	
	// @Compat. Older models are compiled against these method overloads.
	public static boolean isSet(List<? extends Object> field) {
		return isSet((Object)field);
	}
}
