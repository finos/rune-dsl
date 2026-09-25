/*
 * Copyright 2026 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.generator.java.labels;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.FluentRObjectJavaClassGenerator;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope;
import com.regnosys.rosetta.generator.java.statement.builder.JavaLiteral;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.lib.labelprovider.GraphBasedLabelProvider;
import com.regnosys.rosetta.lib.labelprovider.LabelNode;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.AnnotationPathExpression;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.LabelAnnotation;
import com.regnosys.rosetta.rosetta.simple.RuleReferenceAnnotation;
import com.regnosys.rosetta.rules.RuleReferenceService;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RFunction;
import com.regnosys.rosetta.types.RObject;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.utils.AnnotationPathExpressionUtil;
import com.regnosys.rosetta.utils.DeepFeatureCallUtil;
import com.rosetta.util.DottedPath;

import jakarta.inject.Inject;

/**
 * Generates a {@link GraphBasedLabelProvider} for:
 * <ul>
 * <li>every transform function and report, rooted at the function's/report's <b>output</b> type
 * (unchanged, long-standing behaviour); and
 * <li>every type - {@code type} or {@code choice} - that carries a direct {@code [label ...]} on one of
 * its own, inherited or overridden attributes (see {@link LabelProviderGeneratorUtil#shouldGenerateLabelProvider(RDataType)}),
 * rooted at that type itself.
 * </ul>
 * Two invariants hold across both flavours and must not be "simplified" away:
 * <p>
 * <b>1. Providers are not composable or substitutable.</b> A provider rooted at type {@code T} means
 * "labels as seen with {@code T} as the root". A deep-path label (e.g. {@code [label for a -> b ...]}, or
 * {@code ->>}) is stored on the <i>declaring</i> type and deliberately overrides what the nested type's
 * own labels would give for that same path. So an outer type's provider and an inner type's provider can
 * legitimately disagree on the label for the same relative path - that is by design, not a bug. Do not
 * merge, delegate to, or dedupe between providers rooted at different types.
 * <p>
 * <b>2. A function/report provider must not extend, delegate to, or have its label-provider annotation
 * point at a type-rooted provider.</b> The obvious "simplification" -
 * {@code <Func>LabelProvider extends <Type>LabelProvider}, or dropping the function class and pointing
 * its annotation at the type provider - looks like removing duplication when a function's output type
 * happens to have direct labels (in which case the two providers are byte-identical anyway; see the
 * "not the reason" note below). It is wrong, because of where each provider is emitted:
 * <ul>
 * <li>A type-rooted provider is emitted only when the <i>type's own</i> model is generated, into that
 * type's own namespace.
 * <li>A transform function may output a type defined in a different artifact - an ingest into CDM types,
 * or a projection to a type from a dependency.
 * <li>That upstream artifact has no way to know a downstream model will root a transform at the type, so it
 * emits a type-rooted provider for it only if the type happens to carry direct labels of its own (per the
 * gate above) - and even then, a dependency built with an older DSL version may have no {@code labels.types}
 * package at all.
 * </ul>
 * So a function/report provider cannot be replaced by, or delegate to, a type-rooted one: the
 * target class may simply not exist, and no amount of widening the gate fixes this, because the information
 * that would justify emitting it (that some downstream model roots a transform at this type) does not exist
 * where the type is defined. Not the reason, for completeness: it is not that the function gate can fire when
 * the output type has no labels at all - {@code pruneLabelGraph} already collapses that case to an empty
 * provider today. Keep every function/report provider self-contained, generated in full into the function's
 * own namespace, independent of whether a type-rooted twin exists. This is not a migration ramp: the gate in
 * {@link LabelProviderGeneratorUtil#shouldGenerateLabelProvider(RDataType)} is deliberately "direct labels
 * only" (see its javadoc), so an output type whose labels are all on nested descendants never gets a
 * type-rooted provider at all, in any DSL version, regardless of how many dependencies regenerate. The
 * function/report provider is therefore permanently load-bearing for that shape, not a stopgap awaiting a
 * type-rooted replacement - it must never be marked {@code @Deprecated}.
 * <p>
 * On this branch, a report's rule source can still contribute a label via the legacy
 * {@code rule ... as "identifier"} syntax (see {@code RosettaRule.identifier} in the grammar); this
 * predates, and is independent of, the type-rooted case above.
 */
public class LabelProviderGenerator extends FluentRObjectJavaClassGenerator<RObject, RGeneratedJavaClass<?>> {
	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private DeepFeatureCallUtil deepPathUtil;
	@Inject
	private LabelProviderGeneratorUtil util;
	@Inject
	private RuleReferenceService ruleService;
	@Inject
	private AnnotationPathExpressionUtil annotationPathUtil;

