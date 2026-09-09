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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.serializer.impl.Serializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.serializer.RosettaSerializer;
import com.regnosys.rosetta.serializer.SerializerAnalysisWarmUp;

/**
 * Covers what this service decides: whether the language server warms anything, and which languages
 * it warms. The registry and the languages are stubs, because none of that is under test here — the
 * analysis itself is covered by {@code SerializerAnalysisTest}, and building a real one per language
 * would make this slow enough that nobody runs it.
 */
class SerializerWarmUpServiceTest {

	@AfterEach
	void clearFlag() {
		System.clearProperty(SerializerWarmUpService.WARM_UP_ENABLED_VARIABLE_NAME);
	}

	@Test
	void isOffByDefault() {
		CountingLanguage rosetta = new CountingLanguage(new RosettaSerializer());

		serviceFor(rosetta).warmUp();

		// Off by default is the point of the flag: enabling it costs every language server startup
		// several CPU-bound seconds, which is why the tests that spin one up cannot afford it.
		assertEquals(0, rosetta.warmUps.get());
	}

	@Test
	void warmsUpOnlyLanguagesBindingTheRosettaSerializer() {
		System.setProperty(SerializerWarmUpService.WARM_UP_ENABLED_VARIABLE_NAME, "true");
		CountingLanguage rosetta = new CountingLanguage(new RosettaSerializer());
		CountingLanguage other = new CountingLanguage(new Serializer());

		serviceFor(rosetta, other).warmUp();

		// A language that binds Xtext's own serializer has no gate, so warming it would build an
		// analysis nothing here is going to serialise.
		assertEquals(1, rosetta.warmUps.get());
		assertEquals(0, other.warmUps.get());
	}

	@Test
	void warmsUpALanguageRegisteredUnderSeveralExtensionsOnce() {
		System.setProperty(SerializerWarmUpService.WARM_UP_ENABLED_VARIABLE_NAME, "true");
		CountingLanguage rosetta = new CountingLanguage(new RosettaSerializer());

		new SerializerWarmUpService(registryOf(Map.of("rosetta", rosetta, "rosetta-alias", rosetta))).warmUp();

		assertEquals(1, rosetta.warmUps.get());
	}

	private static SerializerWarmUpService serviceFor(CountingLanguage... languages) {
		Map<String, Object> byExtension = new LinkedHashMap<>();
		for (int i = 0; i < languages.length; i++) {
			byExtension.put("language" + i, languages[i]);
		}
		return new SerializerWarmUpService(registryOf(byExtension));
	}

	private static IResourceServiceProvider.Registry registryOf(Map<String, Object> byExtension) {
		return new IResourceServiceProvider.Registry() {
			@Override
			public IResourceServiceProvider getResourceServiceProvider(URI uri) {
				return null;
			}

			@Override
			public IResourceServiceProvider getResourceServiceProvider(URI uri, String contentType) {
				return null;
			}

			@Override
			public Map<String, Object> getContentTypeToFactoryMap() {
				return Map.of();
			}

			@Override
			public Map<String, Object> getExtensionToFactoryMap() {
				return byExtension;
			}

			@Override
			public Map<String, Object> getProtocolToFactoryMap() {
				return Map.of();
			}
		};
	}

	/**
	 * A language whose only interesting property is which constraint provider it binds, and which
	 * counts the warm-ups asked of it instead of running one.
	 */
	private static class CountingLanguage implements IResourceServiceProvider {
		private final ISerializer serializer;
		private final AtomicInteger warmUps = new AtomicInteger();

		CountingLanguage(ISerializer serializer) {
			this.serializer = serializer;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(Class<T> type) {
			if (type == ISerializer.class) {
				return (T) serializer;
			}
			if (type == SerializerAnalysisWarmUp.class) {
				return (T) new CountingWarmUp(warmUps);
			}
			throw new IllegalArgumentException("Nothing here asks for " + type);
		}

		@Override
		public IResourceValidator getResourceValidator() {
			return null;
		}

		@Override
		public IEncodingProvider getEncodingProvider() {
			return null;
		}

		@Override
		public boolean canHandle(URI uri) {
			return true;
		}

		@Override
		public IResourceDescription.Manager getResourceDescriptionManager() {
			return null;
		}

		@Override
		public IContainer.Manager getContainerManager() {
			return null;
		}
	}

	private static class CountingWarmUp extends SerializerAnalysisWarmUp {
		private final AtomicInteger warmUps;

		CountingWarmUp(AtomicInteger warmUps) {
			// Safe because warmUpAsync() is overridden below and never touches the injected fields,
			// which are the only thing the real constructor's arguments are used for.
			super(null, null, null, null, null);
			this.warmUps = warmUps;
		}

		@Override
		public CompletableFuture<Void> warmUpAsync() {
			warmUps.incrementAndGet();
			return CompletableFuture.completedFuture(null);
		}
	}
}
