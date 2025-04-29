package com.regnosys.rosetta.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

/**
 * A cache that will clear every time a write request is made. For all subsequent read requests, the cache is reused.
 */
@Singleton
public class RequestScopedCache implements IRequestScopedCache {
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
//		Object v = values.get(key);
//		if (v == NULL) {
//			return null;
//		}
//		if (v != null) {
//			return (T)v;
//		}
		
		T computed = provider.get();
//		if (computed == null) {
//			values.put(key, NULL);
//		} else {
//			values.put(key, computed);
//		}
		return computed;
	}
	
	public void clear() {
		values.clear();
	}
}
