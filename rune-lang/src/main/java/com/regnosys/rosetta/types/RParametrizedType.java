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
import java.util.Objects;
import java.util.stream.Collectors;

import com.regnosys.rosetta.interpreter.RosettaValue;

public abstract class RParametrizedType extends RType {
	private final LinkedHashMap<String, RosettaValue> arguments;
	
	public RParametrizedType(LinkedHashMap<String, RosettaValue> arguments) {
		this.arguments = arguments;
	}

	public Map<String, RosettaValue> getArguments() {
		return arguments;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getName(), arguments);
	}
	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
        RParametrizedType other = (RParametrizedType) object;
		return Objects.equals(this.getName(), other.getName())
				&& Objects.equals(arguments, other.arguments);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getName());
		String joinedArguments = arguments.entrySet().stream()
				.filter(e -> e.getValue().getSingle().isPresent())
				.map(e -> e.getKey() + ": " + e.getValue().getSingle().get())
				.collect(Collectors.joining(", "));
		if (joinedArguments.length() > 0) {
			builder.append("(")
				.append(joinedArguments)
				.append(")");
		}
		return builder.toString();
	}
}
