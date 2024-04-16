/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.tools.modelimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.xsdelements.XsdSchema;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.rosetta.util.serialisation.RosettaXMLConfiguration;

public class XsdImportMain {

    public static void main(String[] args) throws IOException, ParseException {
        Options options = new Options()
                .addOption("x", "xsd-path", true, "Path to the xsd")
                .addOption("p", "properties-path", true, "Path to generation properties file")
                .addOption("ros", "rosetta-output-path", true, "Path to generation output folder")
                .addOption("xml", "xml-config-output-path", true, "Path to output file for the XML configuration");

        // Parse command line
        CommandLine cmd = new DefaultParser().parse(options, args);
        String xsdPath = cmd.getOptionValue("xsd-path");
        String propertiesPath = cmd.getOptionValue("properties-path");
        String rosettaOutputPath = cmd.getOptionValue("rosetta-output-path");
        String xmlConfigOutputPath = cmd.getOptionValue("xml-config-output-path");

        System.out.println(String.format("xsdPath %s", xsdPath));
        System.out.println(String.format("propertiesPath %s", propertiesPath));
        System.out.println(String.format("rosettaOutputPath %s", rosettaOutputPath));
        System.out.println(String.format("xmlConfigOutputPath %s", xmlConfigOutputPath));

        // Parse rosetta generation properties file
        GenerationProperties properties = getGenerationProperties(propertiesPath);

        // Parse xsd
        XsdParser parserInstance = new XsdParser(xsdPath);
        XsdSchema schema = parserInstance.getResultXsdSchemas().findAny().orElseThrow();

        // Generate rosetta
        Injector injector = new RosettaStandaloneSetup().createInjectorAndDoEMFRegistration();
        XsdImport xsdImport = injector.getInstance(XsdImport.class);
        xsdImport.generateRosetta(schema, properties);
        RosettaXMLConfiguration xmlConfig = xsdImport.generateXMLConfiguration(schema, properties);
        File xmlConfigOutputFile = new File(xmlConfigOutputPath);
        xmlConfigOutputFile.getParentFile().mkdirs();
        getObjectMapper().writerWithDefaultPrettyPrinter().writeValue(xmlConfigOutputFile, xmlConfig);
        xsdImport.saveResources(rosettaOutputPath);
    }
    
    public static ObjectMapper getObjectMapper() {
    	return new ObjectMapper()
    			.registerModule(new Jdk8Module())
    			.setSerializationInclusion(Include.NON_ABSENT);
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
