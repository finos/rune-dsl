package com.rosetta.model.lib.functions;

import java.util.function.Function;

public class NamedFunctionImpl<I,O> implements NamedFunction<I, O> {

	private final String name;
	private final Function<I,O> function;	
	
	public NamedFunctionImpl(String name, Function<I, O> function) {
		super();
		this.name = name;
		this.function = function;
	}

	@Override
	public O apply(I t) {
		return function.apply(t);
	}

	@Override
	public String getName() {
		return name;
	}

}
