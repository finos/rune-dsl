package com.regnosys.rosetta.validation;

import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.config.RuneSchemaConfiguration;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.Schema;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import com.regnosys.rosetta.utils.RuneConfigurationHolder;
import com.regnosys.rosetta.utils.TransformAnnotationHelper;

import jakarta.inject.Inject;

public class SchemaValidator extends AbstractDeclarativeRosettaValidator {

    @Inject
    private RuneConfigurationHolder config;
    @Inject
    private TransformAnnotationHelper transformAnnotationHelper;

    /**
     * {@code [externalConfig]} only makes sense on a {@code schema}; reject it anywhere else.
     */
    @Check
    public void checkExternalConfigOnlyOnSchema(AnnotationRef annotationRef) {
        if (annotationRef.getAnnotation() == null || !"externalConfig".equals(annotationRef.getAnnotation().getName())) {
            return;
        }
        EObject container = annotationRef.eContainer();
        if (!(container instanceof Schema)) {
            error("[externalConfig] can only be applied to a schema", annotationRef, SimplePackage.Literals.ANNOTATION_REF__ANNOTATION);
        }
    }

    /**
     * Keep a schema's {@code [externalConfig]} marker and its external configuration in sync:
     * a schema that declares {@code [externalConfig]} must have a config path configured for it, and a
     * config path configured for a schema that is not marked {@code [externalConfig]} would silently go unused.
     */
    @Check
    public void checkExternalConfigMatchesConfiguration(Schema schema) {
        Optional<AnnotationRef> externalAnnotation = transformAnnotationHelper.findExternalConfigAnnotation(schema);
        Optional<RuneSchemaConfiguration> configEntry = Optional.ofNullable(schema.getName())
                .flatMap(name -> config.get().findSchemaConfig(name))
                .filter(c -> c.getConfigPath() != null);
        if (externalAnnotation.isPresent() && configEntry.isEmpty()) {
            error("Schema '" + schema.getName() + "' is marked [externalConfig] but no external serialization configuration is configured for it",
                    externalAnnotation.get(), SimplePackage.Literals.ANNOTATION_REF__ANNOTATION);
        } else if (externalAnnotation.isEmpty() && configEntry.isPresent()) {
            warning("An external serialization configuration is configured for schema '" + schema.getName()
                            + "', but the schema is not marked [externalConfig], so it will be ignored",
                    schema, RosettaPackage.Literals.ROSETTA_NAMED__NAME);
        }
    }
}
