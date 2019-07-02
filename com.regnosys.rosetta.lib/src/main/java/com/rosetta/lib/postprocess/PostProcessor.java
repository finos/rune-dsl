package com.rosetta.lib.postprocess;

import com.rosetta.model.lib.RosettaModelObject;

import java.util.Optional;

/**
 * A function that takes a {@link RosettaModelObject}, operates on that object and returns a new instance of that object.
 */
public interface PostProcessor {

	<R extends RosettaModelObject> R process(Class<R> rosettaType, R instance);

	<R extends RosettaModelObject> Optional<PostProcessorReport> getReport();

    static PostProcessor identity() {
        return new PostProcessor() {
            @Override
            public <R extends RosettaModelObject> Optional<PostProcessorReport> getReport() {
                return Optional.empty();
            }

			@Override
			public <R extends RosettaModelObject> R process(Class<R> rosettaType, R instance) {
				return null;
			}
        };
    }

}
