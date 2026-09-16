/*
 * Copyright 2026 REGnosys
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

package com.regnosys.rosetta.ide.serializer;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.serializer.ISerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.regnosys.rosetta.serializer.RosettaSerializer;
import com.regnosys.rosetta.serializer.SerializerAnalysisWarmUp;
import com.regnosys.rosetta.utils.EnvironmentUtil;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Starts a background {@link SerializerAnalysisWarmUp} for every registered language that binds the
 * Rosetta serializer, when the language server is constructed.
 *
 * <p>Off by default, because building the analysis is CPU intensive and slows down or times out
 * tests that spin up a language server. This decides only when the analysis is built, never whether
 * serializing is safe: {@link RosettaSerializer} builds it on demand either way, so leaving it off
 * costs the first serializer request a few seconds and nothing else.
 */
@Singleton
public class SerializerWarmUpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SerializerWarmUpService.class);

	/** System property or environment variable that turns the warm-up on. A latency setting. */
	public static final String WARM_UP_ENABLED_VARIABLE_NAME = "ENABLE_SERIALIZER_WARM_UP";

	private final IResourceServiceProvider.Registry registry;

	@Inject
	public SerializerWarmUpService(IResourceServiceProvider.Registry registry) {
		this.registry = registry;
	}

	/** Whether {@value #WARM_UP_ENABLED_VARIABLE_NAME} is set. Defaults to {@code false}. */
	public boolean isWarmUpEnabled() {
		return EnvironmentUtil.getBooleanOrDefault(WARM_UP_ENABLED_VARIABLE_NAME, false);
	}

	public void warmUp() {
		if (!isWarmUpEnabled()) {
			LOGGER.debug("Skipping serializer warm-up because it is disabled. "
					+ "Set {}=true to enable it.", WARM_UP_ENABLED_VARIABLE_NAME);
			return;
		}
		Set<IResourceServiceProvider> seen = new HashSet<>();
		for (IResourceServiceProvider provider : registry.getExtensionToFactoryMap().values().stream()
				.filter(IResourceServiceProvider.class::isInstance)
				.map(IResourceServiceProvider.class::cast)
				.toList()) {
			if (seen.add(provider)) {
				warmUp(provider);
			}
		}
	}

	private void warmUp(IResourceServiceProvider provider) {
		// Any other language's warm-up would build an analysis nothing here is going to serialise.
		if (provider.get(ISerializer.class) instanceof RosettaSerializer) {
			provider.get(SerializerAnalysisWarmUp.class).warmUpAsync();
		}
	}
}