	@Override
	protected Stream<? extends RObject> streamObjects(RosettaModel model) {
		return model.getElements().stream()
				.<RObject>map(element -> {
					if (element instanceof Function function && util.shouldGenerateLabelProvider(function)) {
						return rObjectFactory.buildRFunction(function);
					} else if (element instanceof RosettaReport report) {
						return rObjectFactory.buildRFunction(report);
					} else if (element instanceof Data data) {
						RDataType type = rObjectFactory.buildRDataType(data);
						if (util.shouldGenerateLabelProvider(type)) {
							return type;
						}
					}
					return null;
				})
				.filter(Objects::nonNull);
	}

	@Override
	protected RGeneratedJavaClass<?> createTypeRepresentation(RObject target) {
		if (target instanceof RFunction function) {
			return typeTranslator.toLabelProviderJavaClass(function);
		}
		return typeTranslator.toLabelProviderJavaClass((RDataType) target);
	}

	@Override
	protected CodeRenderer generateClass(RObject target, RGeneratedJavaClass<?> labelClass, String version, JavaClassScope classScope) {
		Map<RAttribute, RosettaRule> attributeToRuleMap = target instanceof RFunction function && function.getEObject() instanceof RosettaReport report
				? legacyRuleLabels(report, (RDataType) function.getOutput().getRMetaAnnotatedType().getRType())
				: Map.of();

		JavaMethodScope constructorScope = classScope.createMethodScope("constructor");

		Map<RDataType, Map<DottedPath, String>> labelsPerNode = new LinkedHashMap<>();
		Map<RDataType, Map<String, RDataType>> edgesPerNode = new LinkedHashMap<>();
		RType startNode = target instanceof RFunction function
				? normalizeChoiceType(function.getOutput().getRMetaAnnotatedType().getRType())
				: (RDataType) target;
		if (startNode instanceof RDataType startType) {
			buildLabelGraph(startType, labelsPerNode, edgesPerNode, attributeToRuleMap);
			pruneLabelGraph(labelsPerNode, edgesPerNode);
		}
		constructorScope.createIdentifier(startNode, "startNode");
		labelsPerNode.keySet().forEach(node -> {
			if (!node.equals(startNode)) {
				constructorScope.createIdentifier(node, StringUtils.uncapitalize(node.getName()) + "Node");
			}
		});

		return out -> {
			out.writeln("public class ", labelClass, " extends ", GraphBasedLabelProvider.class, " {");
			out.indented(() -> {
				out.writeln("public ", labelClass, "() {");
				out.indented(() -> {
					out.writeln("super(new ", LabelNode.class, "());");
					out.newline();
					labelsPerNode.forEach((node, labels) -> {
						GeneratedIdentifier nodeVarName = constructorScope.getIdentifierOrThrow(node);
						if (!node.equals(startNode)) {
							out.newline();
							out.writeln(LabelNode.class, " ", nodeVarName, " = new ", LabelNode.class, "();");
						}
						labels.forEach((path, label) ->
								out.writeln(nodeVarName, ".addLabel(", representAsList(path), ", ", JavaLiteral.STRING(label), ");"));
					});
					edgesPerNode.forEach((node, edges) -> {
						if (!edges.isEmpty()) {
							out.newline();
							GeneratedIdentifier nodeVarName = constructorScope.getIdentifierOrThrow(node);
							edges.forEach((pathElement, edgeTarget) ->
									out.writeln(nodeVarName, ".addOutgoingEdge(", JavaLiteral.STRING(pathElement), ", ", constructorScope.getIdentifierOrThrow(edgeTarget), ");"));
						}
					});
				});
				out.writeln("}");
			});
			out.write("}");
		};
	}

	private CodeRenderer representAsList(DottedPath path) {
		return out -> {
			out.write(Arrays.class, ".asList(");
			out.join(path.stream().toList(), ", ", segment -> out.write(JavaLiteral.STRING(segment)));
			out.write(")");
		};
	}

	private RType normalizeChoiceType(RType type) {
		return type instanceof RChoiceType choiceType ? choiceType.asRDataType() : type;
	}

	private Map<RAttribute, RosettaRule> legacyRuleLabels(RosettaReport report, RDataType outputType) {
		return ruleService.traverse(report.getRuleSource(), outputType, new HashMap<>(), (map, context) -> {
			if (context.getRule() != null && context.getRule().getIdentifier() != null
					&& context.getRuleOrigin() instanceof RuleReferenceAnnotation origin && origin.getPath() == null) {
				map.put(context.getTargetAttribute(), context.getRule());
			}
			return map;
		});
	}

