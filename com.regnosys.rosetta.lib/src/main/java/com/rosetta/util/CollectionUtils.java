package com.rosetta.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CollectionUtils {

	public static <T> List<T> emptyIfNull(List<T> list) {
		return Optional.ofNullable(list)
				.orElseGet(ArrayList::new);
	}

	public static <T> List<T> copy(List<T> list) {
		return Optional.ofNullable(list)
				.map(l -> new ArrayList<>(l))
				.orElseGet(ArrayList::new);
	}
}
