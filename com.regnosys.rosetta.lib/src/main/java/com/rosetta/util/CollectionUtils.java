package com.rosetta.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class CollectionUtils {

	public static <T> T nextOrNull(Iterator<T> iterator) {
		return iterator.hasNext() ? iterator.next() : null;
	}

	public static <T> T nextOrGet(Iterator<T> iterator, Supplier<T> supplier) {
		return iterator.hasNext() ? iterator.next() : supplier.get();
	}

	public static <T> List<T> emptyIfNull(List<T> list) {
		return Optional.ofNullable(list).orElseGet(() -> new ArrayList<>());
	}

	public static <T> List<T> copy(List<T> list) {
		return Optional.ofNullable(list)
				.map(l -> new ArrayList<>(l))
				.orElseGet(() -> new ArrayList<>());
	}
}
