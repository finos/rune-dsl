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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.generator.java.FluentRObjectJavaClassGenerator;
import com.regnosys.rosetta.generator.java.function.FunctionGenerator;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.types.RFunction;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.rosetta.model.lib.annotations.RuneLabelProvider;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.reports.ReportFunction;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaType;

import jakarta.inject.Inject;

public class ReportGenerator extends FluentRObjectJavaClassGenerator<RFunction, RGeneratedJavaClass<? extends RosettaFunction>> {
	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private FunctionGenerator functionGenerator;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private ModelIdProvider modelIdProvider;

	@Override
	protected Stream<? extends RFunction> streamObjects(RosettaModel model) {
		return model.getElements().stream()
				.filter(RosettaReport.class::isInstance)
				.map(RosettaReport.class::cast)
				.map(rObjectFactory::buildRFunction);
	}

	@Override
	protected RGeneratedJavaClass<? extends RosettaFunction> createTypeRepresentation(RFunction rFunction) {
		return typeTranslator.toFunctionJavaClass(rFunction);
	}

	@Override
	protected CodeRenderer generateClass(RFunction rFunction, RGeneratedJavaClass<? extends RosettaFunction> clazz, String version, JavaClassScope scope) {
		RosettaReport report = (RosettaReport) rFunction.getEObject();
		JavaType baseInterface = JavaParameterizedType.from(
				new TypeReference<ReportFunction<?, ?>>() {},
				typeTranslator.toMetaJavaType(rFunction.getInputs().get(0)),
				typeTranslator.toMetaJavaType(rFunction.getOutput()));

		Map<Class<?>, CodeRenderer> annotations = new LinkedHashMap<>();
		// Fully qualified: clashes with the imported com.regnosys.rosetta.rosetta.RosettaReport.
		annotations.put(com.rosetta.model.lib.annotations.RosettaReport.class, reportAnnotation(report));
		JavaClass<?> labelProviderClass = typeTranslator.toLabelProviderJavaClass(rFunction);
		annotations.put(RuneLabelProvider.class, labelProviderAnnotation(labelProviderClass));

		return functionGenerator.rBuildClass(rFunction, clazz, false, List.of(baseInterface), annotations, true, scope);
	}

	private CodeRenderer reportAnnotation(RosettaReport report) {
		return out -> {
			out.write("namespace=\"");
			out.write(modelIdProvider.toDottedPath(report.getModel()));
			out.write("\", body=\"");
			out.write(report.getRegulatoryBody().getBody().getName());
			out.write("\", corpusList={");
			out.join(report.getRegulatoryBody().getCorpusList(), ", ", corpus -> out.write("\"", corpus.getName(), "\""));
			out.write("}");
		};
	}

	private CodeRenderer labelProviderAnnotation(JavaClass<?> labelProviderClass) {
		return out -> {
			out.write("labelProvider=");
			out.write(labelProviderClass);
			out.write(".class");
		};
	}
}
