package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.rosetta.simple.Annotated;
import com.regnosys.rosetta.rosetta.simple.Annotation;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import org.eclipse.emf.ecore.EObject;

import java.util.List;
import java.util.Optional;

public class WarningSuppressionHelper {
    public static final String CAPITALISATION_CATEGORY = "capitalisation";

    public boolean isCapitalisationSuppressed(EObject eObject) {
        return isSuppressed(eObject, CAPITALISATION_CATEGORY);
    }

    public boolean isSuppressed(EObject eObject, String warningCategory) {
        if (!(eObject instanceof Annotated annotated)) {
            return false;
        }

        List<Annotation> supressWarnings = annotated.getAnnotations()
                .stream()
                .map(AnnotationRef::getAnnotation)
                .filter(annotation -> annotation.getName().equals("suppressWarnings"))
                .toList();

        if (supressWarnings.isEmpty()) {
            return false;
        }

        Optional<Attribute> warningCategoryAttribute = supressWarnings.stream()
                .flatMap(a -> a.getAttributes().stream())
                .filter(attribute -> attribute.getName().equals(warningCategory))
                .findFirst();

        return warningCategoryAttribute.isPresent();
    }
}
