package com.regnosys.rosetta.tools.modelimport;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.core.XsdParserCore;
import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdSchema;
import org.xmlet.xsdparser.xsdelements.elementswrapper.UnsolvedReference;
import org.xmlet.xsdparser.xsdelements.exceptions.ParentAvailableException;

// A hack to solve issue https://github.com/xmlet/XsdParser/issues/72.
// It also uses the `RosettaXsdParserConfig`, instead of the default.
public class RosettaXsdParser extends XsdParser {
	
	private Field f;

	public RosettaXsdParser(String filePath) {
		super(filePath, new RosettaXsdParserConfig());
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, List<UnsolvedReference>> getUnsolvedElements() {
		try {
			if (f == null) {
				f = XsdParserCore.class.getDeclaredField("unsolvedElements");
				f.setAccessible(true);
			}
			return (Map<String, List<UnsolvedReference>>) f.get(this);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addUnsolvedReference(UnsolvedReference unsolvedReference) {
        XsdSchema schema;

        try {
            schema = XsdAbstractElement.getXsdSchema(unsolvedReference.getElement(), new ArrayList<>());
        } catch (ParentAvailableException e) {
            schema = null;
        }

        String localCurrentFile = currentFile;

        if (schema != null) {
            String schemaFilePath = schema.getFilePath().replace("\\", "/"); // FIX

            if (!localCurrentFile.equals(schemaFilePath)) {
                localCurrentFile = schemaFilePath;
            }
        }

        List<UnsolvedReference> unsolved = getUnsolvedElements().computeIfAbsent(localCurrentFile, k -> new ArrayList<>());

        unsolved.add(unsolvedReference);
    }
}
