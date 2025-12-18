/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.generator.java.scoping;

import com.regnosys.rosetta.generator.IdentifierRepresentationService;
import com.regnosys.rosetta.generator.ImplicitVariableRepresentation;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.types.RFunction;
import com.rosetta.util.types.JavaClass;

public class JavaIdentifierRepresentationService extends IdentifierRepresentationService {	
	public DependencyInstanceRepresentation toDependencyInstance(JavaClass<?> dependency) {
		return new DependencyInstanceRepresentation(dependency);
	}

	public ImplicitVariableRepresentation toRuleInputParameter(RosettaRule rule) {
		return getImplicitVarInContext(rule.getExpression());
	}
	
	public RuleOutputParameterRepresentation toRuleOutputParameter(RosettaRule rule) {
		return new RuleOutputParameterRepresentation(rule);
	}
	
	public SuperFunctionRepresentation toSuperFunctionInstance(RFunction superFunc) {
		return new SuperFunctionRepresentation(superFunc);
	}
}
