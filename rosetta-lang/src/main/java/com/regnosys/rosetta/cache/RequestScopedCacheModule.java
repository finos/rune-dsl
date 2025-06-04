package com.regnosys.rosetta.cache;

import java.util.ArrayList;
import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.regnosys.rosetta.cache.caches.ExpectedTypeCache;
import com.regnosys.rosetta.cache.caches.ExpressionTypeCache;
import com.regnosys.rosetta.cache.caches.RDataTypeCache;
import com.regnosys.rosetta.cache.caches.RuleComputationCache;
import com.regnosys.rosetta.cache.caches.RulesInputTypeCache;


public class RequestScopedCacheModule extends AbstractModule {
	private final List<IRequestScopedCache<?, ?>> caches = new ArrayList<>();
	
	private void configureCaches() {
		caches.add(
			new RDataTypeCache(
				CacheBuilder.newBuilder()
					.build()
			));
		
		caches.add(
			new ExpectedTypeCache(
				CacheBuilder.newBuilder()
					.build()
			));
		
		caches.add(
			new RuleComputationCache(
				CacheBuilder.newBuilder()
					.build()
			));
		
		caches.add(
			new RulesInputTypeCache(
				CacheBuilder.newBuilder()
					.build()
			));

		caches.add(
			new ExpressionTypeCache(
				CacheBuilder.newBuilder()
					.weakKeys() // keys are reference-based and should be cleaned up under memory pressure
					.build()
			));
	}
	
	@SuppressWarnings("unchecked")
	@Override
    protected void configure() {
		configureCaches();
		
        Multibinder<IRequestScopedCache<?, ?>> multibinder = Multibinder.newSetBinder(binder(), new TypeLiteral<IRequestScopedCache<?, ?>>() {});
        for (var cache : caches) {
        	multibinder.addBinding().toInstance(cache);
        	bind((Class<IRequestScopedCache<?, ?>>)cache.getClass()).toInstance(cache);
        }
    }
}
