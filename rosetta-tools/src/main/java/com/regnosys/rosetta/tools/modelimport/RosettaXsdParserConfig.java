package com.regnosys.rosetta.tools.modelimport;

import java.util.LinkedHashMap;
import java.util.Map;

import org.xmlet.xsdparser.core.utils.ConfigEntryData;
import org.xmlet.xsdparser.core.utils.DefaultParserConfig;
import org.xmlet.xsdparser.xsdelements.XsdSchema;
import org.xmlet.xsdparser.xsdelements.visitors.XsdSchemaVisitor;

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

    // A hack for issue https://github.com/xmlet/XsdParser/issues/74
    @Override
    public Map<String, ConfigEntryData> getParseMappers() {
        Map<String, ConfigEntryData> parseMappers = super.getParseMappers();

        parseMappers.put(XsdSchema.TAG, new ConfigEntryData(d -> {
            d.parserInstance.parseElements = new LinkedHashMap<>(d.parserInstance.parseElements);
            parseMappers.put(XsdSchema.TAG, new ConfigEntryData(XsdSchema::parse, elem -> new XsdSchemaVisitor((XsdSchema) elem)));
            return XsdSchema.parse(d);
        }, elem -> new XsdSchemaVisitor((XsdSchema) elem)));

        return parseMappers;
    }
}
