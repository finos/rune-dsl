package com.regnosys.rosetta.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Records which tool produced a namespace, e.g.
 *
 * <pre>
 * namespaceConfig:
 * - namespace: demo.unavista.csv
 *   origin:
 *     modelImport: csv
 * </pre>
 *
 * The marker is keyed on the producing tool rather than being a single flag, so that another
 * generator can record its own provenance later by adding a field here. A configuration is only ever
 * read by the version that wrote it or a newer one &mdash; a model reads its dependencies' configs,
 * never the reverse &mdash; so a reader always knows every key a config can contain, and an
 * unrecognised one is dropped like any other unknown property.
 * <p>
 * Provenance is deliberately separate from {@link RuneNamespaceConfiguration#isReadOnly() readOnly}
 * and from {@link RuneNamespaceConfiguration#getSchemaConfig() schemaConfig}. A namespace generated
 * from an authoritative schema is read-only, one inferred from samples is not, and a hand-written
 * type may carry a schema configuration without any import having taken place.
 */
public class RuneOriginConfiguration {
	private final String modelImport;

	@JsonCreator
	public RuneOriginConfiguration(@JsonProperty("modelImport") String modelImport) {
		this.modelImport = modelImport;
	}

	/**
	 * The source format model-import read to produce this namespace (e.g. {@code csv} or {@code xsd}),
	 * or {@code null} if model-import did not produce it.
	 */
	public String getModelImport() {
		return modelImport;
	}

	/**
	 * Jackson {@code valueFilter} that omits an absent origin, or one naming no tool, so a roundtrip
	 * writes no {@code origin: null} or {@code origin: {}} noise. A property-level inclusion overrides
	 * the mapper's default, so this has to cover null itself.
	 */
	public static final class EmptyFilter {
		@Override
		public boolean equals(Object other) {
			return other == null
					|| (other instanceof RuneOriginConfiguration && ((RuneOriginConfiguration) other).modelImport == null);
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}
}
