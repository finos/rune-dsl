package com.regnosys.rosetta.generator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

public class GenerationException extends RuntimeException {
    private static final long serialVersionUID = -6542373098869340042L;
    private final URI resourceUri;
    private final EObject context;

    public GenerationException(String message, URI resourceUri, EObject context) {
        super(message);
        this.resourceUri = resourceUri;
        this.context = context;
    }

    public GenerationException(String message, URI resourceUri, EObject context, Throwable cause) {
        super(message, cause);
        this.resourceUri = resourceUri;
        this.context = context;
    }

    public EObject getContext() {
        return context;
    }

    public URI getResourceUri() {
        return resourceUri;
    }

    @Override
    public String toString() {
        return "GenerationException [resourceUri=" + resourceUri + ", context=" + context + ", getMessage()="
                + getMessage() + "]";
    }
}
