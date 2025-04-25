package com.regnosys.rosetta.rules;

import java.util.LinkedHashMap;
import java.util.Map;

import com.regnosys.rosetta.types.RAttribute;

/** 
 * A map from attributes of one particular type to a `RulePathMap`, which keeps track of rules
 * attached to the attribute.
 */
class RuleAttributeMap {
	private final Map<String, RulePathMap> map = new LinkedHashMap<>();
	
	public void add(RAttribute attr, RulePathMap pathMap) {
		map.put(attr.getName(), pathMap);
	}
	public RulePathMap get(RAttribute attr) {
		return map.get(attr.getName());
	}
}
