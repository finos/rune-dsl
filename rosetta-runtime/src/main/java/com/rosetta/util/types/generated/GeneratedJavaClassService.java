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

package com.rosetta.util.types.generated;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rosetta.model.lib.ModelReportId;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.model.lib.ModelTranslationId;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.reports.ReportFunction;
import com.rosetta.model.lib.reports.Tabulator;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;

public class GeneratedJavaClassService {
	public JavaClass<ReportFunction<?, ?>> toJavaReportFunction(ModelReportId id) {
		DottedPath packageName = id.getNamespace().child("reports");
		String simpleName = id.joinRegulatoryReference() + "ReportFunction";
		return new GeneratedJavaClass<>(packageName, simpleName, new TypeReference<ReportFunction<?, ?>>() {});
	}
	public JavaClass<Tabulator<?>> toJavaReportTabulator(ModelReportId id) {
		DottedPath packageName = id.getNamespace().child("reports");
		String simpleName = id.joinRegulatoryReference() + "ReportTabulator";
		return new GeneratedJavaClass<>(packageName, simpleName, new TypeReference<Tabulator<?>>() {});
	}
	
	@Deprecated
	public JavaClass<Tabulator<?>> toJavaProjectionTabulator(ModelSymbolId id) {
		DottedPath packageName = id.getNamespace().child("projections");
		String simpleName = id.getName() + "ProjectionTabulator";
		return new GeneratedJavaClass<>(packageName, simpleName, new TypeReference<Tabulator<?>>() {});
	}
	
	public JavaClass<Tabulator<?>> toJavaFunctionTabulator(ModelSymbolId id) {
		DottedPath packageName = id.getNamespace().child("tabulator");
		String simpleName = id.getName() + "Tabulator";
		return new GeneratedJavaClass<>(packageName, simpleName, new TypeReference<Tabulator<?>>() {});
	}
	
	public JavaClass<RosettaFunction> toJavaFunction(ModelSymbolId id) {
		DottedPath packageName = id.getNamespace().child("functions");
		String simpleName = id.getName();
		return new GeneratedJavaClass<>(packageName, simpleName, RosettaFunction.class);
	}
	
	public JavaClass<ReportFunction<?, ?>> toJavaRule(ModelSymbolId id) {
		DottedPath packageName = id.getNamespace().child("reports");
		String simpleName = id.getName() + "Rule";
		return new GeneratedJavaClass<>(packageName, simpleName, new TypeReference<ReportFunction<?, ?>>() {});
	}
	
	public JavaClass<RosettaFunction> toJavaTranslationFunction(ModelTranslationId translationId) {
		DottedPath packageName = translationId.getNamespace().child("translate");
		String inputNames = generateInputName(translationId, false);
		String outputName = StringUtils.capitalize(translationId.getOutputType().getName());
		String sourceName = StringUtils.capitalize(translationId.getTranslateSource().getName());
		String simpleName = String.format("Translate%sTo%sUsing%s", inputNames, outputName, sourceName);
	 	//Max Unix file length is 255 chars. A translator called `TranslatorFunc` will result in an inner default file called `TranslatorFunc$TranslatorFuncDefault`
	 	//So our max translator name can be (255 - 7 for default - 4 for ext - 1 for $)/2 = 121.5. So our char limit for function names is 121
		//TOOD: A better solution here would be to stop generating the full name for the default inner class
        if (simpleName.length() > 121) {
        	inputNames = generateInputName(translationId, true);
        	simpleName = String.format("Translate%sTo%sUsing%s", inputNames, outputName, sourceName);
        }
		return new GeneratedJavaClass<>(packageName, simpleName, RosettaFunction.class);
	}
	
	private String generateInputName(ModelTranslationId translationId, boolean compress) {
		return translationId.getInputTypes().stream()
				.map(id -> {
					if (compress) {
						return id.getName().substring(0, 2);
					} else {
						return id.getName();
					}
				})
				.map(name -> StringUtils.capitalize(name))
				.collect(Collectors.joining("And"));
	}
	
	public JavaClass<RosettaModelObject> toJavaType(ModelSymbolId id) {
		DottedPath packageName = id.getNamespace();
		String simpleName = id.getName();
		return new GeneratedJavaClass<>(packageName, simpleName, RosettaModelObject.class);
	}
	public JavaClass<RosettaModelObject> toJavaChoiceType(ModelSymbolId id) {
		return toJavaType(id);
	}
}
