package com.regnosys.rosetta.cache.caches;

import com.google.common.cache.CacheBuilder;
import com.regnosys.rosetta.cache.AbstractRequestScopedCache;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RType;

import jakarta.inject.Provider;

public class RulesInputTypeCache extends AbstractRequestScopedCache<RulesInputTypeCache.ReportType, RType> {
	public RulesInputTypeCache() {
		super(
				CacheBuilder.newBuilder()
			);
	}
	
	/**
	 * Note that the source may be null.
	 */
	public RType get(RosettaExternalRuleSource source, RDataType type, Provider<? extends RType> loader) {
		return get(new ReportType(source, type), loader);
	}

	static record ReportType(RosettaExternalRuleSource source, RDataType type) {}
}
