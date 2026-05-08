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

import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.serializer.analysis.IContextTypePDAProvider;

import com.regnosys.rosetta.serializer.RosettaContextTypePDAProvider;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Triggers a non-eager, asynchronous warm-up of the serializer
 * context-type PDA cache for every registered Xtext language.
 *
 * <p>This is invoked during construction of the language server so that the
 * cost of building the PDAs is paid in the background, rather than during the
 * first serializer request from a user.
 */
@Singleton
public class SerializerWarmUpService {
	
	private final IResourceServiceProvider.Registry registry;

	@Inject
	public SerializerWarmUpService(IResourceServiceProvider.Registry registry) {
		this.registry = registry;
	}

	public void warmUp() {
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
		IContextTypePDAProvider pdaProvider = provider.get(IContextTypePDAProvider.class);
		IGrammarAccess grammarAccess = provider.get(IGrammarAccess.class);
		if (pdaProvider instanceof RosettaContextTypePDAProvider && grammarAccess != null) {
			((RosettaContextTypePDAProvider) pdaProvider).warmUpAsync(grammarAccess.getGrammar());
		}
	}
}
