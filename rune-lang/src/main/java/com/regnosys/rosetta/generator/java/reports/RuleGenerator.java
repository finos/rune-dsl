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

package com.regnosys.rosetta.generator.java.reports;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.generator.java.FluentRObjectJavaClassGenerator;
import com.regnosys.rosetta.generator.java.function.FunctionGenerator;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.types.RFunction;
import com.regnosys.rosetta.types.RObjectFactory;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.reports.ReportFunction;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaType;

import jakarta.inject.Inject;

public class RuleGenerator extends FluentRObjectJavaClassGenerator<RFunction, RGeneratedJavaClass<? extends RosettaFunction>> {
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private FunctionGenerator functionGenerator;

	@Override
	protected Stream<? extends RFunction> streamObjects(RosettaModel model) {
		return model.getElements().stream()
				.filter(RosettaRule.class::isInstance)
				.map(RosettaRule.class::cast)
				.map(rObjectFactory::buildRFunction);
	}

	@Override
	protected RGeneratedJavaClass<? extends RosettaFunction> createTypeRepresentation(RFunction rFunction) {
		return typeTranslator.toFunctionJavaClass(rFunction);
	}

	@Override
	protected CodeRenderer generateClass(RFunction rFunction, RGeneratedJavaClass<? extends RosettaFunction> clazz, String version, JavaClassScope scope) {
		JavaType baseInterface = JavaParameterizedType.from(
				new TypeReference<ReportFunction<?, ?>>() {},
				typeTranslator.toMetaJavaType(rFunction.getInputs().get(0)),
				typeTranslator.toMetaJavaType(rFunction.getOutput()));
		return functionGenerator.rBuildClass(rFunction, clazz, false, List.of(baseInterface), Collections.emptyMap(), true, scope);
	}
}
