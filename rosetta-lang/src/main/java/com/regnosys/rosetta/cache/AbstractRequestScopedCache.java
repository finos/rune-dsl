package com.regnosys.rosetta.cache;

import com.google.common.cache.Cache;
import com.regnosys.rosetta.utils.EnvironmentUtil;

import jakarta.inject.Provider;


public abstract class AbstractRequestScopedCache<K, V> implements IRequestScopedCache<K, V> {
	public static final String REQUEST_SCOPED_CACHE_ENABLED_VARIABLE_NAME = "DISABLE_REQUEST_SCOPED_CACHE";
	private static final boolean REQUEST_SCOPED_CACHE_ENABLED = EnvironmentUtil.getBooleanOrDefault(REQUEST_SCOPED_CACHE_ENABLED_VARIABLE_NAME, true);
	
	private final Object NULL_SENTINEL = new Object();
	
	private final Cache<K, Object> managedCache;
	
	public AbstractRequestScopedCache(Cache<K, Object> managedCache) {
		this.managedCache = managedCache;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public V get(K key, Provider<? extends V> loader) {
		if (!REQUEST_SCOPED_CACHE_ENABLED) {
			return loader.get();
		}
		
		// If value is present - no need to synchronize.
		Object v = managedCache.getIfPresent(key);
		if (v == NULL_SENTINEL) return null;
		if (v != null) return (V) v;

		// If value is not present - perform get and put in same block.
		synchronized (this) {
		    v = managedCache.getIfPresent(key);
		    if (v == NULL_SENTINEL) return null;
		    if (v != null) return (V) v;

		    V computed = loader.get();
		    managedCache.put(key, computed != null ? computed : NULL_SENTINEL);
		    return computed;
		}
	}
	
	@Override
	public void clear() {
		managedCache.invalidateAll();
	}
}
