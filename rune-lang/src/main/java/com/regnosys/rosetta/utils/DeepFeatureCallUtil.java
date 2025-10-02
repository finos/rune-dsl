package com.regnosys.rosetta.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.regnosys.rosetta.rosetta.expression.OneOfOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RCardinality;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RType;

public class DeepFeatureCallUtil {
	public List<List<RAttribute>> findDeepFeaturePaths(RDataType type, RAttribute deepFeature) {
		List<List<RAttribute>> result = new ArrayList<>();
		findDeepFeaturePaths(type, new ArrayList<>(), deepFeature, result);
		return result;
	}
	private void findDeepFeaturePaths(RDataType currentType, List<RAttribute> currentPath, RAttribute deepFeature, List<List<RAttribute>> paths) {
		Collection<RAttribute> allAttrs = currentType.getAllAttributes();
		Optional<RAttribute> matchingAttribute = allAttrs.stream().filter(a -> match(a, deepFeature)).findAny();
		matchingAttribute
			.ifPresentOrElse(
				a -> {
					currentPath.add(a);
					paths.add(currentPath);
				},
				() -> {
					allAttrs.forEach(a -> {
						RType attrType = a.getRMetaAnnotatedType().getRType();
						if (attrType instanceof RChoiceType) {
							attrType = ((RChoiceType) attrType).asRDataType();
						}
						if (attrType instanceof RDataType) {
							List<RAttribute> newPath = new ArrayList<>(currentPath);
							newPath.add(a);
							findDeepFeaturePaths((RDataType) attrType, newPath, deepFeature, paths);
						}
					});
				});
	}
	
	public Collection<RAttribute> findDeepFeatures(RDataType type) {
		return findDeepFeatureMap(type).values();
	}
	
	public Map<String, RAttribute> findDeepFeatureMap(RDataType type) {
		if (!isEligibleForDeepFeatureCall(type)) {
			return new HashMap<>();
		}
		
		Map<String, RAttribute> deepIntersection = null;
		Map<String, RAttribute> result = new HashMap<>();
		Collection<RAttribute> allAttributes = type.getAllAttributes();
		for (RAttribute attr : allAttributes) {
			result.put(attr.getName(), attr);
		}
		for (RAttribute attr : allAttributes) {

			RType attrType = attr.getRMetaAnnotatedType().getRType();
			if (attrType instanceof RChoiceType) {
				attrType = ((RChoiceType) attrType).asRDataType();
			}
			Map<String, RAttribute> attrDeepFeatureMap;
			if (attrType instanceof RDataType) {
				RDataType attrDataType = (RDataType)attrType;
				attrDeepFeatureMap = findDeepFeatureMap(attrDataType);
				for (RAttribute attrFeature : attrDataType.getAllAttributes()) {
					attrDeepFeatureMap.put(attrFeature.getName(), attrFeature);
				}
			} else {
				attrDeepFeatureMap = new HashMap<>();
			}
			if (deepIntersection == null) {
				deepIntersection = attrDeepFeatureMap;
			} else {
				intersect(deepIntersection, attrDeepFeatureMap);
			}
			intersectButRetainAttribute(result, attrDeepFeatureMap, attr);
		}
		if (deepIntersection != null) {
			merge(result, deepIntersection);
		}
		return result;
	}
	private void intersect(Map<String, RAttribute> featuresMapToModify, Map<String, RAttribute> otherFeatureMap) {
		intersectButRetainAttribute(featuresMapToModify, otherFeatureMap, null);
	}
	private void intersectButRetainAttribute(Map<String, RAttribute> featuresMapToModify, Map<String, RAttribute> otherFeatureMap, RAttribute attributeToRetain) {
		featuresMapToModify.entrySet().removeIf(entry -> {
			String attrName = entry.getKey();
			RAttribute attr = entry.getValue();
			if (attr.equals(attributeToRetain)) {
				return false;
			}
			RAttribute otherAttr = otherFeatureMap.get(attrName);
			if (otherAttr != null) {
				if (match(attr, otherAttr)) {
					return false;
				}
			}
			return true;
		});
		// Make sure we don't give back an attribute with metadata if not all of them have it.
		for (Map.Entry<String, RAttribute> e : featuresMapToModify.entrySet()) {
			String name = e.getKey();
			RAttribute currFeature = e.getValue();
			RAttribute otherFeature = otherFeatureMap.get(name);
			if (otherFeature != null && currFeature.getRMetaAnnotatedType().hasAttributeMeta() && !otherFeature.getRMetaAnnotatedType().hasAttributeMeta()) {
				e.setValue(otherFeature);
			}
		}
	}
	private void merge(Map<String, RAttribute> featuresMapToModify, Map<String, RAttribute> otherFeatureMap) {
		otherFeatureMap.forEach((name, attr) -> {
			RAttribute candidate = featuresMapToModify.get(name);
			if (candidate != null) {
				if (!match(candidate, attr)) {
					featuresMapToModify.remove(name);
				} else if (candidate.getRMetaAnnotatedType().hasAttributeMeta() && !attr.getRMetaAnnotatedType().hasAttributeMeta()) {
					// Make sure we don't give back an attribute with metadata if not all of them have it.
					featuresMapToModify.put(name, attr);
				}
			} else {
				featuresMapToModify.put(name, attr);
			}
		});
	}
	public boolean match(RAttribute a, RAttribute b) {
		if (!a.getRMetaAnnotatedType().getRType().equals(b.getRMetaAnnotatedType().getRType())) {
			return false;
		}
		if (a.isMulti() != b.isMulti()) {
			return false;
		}
		return true;
	}
	
	public boolean isEligibleForDeepFeatureCall(RDataType type) {
		// Return true if:
		// 1. The data type has a `one-of` condition.
		// 2. All attributes have a cardinality of the form `(0..1)`.
		// 3. Type has at least one attribute.
		Data data = type.getEObject();
		if (data.getConditions().stream().anyMatch(cond -> isOneOfItem(cond.getExpression()))) {
			Collection<RAttribute> allAttributes = type.getAllAttributes();
			if (allAttributes.stream().allMatch(a -> isSingularOptional(a.getCardinality()))) {
				if (!allAttributes.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}
	private boolean isOneOfItem(RosettaExpression expr) {
		if (expr instanceof OneOfOperation) {
			if (((OneOfOperation) expr).getArgument() instanceof RosettaImplicitVariable) {
				return true;
			}
		}
		return false;
	}
	private boolean isSingularOptional(RCardinality card) {
		return card.equals(RCardinality.OPTIONAL);
	}
}
