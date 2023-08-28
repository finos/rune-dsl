package com.regnosys.rosetta.types.builtin;

import java.util.LinkedHashMap;

import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.scoping.RosettaScopeProvider;
import com.regnosys.rosetta.types.RParametrizedType;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.DottedPath;

public class RBasicType extends RParametrizedType {
	private final ModelSymbolId symbolId;
	private final boolean hasNaturalOrder;
	
	protected RBasicType(String name, LinkedHashMap<String, RosettaValue> parameters, boolean hasNaturalOrder) {
		super(parameters);
		this.symbolId = new ModelSymbolId(DottedPath.splitOnDots(RosettaScopeProvider.LIB_NAMESPACE), name);
		this.hasNaturalOrder = hasNaturalOrder;
	}
	public RBasicType(String name, boolean hasNaturalOrder) {
		this(name, new LinkedHashMap<>(), hasNaturalOrder);
	}

	@Override
	public ModelSymbolId getSymbolId() {
		return symbolId;
	}
	
	@Override
	public boolean hasNaturalOrder() {
		return hasNaturalOrder;
	}
}
