package com.regnosys.rosetta.generator.java;

import com.regnosys.rosetta.generator.IdentifierRepresentationService;
import com.regnosys.rosetta.generator.ImplicitVariableRepresentation;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RFunction;

public class JavaIdentifierRepresentationService extends IdentifierRepresentationService {	
	public FunctionInstanceRepresentation toFunctionInstance(RFunction func) {
		return new FunctionInstanceRepresentation(func);
	}

	public ImplicitVariableRepresentation toRuleInputParameter(RosettaRule rule) {
		return getImplicitVarInContext(rule.getExpression());
	}
	public RuleOutputParameterRepresentation toRuleOutputParameter(RosettaRule rule) {
		return new RuleOutputParameterRepresentation(rule);
	}
}
