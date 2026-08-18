package com.regnosys.rosetta.validation;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.rosetta.RosettaTypeAlias;
import com.regnosys.rosetta.rosetta.simple.Annotation;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;

public class TypeAliasValidator extends AbstractDeclarativeRosettaValidator {
    private static final String SUPPRESS_UNUSED = "suppressUnused";
    private static final String DEPRECATED = "deprecated";
    private static final Set<String> ALLOWED = Set.of(SUPPRESS_UNUSED, DEPRECATED);

    /**
     * A type alias accepts annotations only so that {@code [suppressUnused]} and {@code [deprecated]} can be
     * written on it. The grammar fragment that allows it allows any annotation, and nothing reads the others, so
     * reject them here rather than accept and ignore them.
     */
    @Check
    public void checkOnlyAllowedAnnotations(RosettaTypeAlias ele) {
        Set<String> seen = new HashSet<>();
        for (AnnotationRef ref : ele.getAnnotations()) {
            Annotation annotation = ref.getAnnotation();
            if (annotation == null || annotation.eIsProxy()) {
                continue;
            }
            String name = annotation.getName();
            if (!ALLOWED.contains(name)) {
                error("[" + name + "] is not allowed on a type alias. The only annotations a type alias "
                        + "supports are [" + SUPPRESS_UNUSED + "] and [" + DEPRECATED + "].",
                        ref, SimplePackage.Literals.ANNOTATION_REF__ANNOTATION);
            } else if (!seen.add(name)) {
                error("Only 1 [" + name + "] annotation allowed.",
                        ref, SimplePackage.Literals.ANNOTATION_REF__ANNOTATION);
            }
        }
    }
}
