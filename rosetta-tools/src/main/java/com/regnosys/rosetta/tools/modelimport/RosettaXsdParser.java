package com.regnosys.rosetta.tools.modelimport;

import org.xmlet.xsdparser.core.XsdParser;

// Configure the `RosettaXsdParserConfig`, instead of the default.
public class RosettaXsdParser extends XsdParser {
	public RosettaXsdParser(String filePath) {
		super(filePath, new RosettaXsdParserConfig());
	}
}
