package com.regnosys.rosetta.interpreter;

import java.util.List;
import java.util.regex.Pattern;

public class RosettaPatternValue extends RosettaAbstractValue<Pattern> {
	public RosettaPatternValue(List<Pattern> items) {
		super(items);
	}
	
	public static RosettaPatternValue of(Pattern... items) {
		return new RosettaPatternValue(List.of(items));
	}
}
