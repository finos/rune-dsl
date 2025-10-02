package com.regnosys.rosetta.cache;

import java.util.Set;

import jakarta.inject.Inject;

public class RequestScopedCacheManager {
	@Inject
	private Set<IRequestScopedCache<?, ?>> caches;
	
	public void clearAll() {
		for (var cache : caches) {
			cache.clear();
		}
	}
}
