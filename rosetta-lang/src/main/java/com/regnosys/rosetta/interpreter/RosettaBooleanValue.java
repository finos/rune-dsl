package com.regnosys.rosetta.interpreter;

import java.util.List;

import com.google.common.primitives.Booleans;

public class RosettaBooleanValue extends RosettaValueWithNaturalOrder<Boolean> {
	public RosettaBooleanValue(List<Boolean> items) {
		super(items, Boolean.class);
	}
	
	public static RosettaBooleanValue of(boolean... items) {
		return new RosettaBooleanValue(Booleans.asList(items));
	}
}