	private void buildLabelGraph(RDataType currentNode, Map<RDataType, Map<DottedPath, String>> labelsPerNode, Map<RDataType, Map<String, RDataType>> edgesPerNode, Map<RAttribute, RosettaRule> attributeToRuleMap) {
		if (labelsPerNode.containsKey(currentNode)) {
			// Circular reference: we already computed this node.
			return;
		}
		Map<DottedPath, String> labels = new LinkedHashMap<>();
		labelsPerNode.put(currentNode, labels);
		Map<String, RDataType> edges = new LinkedHashMap<>();
		edgesPerNode.put(currentNode, edges);
		for (RAttribute attr : currentNode.getAllAttributes()) {
			DottedPath attrPath = DottedPath.of(attr.getName());

			// 1. Register labels on the type of this attribute
			if (normalizeChoiceType(attr.getRMetaAnnotatedType().getRType()) instanceof RDataType attrType) {
				edges.put(attr.getName(), attrType);
				buildLabelGraph(attrType, labelsPerNode, edgesPerNode, attributeToRuleMap);
			}

			// 2. Register legacy `as` annotations from rule references
			RosettaRule ruleRef = attributeToRuleMap.get(attr);
			if (ruleRef != null && ruleRef.getIdentifier() != null) {
				labels.put(attrPath, ruleRef.getIdentifier());
			}

			// 3. Register label annotations
			attr.getAllLabelAnnotations().forEach(ann -> registerLabelAnnotation(ann, attrPath, labels));
		}
	}

	private void pruneLabelGraph(Map<RDataType, Map<DottedPath, String>> labelsPerNode, Map<RDataType, Map<String, RDataType>> edgesPerNode) {
		// For each possible path in the graph, see if it is possible to reach any label.
		// If not, prune those nodes.
		Set<RDataType> nodes = new LinkedHashSet<>(labelsPerNode.keySet());

		Set<RDataType> nodesWithReachableLabels = new LinkedHashSet<>();
		nodes.stream()
				.filter(node -> !labelsPerNode.get(node).isEmpty())
				.forEach(nodesWithReachableLabels::add);

		boolean anyReachableNodesFoundInIteration = true;
		while (anyReachableNodesFoundInIteration) {
			anyReachableNodesFoundInIteration = false;
			for (RDataType node : nodes) {
				if (!nodesWithReachableLabels.contains(node)) {
					boolean hasEdgeToNodeWithReachableLabel = edgesPerNode.get(node).values().stream().anyMatch(nodesWithReachableLabels::contains);
					if (hasEdgeToNodeWithReachableLabel) {
						nodesWithReachableLabels.add(node);
						anyReachableNodesFoundInIteration = true;
					}
				}
			}
		}
		// prune
		for (RDataType node : nodes) {
			if (!nodesWithReachableLabels.contains(node)) {
				labelsPerNode.remove(node);
				edgesPerNode.remove(node);
				edgesPerNode.values().forEach(edges -> edges.entrySet().removeIf(edge -> edge.getValue().equals(node)));
			}
		}
	}

	private void registerLabelAnnotation(LabelAnnotation ann, DottedPath attrPath, Map<DottedPath, String> labels) {
		evaluateAnnotationPathExpression(attrPath, ann.getPath())
				.forEach(path -> labels.put(path, ann.getLabel()));
	}

	private List<DottedPath> evaluateAnnotationPathExpression(DottedPath root, AnnotationPathExpression expr) {
		if (expr == null) {
			return List.of(root);
		}
		return annotationPathUtil.fold(
				expr,
				attr -> List.of(root.child(attr.getName())),
				attr -> List.of(root),
				(paths, path) -> paths.stream().map(p -> p.child(path.getAttribute().getName())).toList(),
				(paths, deepPath) -> {
					if (normalizeChoiceType(typeProvider.getRMetaAnnotatedType(deepPath.getReceiver()).getRType()) instanceof RDataType receiverType) {
						RAttribute deepFeature = rObjectFactory.buildRAttribute(deepPath.getAttribute());
						return paths.stream()
								.flatMap(p -> deepPathUtil.findDeepFeaturePaths(receiverType, deepFeature).stream()
										.map(attrs -> p.concat(DottedPath.of(attrs.stream().map(RAttribute::getName).toArray(String[]::new)))))
								.toList();
					}
					return List.of();
				});
	}
}
