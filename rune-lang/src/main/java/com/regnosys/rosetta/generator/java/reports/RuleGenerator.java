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
import com.regnosys.rosetta.generator.java.util.LegacyTemplateRenderer;
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
		JavaParameterizedType<ReportFunction<?, ?>> baseInterface = JavaParameterizedType.from(
				new TypeReference<ReportFunction<?, ?>>() {},
				typeTranslator.toMetaJavaType(rFunction.getInputs().get(0)),
				typeTranslator.toMetaJavaType(rFunction.getOutput()));
		// The class body is still produced as a legacy Xtend template by the (not-yet-migrated)
		// FunctionGenerator; wrap it as a CodeRenderer until that generator is migrated too.
		return LegacyTemplateRenderer.asCodeRenderer(
				functionGenerator.rBuildClass(rFunction, clazz, false, List.<JavaType>of(baseInterface), Collections.emptyMap(), true, scope));
	}
}
