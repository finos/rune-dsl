package com.regnosys.rosetta.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Interners;
import com.regnosys.rosetta.utils.EnvironmentUtil;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

/**
 * A cache that will clear every time a write request is made. For all subsequent read requests, the cache is reused.
 */
@Singleton
public class RequestScopedCache implements IRequestScopedCache {
	public static final String REQUEST_SCOPED_CACHE_ENABLED_VARIABLE_NAME = "DISABLE_REQUEST_SCOPED_CACHE";
	private static final boolean REQUEST_SCOPED_CACHE_ENABLED = EnvironmentUtil.getBooleanOrDefault(REQUEST_SCOPED_CACHE_ENABLED_VARIABLE_NAME, true);
	
	// A special object representing a cached null value.
	private final Object NULL = new Object();
	
	private final Map<Object, Object> values;
	
	@Inject
	public RequestScopedCache() {
		this(500);
	}
	public RequestScopedCache(int initialCapacity) {
		this.values = new ConcurrentHashMap<>(initialCapacity);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Provider<T> provider) {
		if (!REQUEST_SCOPED_CACHE_ENABLED) {
			return provider.get();
		}
				
		Object v = values.get(key);
		if (v == NULL) {
			return null;
		}
		if (v != null) {
			return (T)v;
		}
		
		T computed = provider.get();
		if (computed == null) {
			values.put(key, NULL);
		} else {
			values.put(key, computed);
		}
		return computed;
	}
	
	public void clear() {
		values.clear();
	}
}
