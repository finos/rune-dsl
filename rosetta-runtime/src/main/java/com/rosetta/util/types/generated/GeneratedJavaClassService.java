package com.rosetta.util.types.generated;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rosetta.model.lib.ModelReportId;
import com.rosetta.model.lib.ModelSymbolId;
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
}
