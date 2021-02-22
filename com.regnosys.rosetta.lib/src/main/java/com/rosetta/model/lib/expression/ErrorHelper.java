package com.rosetta.model.lib.expression;

import java.util.stream.Collectors;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.mapper.Mapper;
import com.rosetta.model.lib.mapper.MapperGroupBy;

public class ErrorHelper {

	static String formatEqualsComparisonResultError(Mapper<?> o) {
		return o.resultCount() > 0 ? String.format("%s %s", o.getPaths(), formatMultiError(o)) : o.getErrorPaths().toString();
	}

	static String formatMultiError(Mapper<?> o) {
		Object t = o.getMulti().stream().findAny().orElse(null);
		return t instanceof RosettaModelObject  ? 
				t.getClass().getSimpleName() : // for rosettaModelObjects only log class name otherwise error messages are way too long
				o.getMulti().toString();
	}
	
	
	static <A, B, C, D> String formatGroupByMismatchError(MapperGroupBy<A, B> o1, MapperGroupBy<C, D> o2) {
		return String.format("Group mismatched: %s cannot be compared to %s", formatGroupByPathAndValues(o1), formatGroupByPathAndValues(o2));
	}
	
	private static <A, B> String formatGroupByPathAndValues(MapperGroupBy<A, B> o) {
		return o.getGroups().keySet().stream()
				.map(ErrorHelper::formatEqualsComparisonResultError)
				.sorted((p1, p2) -> p1.compareTo(p2))
				.collect(Collectors.joining(","));
	}
	
}
