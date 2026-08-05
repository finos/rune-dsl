package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.RosettaReport
import com.regnosys.rosetta.types.RObject
import com.regnosys.rosetta.types.RObjectFactory
import jakarta.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.types.RFunction
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import java.util.Map
import com.regnosys.rosetta.types.RDataType
import com.rosetta.util.DottedPath
import com.regnosys.rosetta.rosetta.simple.LabelAnnotation
import java.util.List
import com.regnosys.rosetta.rosetta.simple.AnnotationPathExpression
import com.regnosys.rosetta.utils.DeepFeatureCallUtil
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.types.RChoiceType
import org.apache.commons.text.StringEscapeUtils
import com.regnosys.rosetta.lib.labelprovider.GraphBasedLabelProvider
import com.regnosys.rosetta.lib.labelprovider.LabelNode
import java.util.Arrays
import java.util.stream.Collectors
import java.util.HashSet
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.utils.AnnotationPathExpressionUtil
import com.regnosys.rosetta.generator.java.RObjectJavaClassGenerator
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope
import com.regnosys.rosetta.rosetta.RosettaModel

/**
 * Generates a {@link com.regnosys.rosetta.lib.labelprovider.GraphBasedLabelProvider} for:
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
 * <b>2. A function/report provider must never extend, delegate to, or have its label-provider annotation
 * point at a type-rooted provider - permanently, not just until some future cleanup.</b> The obvious
 * "simplification" - {@code <Func>LabelProvider extends <Type>LabelProvider}, or dropping the function
 * class and pointing its annotation at the type provider - looks like removing duplication when a
 * function's output type happens to have direct labels (in which case the two providers are byte-identical
 * anyway; see the "not the reason" note below). It is wrong regardless, because of where each provider is
 * emitted:
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
 * So a function/report provider cannot in general be replaced by, or delegate to, a type-rooted one: the
 * target class may simply not exist, and no amount of widening the gate fixes this, because the information
 * that would justify emitting it (that some downstream model roots a transform at this type) does not exist
 * where the type is defined. Not the reason, for completeness: it is not that the function gate can fire when
 * the output type has no labels at all - {@code pruneLabelGraph} already collapses that case to an empty
 * provider today. Keep every function/report provider self-contained, generated in full into the function's
 * own namespace, independent of whether a type-rooted twin exists.
 */
class LabelProviderGenerator extends RObjectJavaClassGenerator<RObject, RGeneratedJavaClass<?>> {
	@Inject RObjectFactory rObjectFactory
	@Inject RosettaTypeProvider typeProvider
	@Inject JavaTypeTranslator typeTranslator
	@Inject DeepFeatureCallUtil deepPathUtil
	@Inject LabelProviderGeneratorUtil util
	@Inject AnnotationPathExpressionUtil annotationPathUtil


	override protected streamObjects(RosettaModel model) {
		model.elements.stream.<RObject>map[
			if (it instanceof Function && util.shouldGenerateLabelProvider(it as Function)) {
				return rObjectFactory.buildRFunction(it as Function)
			} else if (it instanceof RosettaReport) {
				return rObjectFactory.buildRFunction(it)
			} else if (it instanceof Data) {
				val type = rObjectFactory.buildRDataType(it)
				if (util.shouldGenerateLabelProvider(type)) {
					return type
				}
			}
			null
		].filter[it !== null]
	}
	override protected createTypeRepresentation(RObject target) {
		if (target instanceof RFunction) {
			typeTranslator.toLabelProviderJavaClass(target)
		} else {
			typeTranslator.toLabelProviderJavaClass(target as RDataType)
		}
	}
	override protected generateClass(RObject target, RGeneratedJavaClass<?> labelClass, String version, JavaClassScope classScope) {
		val constructorScope = classScope.createMethodScope("constructor")

		val Map<RDataType, Map<DottedPath, String>> labelsPerNode = newLinkedHashMap
		val edgesPerNode = newLinkedHashMap
		val startNode = if (target instanceof RFunction) {
			val outputType = target.output.RMetaAnnotatedType.RType
			if (outputType instanceof RChoiceType) {
				outputType.asRDataType
			} else {
				outputType
			}
		} else {
			target as RDataType
		}
		if (startNode instanceof RDataType) {
			buildLabelGraph(startNode, labelsPerNode, edgesPerNode)
			pruneLabelGraph(labelsPerNode, edgesPerNode)
		}
		constructorScope.createIdentifier(startNode, "startNode")
		labelsPerNode.keySet.forEach[node|
			if (node != startNode) {
				constructorScope.createIdentifier(node, node.name.toFirstLower + "Node")
			}
		]

		'''
			public class «labelClass» extends «GraphBasedLabelProvider» {
				public «labelClass»() {
					super(new «LabelNode»());
					
					«FOR node : labelsPerNode.keySet»
						«val nodeVarName = constructorScope.getIdentifierOrThrow(node)»
						«val labels = labelsPerNode.get(node)»
						«IF node != startNode»
							
							«LabelNode» «nodeVarName» = new «LabelNode»();
						«ENDIF»
						«FOR path : labels.keySet»
							«nodeVarName».addLabel(«path.representAsList», "«StringEscapeUtils.escapeJava(labels.get(path))»");
						«ENDFOR»
					«ENDFOR»
					«FOR node : edgesPerNode.keySet»
						«val nodeVarName = constructorScope.getIdentifierOrThrow(node)»
						«val edges = edgesPerNode.get(node)»
						«IF !edges.empty»
						
						«FOR pathElement : edges.keySet»
							«nodeVarName».addOutgoingEdge("«StringEscapeUtils.escapeJava(pathElement)»", «constructorScope.getIdentifierOrThrow(edges.get(pathElement))»);
						«ENDFOR»
						«ENDIF»
					«ENDFOR»
				}
			}
		'''
	}
	
