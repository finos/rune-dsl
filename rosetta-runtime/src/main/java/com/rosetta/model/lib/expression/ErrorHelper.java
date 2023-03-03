package com.rosetta.model.lib.expression;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.mapper.Mapper;

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
}
