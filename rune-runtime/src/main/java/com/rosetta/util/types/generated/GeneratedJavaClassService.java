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

import com.fasterxml.jackson.core.type.TypeReference;
import com.rosetta.model.lib.ModelReportId;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.model.lib.reports.ReportFunction;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;

public class GeneratedJavaClassService {
	public JavaClass<ReportFunction<?, ?>> toJavaReportFunction(ModelReportId id) {
		DottedPath packageName = id.getNamespace().child("reports");
		String simpleName = id.joinRegulatoryReference() + "ReportFunction";
		return new GeneratedJavaClass<>(packageName, simpleName, new TypeReference<ReportFunction<?, ?>>() {});
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
	
	public JavaClass<RosettaModelObject> toJavaType(ModelSymbolId id) {
		DottedPath packageName = id.getNamespace();
		String simpleName = id.getName();
		return new GeneratedJavaClass<>(packageName, simpleName, RosettaModelObject.class);
	}
	public JavaClass<RosettaModelObject> toJavaChoiceType(ModelSymbolId id) {
		return toJavaType(id);
	}
}
