package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.rosetta.simple.Annotated;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import org.eclipse.emf.ecore.EObject;

public class WarningSuppressionHelper {
    public static final String CAPITALISATION_CATEGORY = "capitalisation";
    public static final String UNUSED_CATEGORY = "unused";

    public boolean isCapitalisationSuppressed(EObject eObject) {
        return isSuppressed(eObject, CAPITALISATION_CATEGORY);
    }

    public boolean isUnusedSuppressed(EObject eObject) {
        return isSuppressed(eObject, UNUSED_CATEGORY);
    }

    public boolean isSuppressed(EObject eObject, String warningCategory) {
        if (!(eObject instanceof Annotated annotated)) {
            return false;
        }

        return annotated.getAnnotations().stream()
                .filter(ref -> ref.getAnnotation() != null
                        && "suppressWarnings".equals(ref.getAnnotation().getName()))
                .map(AnnotationRef::getAttribute)
                .filter(attribute -> attribute != null)
                .anyMatch(attribute -> warningCategory.equals(attribute.getName()));
    }
}
