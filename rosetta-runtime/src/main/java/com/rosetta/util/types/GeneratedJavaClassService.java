package com.rosetta.util.types;

import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.DottedPath;

public class GeneratedJavaClassService {	
	public JavaClass toJavaReportTabulator(ModelSymbolId id) {
		DottedPath packageName = id.getNamespace().child("reports");
		String simpleName = id.getName() + "ReportTabulator";
		return new JavaClass(packageName, simpleName);
	}
	
	public JavaClass toJavaFunction(ModelSymbolId id) {
		DottedPath packageName = id.getNamespace().child("functions");
		String simpleName = id.getName();
		return new JavaClass(packageName, simpleName);
	}
	
	public JavaClass toJavaRule(ModelSymbolId id) {
		DottedPath packageName = id.getNamespace().child("reports");
		String simpleName = id.getName() + "Rule";
		return new JavaClass(packageName, simpleName);
	}
	
	public JavaClass toJavaReportFunction(ModelSymbolId id) {
		DottedPath packageName = id.getNamespace().child("reports");
		String simpleName = id.getName() + "ReportFunction";
		return new JavaClass(packageName, simpleName);
	}
	
}
