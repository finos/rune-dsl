package com.regnosys.rosetta.cache;

import java.util.Set;

import jakarta.inject.Inject;

public class RequestScopedCacheManager {
	private final Set<IRequestScopedCache<?, ?>> caches;
	
	@Inject
	public RequestScopedCacheManager(Set<IRequestScopedCache<?, ?>> caches) {
		this.caches = caches;
	}
	
	public void clearAll() {
		for (var cache : caches) {
			cache.clear();
		}
	}
}
