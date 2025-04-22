package com.regnosys.rosetta.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.regnosys.rosetta.rosetta.ExternalValueOperator;
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rosetta.simple.AnnotationPathExpression;
import com.regnosys.rosetta.rosetta.simple.RuleReferenceAnnotation;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.utils.AnnotationPathExpressionUtil;

public class RuleReferenceService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RuleReferenceService.class);
	
	@Inject
	private AnnotationPathExpressionUtil pathExpressionUtil;
	
	public RAttribute getTargetAttribute(RAttribute start, List<String> path) {
		RAttribute result = start;
		for (String next : path) {
			RType attrType = start.getRMetaAnnotatedType().getRType();
			if (attrType instanceof RChoiceType) {
				attrType = ((RChoiceType) attrType).asRDataType();
			}
			try {
				RDataType attrDataType = (RDataType)attrType;
				result = attrDataType.getAllAttributes().stream().filter(a -> next.equals(a.getName())).findAny().orElseThrow();
			} catch (ClassCastException | NoSuchElementException e) {
				// This should never happen - it indicates some invalid computation before calling this method.
				LOGGER.error("Error while following path " + path + " from " + start + ".", e);
				return  null;
			}
		}
		return result;
	}
	
	public <T> T traverse(RosettaExternalRuleSource source, RDataType type, T initial, BiFunction<T, FoldContext, T> foldFunc) {
		return traverse(source, type, initial, foldFunc, new RuleMap());
	}
	public <T> T traverse(RosettaExternalRuleSource source, RDataType type, T initial, BiFunction<T, FoldContext, T> foldFunc, RuleMap map) {
		return traverse(source, type, initial, foldFunc, map, new HashMap<>(), new ArrayList<>(), new HashSet<>());
	}
	private <T> T traverse(RosettaExternalRuleSource source, RDataType type, T initial, BiFunction<T, FoldContext, T> foldFunc, RuleMap map, Map<List<String>, RuleResult> context, List<RAttribute> path, Set<RDataType> visited) {		
		if (!visited.add(type)) {
			return initial;
		}
		
		T foldResult = initial;
		for (RAttribute attr : type.getAllAttributes()) {
			String attrName = attr.getName();
			List<RAttribute> attrPath = new ArrayList<>(path);
			attrPath.add(attr);
			
			RuleResult ruleResult = context.get(List.of(attrName));
			if (ruleResult != null) {
				foldResult = foldFunc.apply(foldResult, new FoldContext(attrPath, ruleResult));
			} else {
				RulePathMap pathMap = computeRulePathMapInContext(source, type, attr, map);
				ruleResult = pathMap.get(List.of());
				if (ruleResult != null) {
					foldResult = foldFunc.apply(foldResult, new FoldContext(attrPath, ruleResult));
				} else {
					RType attrType = attr.getRMetaAnnotatedType().getRType();
					if (attrType instanceof RChoiceType) {
						attrType = ((RChoiceType) attrType).asRDataType();
					}
					if (attrType instanceof RDataType) {
						Map<List<String>, RuleResult> subcontext = getSubcontextForAttribute(attr, context);
						pathMap.addToMapIfNotPresent(subcontext);
						foldResult = traverse(source, (RDataType) attrType, foldResult, foldFunc, map, subcontext, attrPath, new HashSet<>(visited));
					}
				}
			}
		}
		return foldResult;
	}
	public static class FoldContext {
		private final List<RAttribute> path;
		private final RuleResult ruleResult;
		public FoldContext(List<RAttribute> path, RuleResult ruleResult) {
			this.path = path;
			this.ruleResult = ruleResult;
		}
		
		public List<RAttribute> getPath() {
			return path;
		}
		public RAttribute getRootAttribute() {
			return path.get(0);
		}
		public RAttribute getTargetAttribute() {
			return path.get(path.size() - 1);
		}
 		public RuleResult getRuleResult() {
			return ruleResult;
		}
	}
	private Map<List<String>, RuleResult> getSubcontextForAttribute(RAttribute attribute, Map<List<String>, RuleResult> context) {
		String attributeName = attribute.getName();
		return context.entrySet().stream()
			.filter(e -> e.getKey().size() > 0 && e.getKey().get(0).equals(attributeName))
			.collect(Collectors.toMap(
					e -> e.getKey().subList(1, e.getKey().size()),
					e -> e.getValue()
				));
	}
	
	public RulePathMap computeRulePathMap(RDataType type, RAttribute attribute, RuleMap map) {
		return computeRulePathMapInContext(null, type, attribute, map);
	}
	public RulePathMap computeRulePathMapInContext(RosettaExternalRuleSource source, RDataType type, RAttribute attribute, RuleMap map) {
		RuleTypeMap typeMap = map.get(source);
		if (typeMap == null) {
			typeMap = new RuleTypeMap();
			map.add(source, typeMap);
		}
		
		RuleAttributeMap attrMap = typeMap.get(type);
		if (attrMap == null) {
			attrMap = new RuleAttributeMap();
			typeMap.add(type, attrMap);
		}
		
		RulePathMap existingPathMap = attrMap.get(attribute);
		if (existingPathMap != null) {
			return existingPathMap;
		}
		
		// First, compute parents from super sources and super types.
		List<RulePathMap> parentsInDescendingPriority = new ArrayList<>();
		
		if (source != null) {
			// Inside rule source:
			// - first look at super types within the same rule source,
			// - then look at super sources or outside this rule source.
			RDataType superType = type.getSuperType();
			if (superType != null) {
				// Due to attribute overrides, the attribute in the super type might be a different attribute with the same name.
				RAttribute attrInSuperType = superType.getAttributeByName(attribute.getName());
				if (attrInSuperType != null) {
					RulePathMap parentMap = computeRulePathMapInContext(source, superType, attrInSuperType, map);
					parentsInDescendingPriority.add(parentMap);
				}
			}
			
			if (source.getSuperRuleSources().isEmpty()) {
				RulePathMap parentMap = computeRulePathMapInContext(null, type, attribute, map);
				parentsInDescendingPriority.add(parentMap);
			} else {
				source.getSuperRuleSources().stream()
					.map(superSource -> computeRulePathMapInContext(superSource, type, attribute, map))
					.forEach(parentMap -> parentsInDescendingPriority.add(parentMap));
			}
		} else {
			// Outside rule source:
			// check if attribute overrides another attribute.
			RAttribute parentAttribute = attribute.getParentAttribute();
			if (parentAttribute != null) {
				RulePathMap parentMap = computeRulePathMapInContext(null, parentAttribute.getEnclosingType(), parentAttribute, map);
				parentsInDescendingPriority.add(parentMap);
			}
		}
		
		RulePathMap pathMap = new RulePathMap(parentsInDescendingPriority);
		attrMap.add(attribute, pathMap);
		
		// Second, add own rule annotations
		if (source == null) {
			addRuleReferenceAnnotationsToMap(attribute.getOwnRuleReferences(), pathMap);
		} else {
			findAttributesInSource(source, type, attribute).forEach(annotatedAttr -> {
				if (annotatedAttr.getOperator() == ExternalValueOperator.MINUS) {
					// Minus by setting all existing paths to null
					RuleResult minusResult = RuleResult.explicitlyEmptyFromMinusInRuleSource(annotatedAttr);
					var existingRules = pathMap.getAsMap();
					if (!existingRules.isEmpty()) {
						for (List<String> path : existingRules.keySet()) {
							pathMap.add(path, minusResult);
						}
					} else {
						// Add a dummy rule which will trigger a validation failure
						pathMap.add(List.of(), minusResult);
					}
				} else if (annotatedAttr.getOperator() == ExternalValueOperator.PLUS) {
					addRuleReferenceAnnotationsToMap(annotatedAttr.getExternalRuleReferences(), pathMap);
				}
			});
		}
		return pathMap;
	}
	
	private void addRuleReferenceAnnotationsToMap(List<RuleReferenceAnnotation> annotations, RulePathMap map) {
		for (RuleReferenceAnnotation ruleRef : annotations) {
			List<String> path = toList(ruleRef.getPath());
			if (path != null) { // Only add the annotation if the path is valid, i.e., if it does not contain any deep paths.
				map.add(path, RuleResult.fromAnnotation(ruleRef));
			}
		}
	}
	
	private Stream<RosettaExternalRegularAttribute> findAttributesInSource(RosettaExternalRuleSource source, RDataType type, RAttribute attribute) {
		return source.getExternalClasses().stream()
				.filter(t -> t.getData().equals(type.getEObject()))
				.findAny()
				.stream()
				.flatMap(t -> 
					t.getRegularAttributes().stream()
						.filter(a -> a.getAttributeRef().equals(attribute.getEObject()))
				);
	}
	
	private List<String> toList(AnnotationPathExpression path) {
		if (path == null) {
			return List.of();
		}
		return pathExpressionUtil.fold(
				path,
				(attr) -> {
					String name = attr.getName();
					if (name == null) {
						// Invalid: the reference to the attribute is unresolved
						return null;
					}
					List<String> acc = new ArrayList<>();
					acc.add(name);
					return acc;
				},
				(attr) -> new ArrayList<>(),
				(acc, pathExpr) -> {
					if (acc == null) {
						// In case the accumulator is invalid, stay invalid
						return null;
					}
					String name = pathExpr.getAttribute().getName();
					if (name == null) {
						// Invalid: the reference to the attribute is unresolved
						return null;
					}
					acc.add(pathExpr.getAttribute().getName());
					return acc;
				},
				(acc, deepPathExpr) -> {
					// Invalid: deep paths are not allowed
					return null;
				});
	}
}
