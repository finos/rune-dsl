package com.regnosys.rosetta.tools.modelimport;

import java.util.Properties;

public class GenerationProperties {

    private static final String NAMESPACE = "header.namespace";
    private static final String NAMESPACE_DEFINITION = "header.namespace.definition";
    private static final String BODY_TYPE = "body.type";
    private static final String BODY_NAME = "body.name";
    private static final String BODY_DEFINITION = "body.definition";
    private static final String CORPUS_TYPE = "corpus.type";
    private static final String CORPUS_NAME = "corpus.name";
    private static final String CORPUS_DISPLAY_NAME = "corpus.displayName";
    private static final String CORPUS_DEFINITION = "corpus.definition";
    private static final String SEGMENT_NAME = "segment.name";
    private static final String SYNONYM_SOURCE_NAME = "synonymSource.name";

    private final String namespace;
    private final String namespaceDefinition;
    private final String bodyType;
    private final String bodyName;
    private final String bodyDefinition;
    private final String corpusType;
    private final String corpusName;
    private final String corpusDisplayName;
    private final String corpusDefinition;
    private final String segmentName;
    private final String synonymSourceName;

    public GenerationProperties(Properties properties) {
        this.namespace = properties.getProperty(NAMESPACE);
        this.namespaceDefinition = properties.getProperty(NAMESPACE_DEFINITION);
        this.bodyType = properties.getProperty(BODY_TYPE);
        this.bodyName = properties.getProperty(BODY_NAME);
        this.bodyDefinition = properties.getProperty(BODY_DEFINITION);
        this.corpusType = properties.getProperty(CORPUS_TYPE);
        this.corpusName = properties.getProperty(CORPUS_NAME);
        this.corpusDisplayName = properties.getProperty(CORPUS_DISPLAY_NAME);
        this.corpusDefinition = properties.getProperty(CORPUS_DEFINITION);
        this.segmentName = properties.getProperty(SEGMENT_NAME);
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
        this.bodyType = bodyType;
        this.bodyName = bodyName;
        this.bodyDefinition = bodyDefinition;
        this.corpusType = corpusType;
        this.corpusName = corpusName;
        this.corpusDisplayName = corpusDisplayName;
        this.corpusDefinition = corpusDefinition;
        this.segmentName = segmentName;
        this.synonymSourceName = synonymSourceName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getNamespaceDefinition() {
        return namespaceDefinition;
    }

    public String getBodyType() {
        return bodyType;
    }

    public String getBodyName() {
        return bodyName;
    }

    public String getBodyDefinition() {
        return bodyDefinition;
    }

    public String getCorpusType() {
        return corpusType;
    }

    public String getCorpusName() {
        return corpusName;
    }

    public String getCorpusDisplayName() {
        return corpusDisplayName;
    }

    public String getCorpusDefinition() {
        return corpusDefinition;
    }

    public String getSegmentName() {
        return segmentName;
    }

    public String getSynonymSourceName() {
        return synonymSourceName;
    }
}
