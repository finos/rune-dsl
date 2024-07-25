package com.regnosys.rosetta.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.expression.OneOfOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.CardinalityProvider;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;

public class DeepFeatureCallUtil {
	private final RosettaTypeProvider typeProvider;
	private final CardinalityProvider cardinalityProvider;
	private final RosettaExtensions ext;
	
	@Inject
	public DeepFeatureCallUtil(RosettaTypeProvider typeProvider, CardinalityProvider cardinalityProvider, RosettaExtensions ext) {
		this.typeProvider = typeProvider;
		this.cardinalityProvider = cardinalityProvider;
		this.ext = ext;
	}
	
	public Collection<Attribute> findDeepFeatures(RDataType type) {
		return findDeepFeatureMap(type).values();
	}
	
	public Map<String, Attribute> findDeepFeatureMap(RDataType type) {
		if (!isEligibleForDeepFeatureCall(type)) {
			return new HashMap<>();
		}
		
		Map<String, Attribute> deepIntersection = null;
		Map<String, Attribute> result = new HashMap<>();
		for (Attribute attr : ext.allNonOverridesAttributes(type.getData())) {
			result.put(attr.getName(), attr);
		}
		for (Attribute attr : ext.allNonOverridesAttributes(type.getData())) {
			RType attrType = typeProvider.getRTypeOfSymbol(attr);
			Map<String, Attribute> attrDeepFeatureMap;
			if (attrType instanceof RDataType) {
				RDataType attrDataType = (RDataType)attrType;
				attrDeepFeatureMap = findDeepFeatureMap(attrDataType);
				for (Attribute attrFeature : ext.allNonOverridesAttributes(attrDataType.getData())) {
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
	private void intersect(Map<String, Attribute> featuresMapToModify, Map<String, Attribute> otherFeatureMap) {
		intersectButRetainAttribute(featuresMapToModify, otherFeatureMap, null);
	}
	private void intersectButRetainAttribute(Map<String, Attribute> featuresMapToModify, Map<String, Attribute> otherFeatureMap, Attribute attributeToRetain) {
		featuresMapToModify.entrySet().removeIf(entry -> {
			String attrName = entry.getKey();
			Attribute attr = entry.getValue();
			if (attr.equals(attributeToRetain)) {
				return false;
			}
			Attribute otherAttr = otherFeatureMap.get(attrName);
			if (otherAttr != null) {
				if (match(attr, otherAttr)) {
					return false;
				}
			}
			return true;
		});
		// Make sure we don't give back an attribute with metadata if not all of them have it.
		for (Map.Entry<String, Attribute> e : featuresMapToModify.entrySet()) {
			String name = e.getKey();
			Attribute currFeature = e.getValue();
			Attribute otherFeature = otherFeatureMap.get(name);
			if (otherFeature != null && !Iterables.isEmpty(ext.metaAnnotations(currFeature)) && Iterables.isEmpty(ext.metaAnnotations(otherFeature))) {
				e.setValue(otherFeature);
			}
		}
	}
	private void merge(Map<String, Attribute> featuresMapToModify, Map<String, Attribute> otherFeatureMap) {
		otherFeatureMap.forEach((name, attr) -> {
			Attribute candidate = featuresMapToModify.get(name);
			if (candidate != null) {
				if (!match(candidate, attr)) {
					featuresMapToModify.remove(name);
				} else if (!Iterables.isEmpty(ext.metaAnnotations(candidate)) && Iterables.isEmpty(ext.metaAnnotations(attr))) {
					// Make sure we don't give back an attribute with metadata if not all of them have it.
					featuresMapToModify.put(name, attr);
				}
			} else {
				featuresMapToModify.put(name, attr);
			}
		});
	}
	public boolean match(Attribute a, Attribute b) {
		if (!typeProvider.getRTypeOfFeature(a).equals(typeProvider.getRTypeOfFeature(b))) {
			return false;
		}
		if (cardinalityProvider.isFeatureMulti(a) != cardinalityProvider.isFeatureMulti(b)) {
			return false;
		}
		return true;
	}
	
	public boolean isEligibleForDeepFeatureCall(RDataType type) {
		// Return true if:
		// 1. The data type has a `one-of` condition.
		// 2. All attributes have a cardinality of the form `(0..1)`.
		// 3. Type has at least one attribute.
		Data data = type.getData();
		if (data.getConditions().stream().anyMatch(cond -> isOneOfItem(cond.getExpression()))) {
			if (data.getAttributes().stream().allMatch(a -> isSingularOptional(a.getCard()))) {
				if (!data.getAttributes().isEmpty()) {
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
	private boolean isSingularOptional(RosettaCardinality card) {
		return !card.isUnbounded() && card.getInf() == 0 && card.getSup() == 1;
	}
}
