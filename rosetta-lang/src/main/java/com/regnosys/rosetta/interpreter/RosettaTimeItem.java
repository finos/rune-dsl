package com.regnosys.rosetta.interpreter;

import java.time.LocalTime;

public class RosettaTimeItem extends RosettaValueItemWithNaturalOrder<LocalTime> {	
	public RosettaTimeItem(LocalTime value) {
		super(value);
	}
}
