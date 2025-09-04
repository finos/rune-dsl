package com.regnosys.rosetta.rules;

import com.regnosys.rosetta.rosetta.*;
import com.regnosys.rosetta.rosetta.simple.AnnotationPathExpression;
import com.regnosys.rosetta.rosetta.simple.RuleReferenceAnnotation;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.utils.AnnotationPathExpressionUtil;
import jakarta.inject.Inject;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RuleReferenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleReferenceService.class);

    @Inject
    private AnnotationPathExpressionUtil pathExpressionUtil;

    /**
     * Traverse the tree structure defined by the attributes and their nested attributes of a given data type, together with their associated rule references.
     * For each attribute with an associated rule reference, a given callback is called, which based on a state, the current path
     * and the associated rule reference updates the current state. In functional programming, this is better known as a "fold".
     * <p>
     * During traversal, a context is passed down to remember rule references with a path that point towards nested attributes. In what follows, this is called
     * the "nested rule context".
     * <p>
     * A rule reference is said to be "associated" to an attribute if one of the three following conditions is true:
     * 1. The nested rule context has a rule reference that points to the attribute.
     * 2. The attribute has a rule reference that points to itself.
     * 3. The attribute inherits a rule reference that points to itself from a rule source or a super type.
     * If none of the above conditions are true, the attribute does not have an associated rule reference.
     * Note that an attribute with an `empty` rule reference is still considered to have an associated rule reference.
     * <p>
     * A rule source may be specified to determine which rule reference annotations to use. If the provided
     * rule source is null, only inline annotations on attributes are considered.
     * <p>
     * A "minus" in a rule source is equivalent to replacing all inherited rule references with an empty rule reference.
     * <p>
     * The traversal works as follows. For each attribute of the current data type:
     * 1. If the attribute has an associated rule reference, update the state based on
     * the current state, the path to the attribute and the associated rule.
     * 2. If the attribute does not have an associated rule reference, the attribute is single cardinality, and the type of the attribute is a data type,
     * traverse down that type while remembering the rule references that have a path pointing inside the attribute
     * by adding them to the nested rule context.
     * Note that the type of a multi-cardinality attribute is never considered for traversal.
     * <p>
     * If a cycle is detected in the traversed types, no more rule references are added to the nested rule context. If
     * the nested rule context is empty, the traversal terminates for that path.
     *
     * @param <T>          The type of the state during traversal.
     * @param source       The rule source to determine associated rule references. May be null to only consider inline rule references on attributes.
     * @param type         The type to traverse.
     * @param initialState The initial state.
     * @param updateState  A function that will be called each time an associated rule reference is found, and which updates the current state.
     *                     The first parameter represents the current state. The second parameter contains information about the current path
     *                     and the associated rule reference for that path.
     * @return The end state.
     */
    public <T> T traverse(RosettaExternalRuleSource source, RDataType type, T initialState, BiFunction<T, RuleReferenceContext, T> updateState) {
		return traverse(source, type, initialState, updateState, new HashMap<>(), new ArrayList<>(), new HashSet<>());
    }
	private <T> T traverse(RosettaExternalRuleSource source, RDataType type, T initialState, BiFunction<T, RuleReferenceContext, T> updateState, Map<List<String>, RuleResult> nestedRuleContext, List<RAttribute> path, Set<RDataType> visited) {		
        boolean isCycle = !visited.add(type);
        if (isCycle && nestedRuleContext.isEmpty()) {
            return initialState;
        }

        T currentState = initialState;
        for (RAttribute attr : type.getAllAttributes()) {
            String attrName = attr.getName();
            List<RAttribute> attrPath = new ArrayList<>(path);
            attrPath.add(attr);

            RuleResult ruleResult = nestedRuleContext.get(List.of(attrName));
            if (ruleResult != null) {
                currentState = updateState.apply(currentState, new RuleReferenceContext(attrPath, ruleResult));
            } else {
				RulePathMap pathMap = computeRulePathMapInContext(source, type, attr);
                ruleResult = pathMap.get(List.of());
                if (ruleResult != null) {
                    currentState = updateState.apply(currentState, new RuleReferenceContext(attrPath, ruleResult));
                } else if (!attr.isMulti()) {
                    RType attrType = attr.getRMetaAnnotatedType().getRType();
                    if (attrType instanceof RChoiceType) {
                        attrType = ((RChoiceType) attrType).asRDataType();
                    }
                    if (attrType instanceof RDataType) {
                        Map<List<String>, RuleResult> subcontext = getSubcontextForAttribute(attr, nestedRuleContext);
                        if (!isCycle) {
                            pathMap.addRulesToMapIfNotPresent(subcontext);
                        }
						currentState = traverse(source, (RDataType) attrType, currentState, updateState, subcontext, attrPath, new HashSet<>(visited));
                    }
                }
            }
        }
        return currentState;
    }

    public static class RuleReferenceContext {
        private final List<RAttribute> path;
        private final RuleResult ruleResult;

        public RuleReferenceContext(List<RAttribute> path, RuleResult ruleResult) {
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

        public boolean isExplicitlyEmpty() {
            return ruleResult.isExplicitlyEmpty();
        }

        public RosettaRule getRule() {
            return ruleResult.getRule();
        }

        public EObject getRuleOrigin() {
            return ruleResult.getOrigin();
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

	public RulePathMap computeRulePathMap(RAttribute attribute) {
		return computeRulePathMapInContext(null, attribute.getEnclosingType(), attribute);
	}
	public RulePathMap computeRulePathMapInContext(RosettaExternalRuleSource source, RDataType type, RAttribute attribute) {
        // First, compute parents from super sources and super types.
		RulePathMap parentInSameContext = null;
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
                	parentInSameContext = computeRulePathMapInContext(source, superType, attrInSuperType);
                }
            }
            
            if (source.getSuperRuleSources().isEmpty()) {
                RulePathMap parentMap = computeRulePathMapInContext(null, type, attribute);
                parentsInDescendingPriority.add(parentMap);
            } else {
                source.getSuperRuleSources().stream()
                        .map(superSource -> computeRulePathMapInContext(superSource, type, attribute))
                        .forEach(parentMap -> parentsInDescendingPriority.add(parentMap));
            }
        } else {
            // Outside rule source:
            // check if attribute overrides another attribute.
            RAttribute parentAttribute = attribute.getParentAttribute();
            if (parentAttribute != null) {
            	parentInSameContext = computeRulePathMapInContext(null, parentAttribute.getEnclosingType(), parentAttribute);
            }
        }
        String ruleSourceName = Optional.ofNullable(source).map(RosettaNamed::getName).orElse("no-source");
        String typeName = type.getName();
        String attributeName = attribute.getName();

        RulePathMap pathMap = new RulePathMap(String.join("-", ruleSourceName, typeName, attributeName), parentInSameContext, parentsInDescendingPriority);

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
        	if (ruleRef.getReportingRule() != null && ruleRef.getReportingRule().eIsProxy()) {
        		continue; // ignore unresolved rule references
        	}
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

    public RAttribute getTargetAttribute(RAttribute start, List<String> path) {
        RAttribute result = start;
        for (String next : path) {
            RType attrType = result.getRMetaAnnotatedType().getRType();
            if (attrType instanceof RChoiceType) {
                attrType = ((RChoiceType) attrType).asRDataType();
            }
            try {
                RDataType attrDataType = (RDataType) attrType;
                result = Objects.requireNonNull(attrDataType.getAttributeByName(next));
            } catch (ClassCastException | NullPointerException e) {
                // This should never happen - it indicates some invalid computation before calling this method.
                LOGGER.error("Error while following attribute `" + next + "` path " + path + " from " + start + ".", e);
                return null;
            }
        }
        return result;
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
