package com.regnosys.rosetta.tools.modelimport;

import java.util.Properties;

public class GenerationProperties {

    private static final String NAMESPACE = "header.namespace";
    private static final String NAMESPACE_DEFINITION = "header.namespace.definition";
    private static final String SYNONYM_SOURCE_NAME = "synonymSource.name";
//
    private final String namespace;
    private final String namespaceDefinition;
    private final String synonymSourceName;

    public GenerationProperties(Properties properties) {
        this.namespace = properties.getProperty(NAMESPACE);
        this.namespaceDefinition = properties.getProperty(NAMESPACE_DEFINITION);
        this.synonymSourceName = properties.getProperty(SYNONYM_SOURCE_NAME);
    }

    public GenerationProperties(String namespace,
                                String namespaceDefinition,
                                String bodyType,
                                String bodyName,
                                String bodyDefinition,
                                String corpusType,
                                String corpusName,
                                String corpusDisplayName,
                                String corpusDefinition,
                                String segmentName,
                                String synonymSourceName) {
        this.namespace = namespace;
        this.namespaceDefinition = namespaceDefinition;
        this.synonymSourceName = synonymSourceName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getNamespaceDefinition() {
        return namespaceDefinition;
    }

    public String getSynonymSourceName() {
        return synonymSourceName;
    }
}
