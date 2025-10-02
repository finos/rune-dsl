package com.regnosys.rosetta.cache;

import jakarta.inject.Provider;

/**
 * A cache that will clear every time a write request is made. For all subsequent read requests, the cache is reused.
 */
interface IRequestScopedCache<K, V> {
	V get(K key, Provider<? extends V> provider);
	
	void clear();
}
