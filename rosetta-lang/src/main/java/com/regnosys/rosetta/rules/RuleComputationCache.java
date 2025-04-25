package com.regnosys.rosetta.rules;

import java.util.LinkedHashMap;
import java.util.Map;

import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;

/** 
 * A map from a context (being either a rule source or no rule source) to a `RuleTypeMap`, which keeps track of rules
 * attached to attributes for each type.
 */
// TODO: this class should be made package private and put into the `IRequestScopedCache`.
public class RuleComputationCache {
	private final Map<RosettaExternalRuleSource, RuleTypeMap> map = new LinkedHashMap<>();
	
	/**
	 * The source may be null, in which case this represents the context based on inline rule reference annotations
	 * directly on the types.
	 */
	public void add(RosettaExternalRuleSource source, RuleTypeMap typeMap) {
		map.put(source, typeMap);
	}
	/**
	 * The source may be null, in which case this represents the context based on inline rule reference annotations
	 * directly on the types.
	 */
	public RuleTypeMap get(RosettaExternalRuleSource source) {
		return map.get(source);
	}
}
