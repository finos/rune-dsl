package com.regnosys.rosetta.cache.caches;

import com.google.common.cache.CacheBuilder;
import com.regnosys.rosetta.cache.AbstractRequestScopedCache;
import com.regnosys.rosetta.generator.java.types.RJavaPojoInterface;
import com.regnosys.rosetta.types.RDataType;

public class RJavaPojoInterfaceCache extends AbstractRequestScopedCache<RDataType, RJavaPojoInterface> {
    public RJavaPojoInterfaceCache() {
        super(
                CacheBuilder.newBuilder()
        );
    }
}
