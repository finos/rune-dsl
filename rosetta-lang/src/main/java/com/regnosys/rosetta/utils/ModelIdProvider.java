package com.regnosys.rosetta.utils;

import java.util.stream.Collectors;

import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaNamespace;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.translate.TranslateSource;
import com.regnosys.rosetta.rosetta.translate.Translation;
import com.rosetta.model.lib.ModelReportId;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.model.lib.ModelTranslationId;
import com.rosetta.util.DottedPath;

public class ModelIdProvider {
	public DottedPath toDottedPath(RosettaNamespace namespace) {
		DottedPath segment = DottedPath.splitOnDots(namespace.getName());
		if (namespace instanceof RosettaRootElement) {
			RosettaRootElement elem = (RosettaRootElement)namespace;
			return toDottedPath(elem.getNamespace()).concat(segment);
		}
		return segment;
	}
	
	public <T extends RosettaRootElement & RosettaNamed> ModelSymbolId getSymbolId(T namedRootElement) {
		DottedPath namespace = toDottedPath(namedRootElement.getNamespace());
		String name = namedRootElement.getName();
		return new ModelSymbolId(namespace, name);
	}
	
	public ModelReportId getReportId(RosettaReport report) {
		DottedPath namespace = toDottedPath(report.getNamespace());
		String body = report.getRegulatoryBody().getBody().getName();
		String[] corpusList = report.getRegulatoryBody().getCorpusList().stream().map(c -> c.getName()).toArray(String[]::new);
		return new ModelReportId(namespace, body, corpusList);
	}
	
	public ModelTranslationId getTranslationId(Translation translation) {
		TranslateSource source = translation.getSource();
		TypeCall resultType = translation.getResultType();
		if (resultType == null) {
			resultType = ((RosettaConstructorExpression)translation.getExpression()).getTypeCall();
		}
		return new ModelTranslationId(
				getSymbolId(source),
				translation.getParameters().stream().map(p -> getSymbolId(p.getTypeCall().getType())).collect(Collectors.toList()),
				getSymbolId(resultType.getType())
			);
	}
}
