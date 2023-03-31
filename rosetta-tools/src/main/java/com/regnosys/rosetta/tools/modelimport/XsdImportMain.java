package com.regnosys.rosetta.tools.modelimport;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdSchema;
import org.xmlet.xsdparser.xsdelements.elementswrapper.ReferenceBase;

import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaStandaloneSetup;

public class XsdImportMain {

    public static void main(String[] args) throws IOException, ParseException {
        Options options = new Options()
                .addOption("x", "xsd-path", true, "Path to the xsd")
                .addOption("p", "properties-path", true, "Path to generation properties file")
                .addOption("o", "output-path", true, "Path to generation output folder");

        // Parse command line
        CommandLine cmd = new DefaultParser().parse(options, args);
        String xsdPath = cmd.getOptionValue("xsd-path");
        String propertiesPath = cmd.getOptionValue("properties-path");
        String outputPath = cmd.getOptionValue("output-path");

        System.out.println(String.format("xsdPath %s", xsdPath));
        System.out.println(String.format("propertiesPath %s", propertiesPath));
        System.out.println(String.format("outputPath %s", outputPath));

        // Parse rosetta generation properties file
        GenerationProperties properties = getGenerationProperties(propertiesPath);

        // Parse xsd
        XsdParser parserInstance = new XsdParser(xsdPath);
        List<XsdAbstractElement> xsdElements = parserInstance.getResultXsdSchemas()
                .map(XsdSchema::getElements)
                .flatMap(Collection::stream)
                .map(ReferenceBase::getElement)
                .collect(Collectors.toList());

        // Generate rosetta
        Injector injector = new RosettaStandaloneSetup().createInjectorAndDoEMFRegistration();
        XsdImport xsdImport = injector.getInstance(XsdImport.class);
        xsdImport.generateRosetta(xsdElements, properties, List.of());
        xsdImport.saveResources(outputPath);
    }

    private static GenerationProperties getGenerationProperties(String propertiesPath) throws IOException {
        try (InputStream input = new FileInputStream(propertiesPath)) {
        	Properties properties = new Properties();
        	properties.load(input);
        	return new GenerationProperties(properties);
        } catch (IOException e) {
        	System.out.println("Error occurred loading generation properites");
        	throw e;
        }
    }
}
