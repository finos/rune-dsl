package com.regnosys.rosetta.resource;

import java.util.Map;
import java.util.stream.Collectors;

import com.regnosys.rosetta.rosetta.Import;
import org.eclipse.xtext.naming.QualifiedName;

import com.regnosys.rosetta.rosetta.RosettaModel;

public class RosettaModelDescription extends RosettaDescription {
    public static final String IMPORTS = "IMPORTS";

	public RosettaModelDescription(QualifiedName qualifiedName, RosettaModel model, boolean isInOverriddenNamespace) {
		super(qualifiedName, model, Map.of(IMPORTS, getImportsList(model)), isInOverriddenNamespace);
	}

	private static String getImportsList(RosettaModel model) {
		return model.getImports().stream()
				.map(Import::getImportedNamespace)
				.collect(Collectors.joining(";"));
	}
}
