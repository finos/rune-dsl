package com.regnosys.rosetta.cache;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.regnosys.rosetta.utils.EnvironmentUtil;

import jakarta.inject.Provider;


public abstract class AbstractRequestScopedCache<K, V> implements IRequestScopedCache<K, V> {
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractRequestScopedCache.class);
	
	public static final String REQUEST_SCOPED_CACHE_ENABLED_VARIABLE_NAME = "ENABLE_REQUEST_SCOPED_CACHE";
	private static final boolean REQUEST_SCOPED_CACHE_ENABLED = EnvironmentUtil.getBooleanOrDefault(REQUEST_SCOPED_CACHE_ENABLED_VARIABLE_NAME, true);
	
	// A special object representing a cached null value.
	private final Object NULL_ENTRY = new Object();
	
	private final Cache<K, Object> managedCache;
	
	public AbstractRequestScopedCache(CacheBuilder<Object, Object> managedCache) {
		this.managedCache = managedCache.build();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public V get(K key, Provider<? extends V> loader) {
		if (!REQUEST_SCOPED_CACHE_ENABLED) {
			return loader.get();
		}
		
		Object result;
		try {
			result = managedCache.get(key, () -> Objects.requireNonNullElse(loader.get(), NULL_ENTRY));
		} catch (ExecutionException e) {
			result = handleLoaderException(e);
		}
		
		if (result == NULL_ENTRY) {
			return null;
		}
		return (V) result;
	}

	protected boolean has(K key) {
		return managedCache.getIfPresent(key) != null;
	}
	protected void put(K key, V value) {
		if (!REQUEST_SCOPED_CACHE_ENABLED) {
			return;
		}
        managedCache.put(key, Objects.requireNonNullElse(value, NULL_ENTRY));
	}
	
	@Override
	public void clear() {
		LOGGER.debug("Clearing cache {} with approximate size {}", this.getClass().getCanonicalName(), managedCache.size());
		managedCache.invalidateAll();
	}
	
	protected V handleLoaderException(ExecutionException e) {
		Throwable cause = e.getCause();
	    Throwables.throwIfUnchecked(cause);
	    throw new RuntimeException(e);
	}
}
