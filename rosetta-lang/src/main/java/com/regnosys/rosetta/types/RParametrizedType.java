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
				.append(arguments)
				.append(")");
		}
		return builder.toString();
	}
}
