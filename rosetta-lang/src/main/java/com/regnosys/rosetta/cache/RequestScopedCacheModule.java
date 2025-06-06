package com.regnosys.rosetta.cache;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.regnosys.rosetta.cache.caches.ExpectedTypeCache;
import com.regnosys.rosetta.cache.caches.ExpressionTypeCache;
import com.regnosys.rosetta.cache.caches.RDataTypeCache;
import com.regnosys.rosetta.cache.caches.RuleComputationCache;
import com.regnosys.rosetta.cache.caches.RulesInputTypeCache;


public class RequestScopedCacheModule extends AbstractModule {
	private Multibinder<IRequestScopedCache<?, ?>> multibinder;
	
	@Override
    protected void configure() {
		multibinder = Multibinder.newSetBinder(binder(), new TypeLiteral<IRequestScopedCache<?, ?>>() {});
		
		bindCache(RDataTypeCache.class);
		bindCache(ExpectedTypeCache.class);
		bindCache(RuleComputationCache.class);
		bindCache(RulesInputTypeCache.class);
		bindCache(ExpressionTypeCache.class);
    }

	private void bindCache(Class<? extends IRequestScopedCache<?, ?>> cacheClass) {
		multibinder.addBinding().to(cacheClass).in(Scopes.SINGLETON);
    	bind(cacheClass).in(Scopes.SINGLETON);
	}
}
