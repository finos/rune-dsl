package com.regnosys.rosetta.interpreter;

public class RosettaStringItem extends RosettaValueItemWithNaturalOrder<String> {
	public RosettaStringItem(String value) {
		super(value);
	}

	@Override
	public String toString() {
		return '"' + getValue() + '"';
	}
}
