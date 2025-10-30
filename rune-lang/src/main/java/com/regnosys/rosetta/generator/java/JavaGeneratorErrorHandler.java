package com.regnosys.rosetta.generator.java;

import org.eclipse.emf.ecore.EObject;

public interface JavaGeneratorErrorHandler {
    void handleError(String message);
    void handleError(String message, EObject source);
    void handleError(String message, Throwable cause);
    void handleError(String message, EObject source, Throwable cause);
    void handleError(Throwable cause);
    void handleError(EObject source, Throwable cause);
}
