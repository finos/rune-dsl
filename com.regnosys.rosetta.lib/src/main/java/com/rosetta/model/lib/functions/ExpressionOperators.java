package com.rosetta.model.lib.functions;

public class ExpressionOperators {
	
	public static boolean exists(final Object o) {
		return (o != null);
	}

	public static boolean notExists(final Object o) {
		return (o == null);
	}
}
