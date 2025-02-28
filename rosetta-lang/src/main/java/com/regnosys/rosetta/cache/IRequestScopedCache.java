package com.regnosys.rosetta.cache;

import jakarta.inject.Provider;

public interface IRequestScopedCache {
	<T> T get(Object key, Provider<T> provider);
	
	void clear();
}
