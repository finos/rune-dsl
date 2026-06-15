package com.regnosys.rosetta.utils;

import java.util.List;
import java.util.Optional;

import com.regnosys.rosetta.config.RuneConfiguration;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.Schema;
import com.regnosys.rosetta.rosetta.SchemaOrFormat;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.TransformAnnotation;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Resolves a function's transform annotation ({@code [ingest ...]}, {@code [projection ...]} or
 * {@code [enrich]}) to its serialization details: the optional schema id, the serialization format,
 * and the optional config file path (looked up by schema id in the Rune configuration).
 */
@Singleton
public class TransformAnnotationHelper {
	@Inject
	private RuneConfiguration config;

	public List<TransformAnnotation> getTransformAnnotations(Function function) {
		return function.getTransform();
	}

	public Optional<TransformAnnotation> getTransformAnnotation(Function function) {
		return function.getTransform().stream().findFirst();
	}

	/** The schema id, i.e. the name of the referenced {@code schema}; empty for a bare format. */
	public Optional<String> getSchemaId(TransformAnnotation annotation) {
		SchemaOrFormat ref = annotation.getRef();
		return ref instanceof Schema schema ? Optional.ofNullable(schema.getName()) : Optional.empty();
	}

	/** The resolved serialization format name (e.g. {@code XML}); empty for {@code enrich} or an unresolved ref. */
	public Optional<String> getFormat(TransformAnnotation annotation) {
		SchemaOrFormat ref = annotation.getRef();
		if (ref instanceof RosettaEnumValue formatValue) {
			return Optional.ofNullable(formatValue.getName());
		}
		if (ref instanceof Schema schema && schema.getFormat() != null) {
			return Optional.ofNullable(schema.getFormat().getName());
		}
		return Optional.empty();
	}

	public boolean hasFormat(TransformAnnotation annotation, String formatName) {
		return getFormat(annotation).map(formatName::equals).orElse(false);
	}

	/** The classpath location of the schema's config file, looked up by id in the Rune configuration. */
	public Optional<String> getConfigPath(TransformAnnotation annotation) {
		return getSchemaId(annotation)
				.flatMap(config::findSerializationConfigById)
				.map(c -> c.getConfigPath());
	}
}
