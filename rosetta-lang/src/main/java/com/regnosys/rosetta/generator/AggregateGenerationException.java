package com.regnosys.rosetta.generator;

import java.util.List;

import org.eclipse.emf.common.util.URI;

public class AggregateGenerationException extends RuntimeException {
    private static final long serialVersionUID = 3129706933187670252L;
    private final URI resourceUri;
    private final List<GenerationException> generationExceptions;
    
    public AggregateGenerationException(String message, URI resourceUri, List<GenerationException> generationExceptions) {
        super(message, generationExceptions.get(0));
        this.resourceUri = resourceUri;
        this.generationExceptions = generationExceptions;
    }
    
    public URI getResourceUri() {
        return resourceUri;
    }

    public List<GenerationException> getGenerationExceptions() {
        return generationExceptions;
    }

    @Override
    public String toString() {
        return "AggregateGenerationException [resourceUri=" + resourceUri + ", generationExceptions="
                + generationExceptions + ", getMessage()=" + getMessage() + "]";
    }
}
