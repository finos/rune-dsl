package com.rosetta.util.types;

import com.rosetta.model.lib.reports.ReportModelId;
import com.rosetta.util.DottedPath;

public class GeneratedJavaClassService {	
	public JavaClass toJavaReportFunction(ReportModelId id) {
		DottedPath namespace = id.getNamespace().child("reports");
		String simpleName = id.getBody() + String.join("", id.getCorpuses()) + "ReportFunction";
		return new JavaClass(namespace, simpleName);
	}
	
	public JavaClass toJavaReportTabulator(ReportModelId id) {
		DottedPath namespace = id.getNamespace().child("reports");
		String simpleName = id.getBody() + String.join("", id.getCorpuses()) + "ReportTabulator";
		return new JavaClass(namespace, simpleName);
	}
}
