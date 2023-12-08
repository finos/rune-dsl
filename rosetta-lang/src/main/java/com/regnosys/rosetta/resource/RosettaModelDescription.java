package com.regnosys.rosetta.resource;

import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;

import com.regnosys.rosetta.rosetta.RosettaModel;

public class RosettaModelDescription extends EObjectDescription {
	public static final String IMPORTS = "IMPORTS";

	public RosettaModelDescription(QualifiedName qualifiedName, RosettaModel model) {
		super(qualifiedName, model, Map.of(IMPORTS, getSortedImportsList(model)));
	}
	
	private static String getSortedImportsList(RosettaModel model) {
		return model.getImports().stream()
				.map(imp -> imp.getImportedNamespace())
				.collect(Collectors.joining(";"));
	}
}
