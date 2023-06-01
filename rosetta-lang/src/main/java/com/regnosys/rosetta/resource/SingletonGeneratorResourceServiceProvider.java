package com.regnosys.rosetta.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.resource.impl.DefaultResourceServiceProvider;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * A resource service provider that will reuse generator instances.
 * 
 * Necessary for running stateful external code generators using the rosetta-maven-plugin.
 *
 */
public class SingletonGeneratorResourceServiceProvider extends DefaultResourceServiceProvider {

	@Inject
	private Injector injector;
	
	private Map<Class<? extends GeneratorDelegate>, GeneratorDelegate> generatorCache = new HashMap<>();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Class<T> t) {
		try {
			if (GeneratorDelegate.class.isAssignableFrom(t)) {
				Class<? extends GeneratorDelegate> gt = (Class<? extends GeneratorDelegate>) t;
				return (T) generatorCache.computeIfAbsent(gt, (type) -> {
					return injector.getInstance(gt);
				});
			}
			return injector.getInstance(t);
		} catch (ConfigurationException e) {
			return null;
		}
	}
	
}
