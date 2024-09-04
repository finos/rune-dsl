package com.regnosys.rosetta.utils;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.rosetta.model.lib.ModelReportId;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.DottedPath;

public class ModelIdProvider {
	public DottedPath toDottedPath(RosettaModel namespace) {
		return DottedPath.splitOnDots(namespace.getName());
	}
	
	public <T extends RosettaRootElement & RosettaNamed> ModelSymbolId getSymbolId(T namedRootElement) {
		DottedPath namespace = toDottedPath(namedRootElement.getModel());
		String name = namedRootElement.getName();
		return new ModelSymbolId(namespace, name);
	}
	
	public ModelReportId getReportId(RosettaReport report) {
		DottedPath namespace = toDottedPath(report.getModel());
		String body = report.getRegulatoryBody().getBody().getName();
		String[] corpusList = report.getRegulatoryBody().getCorpusList().stream().map(c -> c.getName()).toArray(String[]::new);
		return new ModelReportId(namespace, body, corpusList);
	}
}
