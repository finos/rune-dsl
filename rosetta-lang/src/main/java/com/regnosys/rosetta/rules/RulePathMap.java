package com.regnosys.rosetta.rules;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

/**
 * A map for one particular attribute to keep track of which rules are attached to which path.
 * 
 * It support inheritance from multiple parent attributes, which can be used to model inheritance from
 * (super) rule sources and supertypes. Note that the order of parents matters: the first parent with
 * a rule attached to a particular path will take precedence.
 */
public class RulePathMap {
	private final List<RulePathMap> parents;
	private Map<List<String>, RuleResult> map = new LinkedHashMap<>();

	public RulePathMap(List<RulePathMap> parentsInDescendingPriority) {
		this.parents = parentsInDescendingPriority;
	}

	public void add(List<String> path, RuleResult ruleResult) {
		map.put(path, ruleResult);
	}
	public RuleResult get(List<String> path) {
		var result = map.get(path);
		if (result != null) {
			return result;
		}
		for (RulePathMap parent : parents) {
			result = parent.get(path);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	public Map<List<String>, RuleResult> getAsMap() {
		Map<List<String>, RuleResult> result = new LinkedHashMap<>();
		addToMapIfNotPresent(result);
		return result;
	}
	public void addToMapIfNotPresent(Map<List<String>, RuleResult> mapToAddTo) {
		map.forEach(mapToAddTo::putIfAbsent);
		for (RulePathMap parent : parents) {
			parent.addToMapIfNotPresent(mapToAddTo);
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
		for (RulePathMap parent : parents) {
			parent.addToMapIfNotPresent(result);
		}
		return result;
	}
	/**
	 * Return rules defined directly on the attribute.
	 */
	public Map<List<String>, RuleResult> getOwnRules() {
		return map;
	}
}
