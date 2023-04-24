package com.regnosys.rosetta.interpreter;

import java.time.LocalDateTime;

public class RosettaDateTimeItem extends RosettaValueItemWithNaturalOrder<LocalDateTime> {
	public RosettaDateTimeItem(LocalDateTime value) {
		super(value);
	}
}
