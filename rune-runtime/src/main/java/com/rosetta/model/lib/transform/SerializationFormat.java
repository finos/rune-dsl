package com.rosetta.model.lib.transform;

/**
 * The wire format used to (de)serialize the input or output of a transform function
 * (e.g. an ingestion or projection).
 * <p>
 * This is the canonical Java representation of the {@code SerializationFormat} enum declared
 * in {@code basictypes.rosetta}: the Rune code generator maps the model enum onto this type
 * rather than generating a duplicate, so there is a single shared enum across the DSL, the
 * generated annotations and the runtime.
 */
public enum SerializationFormat {
    /** JSON, following a legacy JSON representation of the model. */
    JSON,
    /** JSON, following the updated flattened JSON representation of the model. */
    RUNE_JSON,
    /** XML, typically configured by an associated XML configuration file that maps the schema onto the Rune type. */
    XML,
    /** Comma-separated values. Only valid for tabular types, i.e. types whose attributes are all single-cardinality basic types. */
    CSV,
    /** Comma-separated values with a header row of human-readable labels. */
    CSV_LABELLED
}
