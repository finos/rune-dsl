package com.regnosys.rosetta.tools.modelimport;

import java.util.Map;

import org.xmlet.xsdparser.core.utils.DefaultParserConfig;

/**
 * Some built-in XSD types are missing from the default configuration. This adds them in.
 */
public class RosettaXsdParserConfig extends DefaultParserConfig {
	@Override
    public Map<String, String> getXsdTypesToJava() {
        Map<String, String> xsdTypesToJava = super.getXsdTypesToJava();

        String string = "String";

        xsdTypesToJava.put("hexBinary", string);
        xsdTypesToJava.put("base64Binary", string);

        return xsdTypesToJava;
    }
}
