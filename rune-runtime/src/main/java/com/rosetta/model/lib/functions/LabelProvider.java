package com.rosetta.model.lib.functions;

import com.rosetta.model.lib.path.RosettaPath;

/**
 * Provides a label - a human readable name - for a given `RosettaPath`.
 * 
 * A label may be null if the provider does not know one for the given path.
 */
public interface LabelProvider {
	String getLabel(RosettaPath path);
}
