package com.regnosys.rosetta.cache.caches;

import com.google.common.cache.Cache;
import com.regnosys.rosetta.cache.AbstractRequestScopedCache;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RDataType;

public class RDataTypeCache extends AbstractRequestScopedCache<Data, RDataType> {
	public RDataTypeCache(Cache<Data, Object> managedCache) {
		super(managedCache);
	}
}
