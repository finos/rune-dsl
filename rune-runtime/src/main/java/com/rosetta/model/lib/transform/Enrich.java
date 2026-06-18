package com.rosetta.model.lib.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a generated function class as performing an enrichment. Unlike ingestion and projection,
 * an enrichment does not (de)serialize, so it carries no format or configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Enrich {
}