	private def StringConcatenationClient representAsList(DottedPath path) {
		'''«Arrays».asList(«path.stream.map[StringEscapeUtils.escapeJava(it)].collect(Collectors.joining("\", \"", "\"", "\""))»)'''
	}
	
	private def void buildLabelGraph(RDataType currentNode, Map<RDataType, Map<DottedPath, String>> labelsPerNode, Map<RDataType, Map<String, RDataType>> edgesPerNode) {
		if (labelsPerNode.containsKey(currentNode)) {
			// Circular reference: we already computed this node.
			return
		}
		val labels = newLinkedHashMap
		labelsPerNode.put(currentNode, labels)
		val edges = newLinkedHashMap
		edgesPerNode.put(currentNode, edges)
		for (attr : currentNode.allAttributes) {
			val attrPath = DottedPath.of(attr.name)

			// 1. Register labels on the type of this attribute
			var attrType = attr.RMetaAnnotatedType.RType
			val t = if (attrType instanceof RChoiceType) {
				attrType.asRDataType
			} else {
				attrType
			}
			if (t instanceof RDataType) {
				edges.put(attr.name, t)
				buildLabelGraph(t, labelsPerNode, edgesPerNode)
			}

			// 2. Register label annotations
			attr.allLabelAnnotations.forEach[
				registerLabelAnnotation(it, attrPath, labels)
			]
		}
	}
	
	private def void pruneLabelGraph(Map<RDataType, Map<DottedPath, String>> labelsPerNode, Map<RDataType, Map<String, RDataType>> edgesPerNode) {
		// For each possible path in the graph, see if it is possible to reach any label.
		// If not, prune those nodes.
		val nodes = new HashSet(labelsPerNode.keySet)
		
		val nodesWithReachableLabels = newHashSet
		nodesWithReachableLabels.addAll(nodes.filter[!labelsPerNode.get(it).empty])
		
		var anyReachableNodesFoundInIteration = true
		while (anyReachableNodesFoundInIteration) {
			anyReachableNodesFoundInIteration = false
			for (node : nodes) {
				if (!nodesWithReachableLabels.contains(node)) {
					val hasEdgeToNodeWithReachableLabel = edgesPerNode.get(node).values.exists[nodesWithReachableLabels.contains(it)]
					if (hasEdgeToNodeWithReachableLabel) {
						nodesWithReachableLabels.add(node)
						anyReachableNodesFoundInIteration = true
					}
				}
			}
		}
		// prune
		for (node : nodes) {
			if (!nodesWithReachableLabels.contains(node)) {
				labelsPerNode.remove(node)
				edgesPerNode.remove(node)
				edgesPerNode.values.forEach[
					it.entrySet.removeIf[
						value == node
					]
				]
			}
		}
	}
	
	private def void registerLabelAnnotation(LabelAnnotation ann, DottedPath attrPath, Map<DottedPath, String> labels) {
		evaluateAnnotationPathExpression(attrPath, ann.path)
			.forEach[
				labels.put(it, ann.label)
			]
	}
	
	private def List<DottedPath> evaluateAnnotationPathExpression(DottedPath root, AnnotationPathExpression expr) {
		if (expr === null) {
			#[root]
		} else {
			annotationPathUtil.fold(
				expr,
				[a|#[root.child(a.name)]],
				[a|#[root]],
				[r,p|r.map[child(p.attribute.name)]],
				[r,dp|
					val rawType = typeProvider.getRMetaAnnotatedType(dp.receiver).RType
					val t = if (rawType instanceof RChoiceType) {
							rawType.asRDataType
						} else {
							rawType
						}
					if (t instanceof RDataType) {
						r.flatMap[p|
							deepPathUtil.findDeepFeaturePaths(t, rObjectFactory.buildRAttribute(dp.attribute))
								.map[deepPath|
									p.concat(DottedPath.of(deepPath.map[name]))
								]
						].toList
					} else {
						#[]
					}
				]
			)
		}
	}
	
}