package com.regnosys.rosetta.cache.caches;

import com.google.common.cache.CacheBuilder;
import com.regnosys.rosetta.cache.AbstractRequestScopedCache;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RDataType;

public class RDataTypeCache extends AbstractRequestScopedCache<Data, RDataType> {
	public RDataTypeCache() {
		super(
				CacheBuilder.newBuilder()
			);
	}
}
