package com.regnosys.rosetta.types;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.regnosys.rosetta.interpreter.RosettaValue;
import com.rosetta.model.lib.ModelSymbol.AbstractModelSymbol;
import com.rosetta.util.DottedPath;

public abstract class RTypeFunction extends AbstractModelSymbol {
	
	public RTypeFunction(DottedPath namespace, String name) {
		super(namespace, name);
	}
	
	// TODO: limitation of Xsemantics, which doesn't support anonymous classes.
	public static RTypeFunction create(DottedPath namespace, String name, Function<Map<String, RosettaValue>, RType> evaluate, Function<RType, Optional<LinkedHashMap<String, RosettaValue>>> reverse) {
		return new RTypeFunction(namespace, name) {
			@Override
			public RType evaluate(Map<String, RosettaValue> arguments) {
				return evaluate.apply(arguments);
			}
			@Override
			public Optional<LinkedHashMap<String, RosettaValue>> reverse(RType type) {
				return reverse.apply(type);
			}
		};
	}

	public abstract RType evaluate(Map<String, RosettaValue> arguments);
	
	public Optional<LinkedHashMap<String, RosettaValue>> reverse(RType type) {
		return Optional.empty();
	}
}
