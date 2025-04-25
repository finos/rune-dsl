package com.regnosys.rosetta.rules;

import java.util.LinkedHashMap;
import java.util.Map;

import com.regnosys.rosetta.types.RDataType;

/** 
 * A map from types in one particular context (rule source or no rule source) to a `RuleAttributeMap`, which keeps track of rules
 * attached to its attributes.
 */
class RuleTypeMap {
	private final Map<RDataType, RuleAttributeMap> map = new LinkedHashMap<>();
	
	public void add(RDataType type, RuleAttributeMap attrMap) {
		map.put(type, attrMap);
	}
	public RuleAttributeMap get(RDataType type) {
		return map.get(type);
	}
}
