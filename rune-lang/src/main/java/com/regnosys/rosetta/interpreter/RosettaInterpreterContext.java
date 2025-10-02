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

import com.regnosys.rosetta.rosetta.RosettaSymbol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RosettaInterpreterContext {
	private final Map<String, RosettaValue> variables;
	
	private RosettaInterpreterContext(Map<String, RosettaValue> variables) {
		this.variables = variables;
	}
	public RosettaInterpreterContext() {
		this(Collections.emptyMap());
	}
	
	public static RosettaInterpreterContext ofSymbolMap(Map<RosettaSymbol, RosettaValue> variables) {
		return new RosettaInterpreterContext(variables.entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey().getName(), Map.Entry::getValue)));
	}
	public static RosettaInterpreterContext of(Map<String, RosettaValue> variables) {
		return new RosettaInterpreterContext(new HashMap<>(variables));
	}
	
	public RosettaValue getVariableValue(RosettaSymbol symbol) {
		return getVariableValue(symbol.getName());
	}
	public RosettaValue getVariableValue(String symbol) {
		RosettaValue result = variables.get(symbol);
		if (result == null) {
			throw new RosettaInterpreterException("No value defined for variable `" + symbol + "`.");
		}
		return result;
	}

	public void setVariableValue(RosettaSymbol symbol, RosettaValue value) {
		setVariableValue(symbol.getName(), value);
	}
	public void setVariableValue(String symbol, RosettaValue value) {
		if (variables.put(symbol, value) != null) {
			throw new RosettaInterpreterException("There already is a value defined for variable `" + symbol + "`.");
		}
	}
	
	@Override
	public String toString() {
		return "{" + variables.entrySet().stream().map(e -> e.getKey() + " -> " + e.getValue()).collect(Collectors.joining(", ")) + "}";
	}
}
