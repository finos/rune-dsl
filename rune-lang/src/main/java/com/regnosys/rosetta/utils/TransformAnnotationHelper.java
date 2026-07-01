package com.regnosys.rosetta.utils;

import java.util.Optional;

import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.Schema;
import com.regnosys.rosetta.rosetta.SchemaOrFormat;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.TransformAnnotation;
import com.rosetta.model.lib.transform.SerializationFormat;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Resolves a function's transform annotation ({@code [ingest ...]}, {@code [projection ...]} or
 * {@code [enrich]}) to its serialization details: the optional schema id, the serialization format,
 * and the optional config file path (resolved for schemas marked {@code [externalConfig]}).
 */
@Singleton
public class TransformAnnotationHelper {
	@Inject
	private RuneConfigurationHolder configuration;

	public Optional<TransformAnnotation> getTransformAnnotation(Function function) {
		return function.getTransform().stream().findFirst();
	}

	/** The schema id, i.e. the name of the referenced {@code schema}; empty for a bare format. */
	public Optional<String> getSchemaId(TransformAnnotation annotation) {
		SchemaOrFormat ref = annotation.getRef();
		return ref instanceof Schema schema ? Optional.ofNullable(schema.getName()) : Optional.empty();
	}

	/** The resolved serialization format (e.g. {@link SerializationFormat#XML}); empty for {@code enrich} or an unresolved ref. */
	public Optional<SerializationFormat> getFormat(TransformAnnotation annotation) {
		SchemaOrFormat ref = annotation.getRef();
		if (ref instanceof RosettaEnumValue formatValue) {
			return toSerializationFormat(formatValue.getName());
		}
		if (ref instanceof Schema schema && schema.getFormat() != null) {
			return toSerializationFormat(schema.getFormat().getName());
		}
		return Optional.empty();
	}

	public boolean hasFormat(TransformAnnotation annotation, SerializationFormat format) {
		return getFormat(annotation).map(format::equals).orElse(false);
	}

	private Optional<SerializationFormat> toSerializationFormat(String name) {
		if (name == null) {
			return Optional.empty();
		}
		for (SerializationFormat format : SerializationFormat.values()) {
			if (format.name().equals(name)) {
				return Optional.of(format);
			}
		}
		return Optional.empty();
	}

	/**
	 * The classpath location of the schema's config file. Only resolved for a schema explicitly marked
	 * {@code [externalConfig]}; empty otherwise (a bare format, or a schema that does not declare external
	 * configuration).
	 */
	public Optional<String> getConfigPath(TransformAnnotation annotation) {
		SchemaOrFormat ref = annotation.getRef();
		if (!(ref instanceof Schema schema) || !isExternalConfig(schema)) {
			return Optional.empty();
		}
		return Optional.ofNullable(schema.getName())
				.flatMap(name -> configuration.get().findSchemaConfig(name))
				.map(c -> c.getConfigPath());
	}

	/** Whether the schema declares the {@code [externalConfig]} annotation. */
	public boolean isExternalConfig(Schema schema) {
		return schema.getAnnotations().stream()
				.map(AnnotationRef::getAnnotation)
				.anyMatch(a -> a != null && "externalConfig".equals(a.getName()));
	}
}
