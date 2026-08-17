package com.regnosys.rosetta.validation;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.rosetta.RosettaTypeAlias;
import com.regnosys.rosetta.rosetta.simple.Annotation;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;

public class TypeAliasValidator extends AbstractDeclarativeRosettaValidator {
    private static final String SUPPRESS_UNUSED = "suppressUnused";

    /**
     * A type alias accepts annotations only so that {@code [suppressUnused]} can be written on it. The grammar
     * fragment that allows it allows any annotation, and nothing reads the others, so reject them here rather
     * than accept and ignore them.
     */
    @Check
    public void checkOnlySuppressUnusedIsAllowed(RosettaTypeAlias ele) {
        boolean alreadySuppressed = false;
        for (AnnotationRef ref : ele.getAnnotations()) {
            Annotation annotation = ref.getAnnotation();
            if (annotation == null || annotation.eIsProxy()) {
                continue;
            }
            if (!SUPPRESS_UNUSED.equals(annotation.getName())) {
                error("[" + annotation.getName() + "] is not allowed on a type alias. The only annotation a "
                        + "type alias supports is [" + SUPPRESS_UNUSED + "].",
                        ref, SimplePackage.Literals.ANNOTATION_REF__ANNOTATION);
            } else if (alreadySuppressed) {
                error("Only 1 [" + SUPPRESS_UNUSED + "] annotation allowed.",
                        ref, SimplePackage.Literals.ANNOTATION_REF__ANNOTATION);
            } else {
                alreadySuppressed = true;
            }
        }
    }
}
