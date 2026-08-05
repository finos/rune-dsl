package com.regnosys.rosetta.generator.java.labels;

import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.types.RDataType;

public class LabelProviderGeneratorUtil {

	public boolean shouldGenerateLabelProvider(Function function) {
		return !function.getTransform().isEmpty();
	}

	/**
	 * Whether a type-rooted label provider should be generated for {@code type}.
	 * <p>
	 * The gate is deliberately "direct labels only": a type qualifies if it has at least one
	 * {@code [label ...]} on one of its own, inherited or overridden attributes. A type that merely
	 * <i>contains</i> a labelled descendant (reachable through some nested attribute, but with no label
	 * of its own) does not qualify.
	 * <p>
	 * This is narrower than "has a reachable label", which was considered and rejected: on a CDM/DRR-scale
	 * model, almost every type transitively reaches some labelled leaf, so that reading would emit a
	 * provider - each embedding its whole reachable subgraph - for a large fraction of all types, for no
	 * current consumer. The narrow gate is sufficient for the CSV labelled-ingest story (a flat type whose
	 * own attributes carry the labels) and can be widened later if a consumer needs providers rooted at
	 * wrapper types, backed by a measurement of the generated-code growth on a realistic model.
	 */
	public boolean shouldGenerateLabelProvider(RDataType type) {
		return type.getAllAttributes().stream()
				.anyMatch(attr -> !attr.getAllLabelAnnotations().isEmpty());
	}
}
