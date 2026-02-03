package com.regnosys.rosetta.cache;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.regnosys.rosetta.cache.caches.RDataTypeCache;
import com.regnosys.rosetta.cache.caches.RFunctionCache;
import com.regnosys.rosetta.cache.caches.NonReportTypeCache;
import com.regnosys.rosetta.cache.caches.RJavaPojoInterfaceCache;


public class RequestScopedCacheModule extends AbstractModule {
	private Multibinder<IRequestScopedCache<?, ?>> multibinder;
	
	@Override
    protected void configure() {
		multibinder = Multibinder.newSetBinder(binder(), new TypeLiteral<>() {});
		
		bindCache(RDataTypeCache.class);
		bindCache(RFunctionCache.class);
		bindCache(NonReportTypeCache.class);
		bindCache(RJavaPojoInterfaceCache.class);
    }

	private void bindCache(Class<? extends IRequestScopedCache<?, ?>> cacheClass) {
		multibinder.addBinding().to(cacheClass).in(Scopes.SINGLETON);
    	bind(cacheClass).in(Scopes.SINGLETON);
	}
}
