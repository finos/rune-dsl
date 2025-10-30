package com.regnosys.rosetta.cache.caches;

import com.google.common.cache.CacheBuilder;
import com.regnosys.rosetta.cache.AbstractRequestScopedCache;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.types.RFunction;

public class RFunctionCache extends AbstractRequestScopedCache<Function, RFunction> {
	public RFunctionCache() {
		super(
				CacheBuilder.newBuilder()
			);
	}
}