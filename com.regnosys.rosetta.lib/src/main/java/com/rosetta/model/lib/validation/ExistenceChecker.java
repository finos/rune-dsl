package com.rosetta.model.lib.validation;

import java.util.List;
import java.util.Objects;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

public class ExistenceChecker {
	public ExistenceChecker() {
	}

	public static boolean isSet(Object field) {
		if (field == null) {
			return false;
		}
		if (field instanceof List) {
			@SuppressWarnings("unchecked")
			List<? extends Object> l = (List<? extends Object>)field;
			return l.size() > 0 && l.stream().anyMatch(Objects::nonNull);
		} else if (field instanceof RosettaModelObjectBuilder) {
			return ((RosettaModelObjectBuilder)field).hasData();
		}
		return true;
	}
}
