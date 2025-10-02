package com.regnosys.rosetta.ide.semantictokens;

public enum RosettaSemanticTokenModifiersEnum implements ISemanticTokenModifier {
	// Predefined by the LS protocol:
	DEFAULT_LIBRARY("defaultLibrary"),
	// Custom ones
	SINGLE_CARDINALITY("singleCardinality"),
	MULTI_CARDINALITY("multiCardinality");
	
	private final String value;
	
	RosettaSemanticTokenModifiersEnum(String value) {
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return this.value;
	}
}
