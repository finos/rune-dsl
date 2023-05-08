package com.regnosys.rosetta.types.builtin;

import java.util.LinkedHashMap;

import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.types.RParametrizedType;

public class RBasicType extends RParametrizedType {
	private final String name;
	private final boolean hasNaturalOrder;
	
	protected RBasicType(String name, LinkedHashMap<String, RosettaValue> parameters, boolean hasNaturalOrder) {
		super(parameters);
		this.name = name;
		this.hasNaturalOrder = hasNaturalOrder;
	}
	public RBasicType(String name, boolean hasNaturalOrder) {
		this(name, new LinkedHashMap<>(), hasNaturalOrder);
	}

	@Override
	public String getName() {
		return name;
	}
	@Override
	public boolean hasNaturalOrder() {
		return hasNaturalOrder;
	}
}
