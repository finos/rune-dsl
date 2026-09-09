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
 * Triggers a non-eager, asynchronous {@link SerializerAnalysisWarmUp} for every
 * registered Xtext language that binds the Rosetta serializer.
 *
 * <p>This is invoked during construction of the language server so that the
 * cost of building the serializer analysis is paid in the background, rather
 * than during the first serializer request from a user.
 *
 * <p>The warm-up is <strong>disabled by default</strong> because building the
 * analysis is CPU intensive and slows down (or times out) unit tests that spin
 * up a language server, and {@code RosettaLanguageServerImpl} calls this during
 * language server construction. It can be enabled by setting the system
 * property or environment variable {@value #WARM_UP_ENABLED_VARIABLE_NAME} to
 * {@code true}.
 *
 * <p>This decides when the analysis is built, never whether serializing is safe.
 * {@link com.regnosys.rosetta.serializer.RosettaSerializer} builds it on demand
 * before any serialization either way, so leaving the flag off costs the first
 * serializer request a few seconds and nothing else.
 */
@Singleton
public class SerializerWarmUpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SerializerWarmUpService.class);

	/**
	 * Name of the system property or environment variable that enables the
	 * background serializer warm-up at language server construction. Set it to
	 * {@code true} to turn the warm-up on; it is off by default. It is a latency
	 * setting, not a safety one.
	 */
	public static final String WARM_UP_ENABLED_VARIABLE_NAME = "ENABLE_SERIALIZER_WARM_UP";

	private final IResourceServiceProvider.Registry registry;

	@Inject
	public SerializerWarmUpService(IResourceServiceProvider.Registry registry) {
		this.registry = registry;
	}

	/**
	 * Returns whether the background serializer warm-up is enabled, based on the
	 * {@value #WARM_UP_ENABLED_VARIABLE_NAME} system property or environment
	 * variable. Defaults to {@code false}.
	 */
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
		// Only languages that bind the Rosetta serializer are warmed up: for any other language the
		// warm-up would build an analysis nothing here is going to serialise.
		if (provider.get(ISerializer.class) instanceof RosettaSerializer) {
			provider.get(SerializerAnalysisWarmUp.class).warmUpAsync();
		}
	}
}
