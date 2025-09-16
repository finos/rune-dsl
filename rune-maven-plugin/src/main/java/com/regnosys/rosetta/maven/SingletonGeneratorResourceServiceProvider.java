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

package com.regnosys.rosetta.maven;

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IResourceDescription.Manager;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.validation.IResourceValidator;

import com.google.inject.ConfigurationException;

/**
 * A resource service provider that will reuse generator instances.
 * 
 * Necessary for running stateful external code generators using the rosetta-maven-plugin.
 *
 */
public class SingletonGeneratorResourceServiceProvider implements IResourceServiceProvider {
	
	@Inject
	private IResourceServiceProvider internalServiceProvider;
	
	private Map<Class<? extends GeneratorDelegate>, GeneratorDelegate> generatorCache = new HashMap<>();

	@Override
	public IResourceValidator getResourceValidator() {
		return internalServiceProvider.getResourceValidator();
	}

	@Override
	public Manager getResourceDescriptionManager() {
		return internalServiceProvider.getResourceDescriptionManager();
	}

	@Override
	public IContainer.Manager getContainerManager() {
		return internalServiceProvider.getContainerManager();
	}

	@Override
	public boolean canHandle(URI uri) {
		return internalServiceProvider.canHandle(uri);
	}

	@Override
	public IEncodingProvider getEncodingProvider() {
		return internalServiceProvider.getEncodingProvider();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Class<T> t) {
		try {
			if (GeneratorDelegate.class.isAssignableFrom(t)) {
				Class<? extends GeneratorDelegate> gt = (Class<? extends GeneratorDelegate>) t;
				return (T) generatorCache.computeIfAbsent(gt, (type) -> {
					return internalServiceProvider.get(gt);
				});
			}
			return internalServiceProvider.get(t);
		} catch (ConfigurationException e) {
			return null;
		}
	}
	
}
