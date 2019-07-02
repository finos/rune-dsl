package com.rosetta.model.lib.functions;

import java.util.function.Function;

public interface NamedFunction<I,O> extends Function<I,O>{
	String getName();
}
