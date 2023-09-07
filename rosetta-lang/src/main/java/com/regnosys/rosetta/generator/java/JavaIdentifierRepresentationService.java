package com.regnosys.rosetta.generator.java;

import com.regnosys.rosetta.generator.IdentifierRepresentationService;
import com.regnosys.rosetta.generator.ImplicitVariableRepresentation;
import com.regnosys.rosetta.rosetta.RosettaBlueprint;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RFunction;

public class JavaIdentifierRepresentationService extends IdentifierRepresentationService {
	public BlueprintImplicitVariableRepresentation toBlueprintImplicitVar(RDataType type) {
		return new BlueprintImplicitVariableRepresentation(type);
	}
	
	public FunctionInstanceRepresentation toFunctionInstance(RFunction func) {
		return new FunctionInstanceRepresentation(func);
	}

	public ImplicitVariableRepresentation toRuleInputParameter(RosettaBlueprint rule) {
		return getImplicitVarInContext(rule.getExpression());
	}
	public RuleOutputParameterRepresentation toRuleOutputParameter(RosettaBlueprint rule) {
		return new RuleOutputParameterRepresentation(rule);
	}
}
