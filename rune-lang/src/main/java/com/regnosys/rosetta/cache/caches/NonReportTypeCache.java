package com.regnosys.rosetta.cache.caches;

import com.google.common.cache.CacheBuilder;
import com.regnosys.rosetta.cache.AbstractRequestScopedCache;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.types.RDataType;

public class NonReportTypeCache extends AbstractRequestScopedCache<NonReportTypeCache.NonReportTypeCacheKey, Void> {
    public NonReportTypeCache() {
        super(
                CacheBuilder.newBuilder()
            );
    }
    
    public boolean isMarkedNonReportType(RosettaExternalRuleSource source, RDataType type) {
        return has(new NonReportTypeCacheKey(source, type));
    }
    public void markNonReportType(RosettaExternalRuleSource source, RDataType type) {
        put(new NonReportTypeCacheKey(source, type), null);
    }
    
    record NonReportTypeCacheKey(RosettaExternalRuleSource source, RDataType type) {}
}
