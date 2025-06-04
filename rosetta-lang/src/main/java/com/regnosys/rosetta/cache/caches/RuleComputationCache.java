package com.regnosys.rosetta.cache.caches;

import com.google.common.cache.Cache;
import com.regnosys.rosetta.cache.AbstractRequestScopedCache;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rules.RulePathMap;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RDataType;

import jakarta.inject.Provider;


public class RuleComputationCache extends AbstractRequestScopedCache<RuleComputationCache.RuleContext, RulePathMap> {
	public RuleComputationCache(Cache<RuleContext, Object> managedCache) {
		super(managedCache);
	}
	
	/**
	 * Note that the source may be null.
	 */
	public RulePathMap get(RosettaExternalRuleSource source, RDataType type, RAttribute attribute, Provider<? extends RulePathMap> loader) {
		return get(new RuleContext(source, type, attribute.getName()), loader);
	}

	static record RuleContext(RosettaExternalRuleSource source, RDataType type, String attributeName) {}
}
