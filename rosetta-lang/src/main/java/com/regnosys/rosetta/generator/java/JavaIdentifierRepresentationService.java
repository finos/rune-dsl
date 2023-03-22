package com.regnosys.rosetta.generator.java;

import com.regnosys.rosetta.generator.IdentifierRepresentationService;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.types.RType;

public class JavaIdentifierRepresentationService extends IdentifierRepresentationService {
	public BlueprintImplicitVariableRepresentation toBlueprintImplicitVar(RType type) {
		return new BlueprintImplicitVariableRepresentation(type);
	}
	
	public FunctionInstanceRepresentation toFunctionInstance(Function func) {
		return new FunctionInstanceRepresentation(func);
	}
}
