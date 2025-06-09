package com.regnosys.rosetta.rules;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A map for one particular attribute to keep track of which rules are attached to which path.
 * 
 * It support inheritance from multiple parent attributes, which can be used to model inheritance from
 * (super) rule sources and supertypes. Note that the order of parents matters: the first parent with
 * a rule attached to a particular path will take precedence.
 */
public class RulePathMap {
	private final RulePathMap parentInContext;
	private final List<RulePathMap> parentsOutsideContext;
	private final Map<List<String>, RuleResult> map = new LinkedHashMap<>();
	private final String id;
	
	public RulePathMap(String id, RulePathMap parentInContext, List<RulePathMap> parentsOutsideContextInDescendingPriority) {
		this.parentInContext = parentInContext;
		this.parentsOutsideContext = parentsOutsideContextInDescendingPriority;
		this.id = id;
	}

	public void add(List<String> path, RuleResult ruleResult) {
		map.put(path, ruleResult);
	}
	public RuleResult get(List<String> path) {
		var result = getInContext(path);
		if (result != null) {
			return result;
		}
		for (RulePathMap parent : parentsOutsideContext) {
			result = parent.get(path);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	private RuleResult getInContext(List<String> path) {
		var result = map.get(path);
		if (result != null) {
			return result;
		}
		if (parentInContext != null) {
			result = parentInContext.getInContext(path);
		}
		return result;
	}
	
	public Map<List<String>, RuleResult> getAsMap() {
		Map<List<String>, RuleResult> result = new LinkedHashMap<>();
		addRulesToMapIfNotPresent(result);
		return result;
	}
	public void addRulesToMapIfNotPresent(Map<List<String>, RuleResult> mapToAddTo) {
		addRulesInContextToMapIfNotPresent(mapToAddTo);
		for (RulePathMap parent : parentsOutsideContext) {
			parent.addRulesToMapIfNotPresent(mapToAddTo);
		}
	}
	private void addRulesInContextToMapIfNotPresent(Map<List<String>, RuleResult> mapToAddTo) {
		map.forEach(mapToAddTo::putIfAbsent);
		if (parentInContext != null) {
			parentInContext.addRulesInContextToMapIfNotPresent(mapToAddTo);
		}
	}
	
	/**
	 * Return all rules that are inherited from a parent and which
	 * are not overridden.
	 */
	public Map<List<String>, RuleResult> getInheritedRules() {
		Map<List<String>, RuleResult> result = getParentRules();
		result.keySet().removeAll(map.keySet());
		return result;
	}
	/**
	 * Return all rules defined in parents, even if they are overridden.
	 */
	public Map<List<String>, RuleResult> getParentRules() {
		Map<List<String>, RuleResult> result = new LinkedHashMap<>();
		if (parentInContext != null) {
			parentInContext.addRulesInContextToMapIfNotPresent(result);
		}
		for (RulePathMap parent : parentsOutsideContext) {
			parent.addRulesToMapIfNotPresent(result);
		}
		return result;
	}
	/**
	 * Return rules defined directly on the attribute.
	 */
	public Map<List<String>, RuleResult> getOwnRules() {
		return map;
	}

	@Override
	public String toString() {
		return "RulePathMap{" +
				"parentsSize=" + ((parentInContext == null ? 0 : 1) + parentsOutsideContext.size()) +
				", map=" + map +
				", id='" + id + '\'' +
				'}';
	}
}
