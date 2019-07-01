package com.rosetta.lib.postprocess;

import com.rosetta.model.lib.RosettaModelObjectBuilder;

/**
 * A marker interface to identify output of {@link PostProcessor}s. {@link PostProcessorReport}s can contain
 * arbitrary data.
 */
public interface PostProcessorReport {
	
	RosettaModelObjectBuilder getResultObject();
	
}
