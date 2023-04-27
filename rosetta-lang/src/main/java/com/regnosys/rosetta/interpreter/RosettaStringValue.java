package com.regnosys.rosetta.interpreter;

import java.util.List;
import java.util.stream.Collectors;

public class RosettaStringValue extends RosettaValueWithNaturalOrder<String> {
	public RosettaStringValue(List<String> items) {
		super(items, String.class);
	}
	
	public static RosettaStringValue of(String... items) {
		return new RosettaStringValue(List.of(items));
	}

	@Override
	public String toString() {
		return "[" 
				+ getItems().stream().map(i -> '"' + i + '"').collect(Collectors.joining(", "))
				+ "]";
	}
}
