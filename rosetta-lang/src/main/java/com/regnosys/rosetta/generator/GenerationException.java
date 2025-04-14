package com.regnosys.rosetta.generator;

import org.eclipse.emf.ecore.EObject;

public class GenerationException extends RuntimeException {
    private static final long serialVersionUID = -6542373098869340042L;
    private final EObject context;

    public GenerationException(String message, EObject context) {
        super(message);
        this.context = context;
    }

    public GenerationException(String message, EObject context, Throwable cause) {
        super(message, cause);
        this.context = context;
    }

    public EObject getContext() {
        return context;
    }

    @Override
    public String toString() {
        return super.toString() + " [on EObject: " + context + "]";
    }

}
