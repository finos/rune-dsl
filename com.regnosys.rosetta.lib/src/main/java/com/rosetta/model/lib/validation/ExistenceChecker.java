package com.rosetta.model.lib.validation;

import java.util.List;
import java.util.Objects;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

public class ExistenceChecker {
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
	
	// @Compat. Older models are compiled against these method overloads.
	public static boolean isSet(RosettaModelObjectBuilder field) {
		return isSet((Object)field);
	}
	
	// @Compat. Older models are compiled against these method overloads.
	public static boolean isSet(List<? extends Object> field) {
		return isSet((Object)field);
	}
}
