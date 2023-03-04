package com.regnosys.rosetta.formatting2;

import org.eclipse.xtext.preferences.IntegerKey;

public class RosettaFormatterPreferenceKeys {
	// Preferred max line width
	public static IntegerKey maxLineWidth = new IntegerKey("line.width.max", 92);
	
	// Preferred max width of a conditional expression
	public static IntegerKey conditionalMaxLineWidth = new IntegerKey("line.width.max.conditional", 70);
}
