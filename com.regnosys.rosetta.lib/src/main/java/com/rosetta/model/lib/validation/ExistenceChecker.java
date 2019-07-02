package com.rosetta.model.lib.validation;

import java.util.List;
import java.util.Objects;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

public class ExistenceChecker {
	public ExistenceChecker() {
	}

	public static boolean isSet(Object field) {
		return field != null;
	}
	public static boolean isSet(RosettaModelObjectBuilder field) {
		return field != null && field.hasData();
	}

	public static boolean isSet(List<? extends Object> field) {
		return field != null && field.size() > 0 && field.stream().anyMatch(Objects::nonNull);
	}
}