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

package com.regnosys.rosetta.types;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.regnosys.rosetta.interpreter.RosettaValue;
import com.rosetta.model.lib.ModelSymbol.AbstractModelSymbol;
import com.rosetta.util.DottedPath;

public abstract class RTypeFunction extends AbstractModelSymbol {
	
	public RTypeFunction(DottedPath namespace, String name) {
		super(namespace, name);
	}

	public abstract RType evaluate(Map<String, RosettaValue> arguments);
	
	public Optional<LinkedHashMap<String, RosettaValue>> reverse(RType type) {
		return Optional.empty();
	}
}
