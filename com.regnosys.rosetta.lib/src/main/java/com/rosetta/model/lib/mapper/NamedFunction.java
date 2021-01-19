package com.rosetta.model.lib.mapper;

import java.util.function.Function;

public interface NamedFunction<I,O> extends Function<I,O>{
	String getName();
}
