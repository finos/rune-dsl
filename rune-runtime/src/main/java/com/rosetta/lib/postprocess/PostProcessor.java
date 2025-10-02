/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
