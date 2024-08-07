package com.regnosys.rosetta.cache;

import javax.inject.Provider;

public interface IRequestScopedCache {
	<T> T get(Object key, Provider<T> provider);
	
	void clear();
}
