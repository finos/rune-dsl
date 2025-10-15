package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.RosettaReport
import com.regnosys.rosetta.types.RObjectFactory
import jakarta.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.types.RFunction
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import java.util.Map
import com.regnosys.rosetta.types.RDataType
import com.rosetta.util.DottedPath
import com.regnosys.rosetta.rosetta.simple.LabelAnnotation
import com.regnosys.rosetta.rosetta.RosettaRule
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
import com.regnosys.rosetta.rules.RuleReferenceService
import com.regnosys.rosetta.rosetta.simple.RuleReferenceAnnotation
import com.regnosys.rosetta.generator.java.RObjectJavaClassGenerator
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope
import com.regnosys.rosetta.rosetta.RosettaModel

class LabelProviderGenerator extends RObjectJavaClassGenerator<RFunction, RGeneratedJavaClass<?>> {
	@Inject RObjectFactory rObjectFactory
	@Inject RosettaTypeProvider typeProvider
	@Inject JavaTypeTranslator typeTranslator
	@Inject DeepFeatureCallUtil deepPathUtil
	@Inject LabelProviderGeneratorUtil util
	@Inject RuleReferenceService ruleService
	@Inject AnnotationPathExpressionUtil annotationPathUtil
	
	
	override protected streamObjects(RosettaModel model) {
		model.elements.stream.map[
			if (it instanceof Function && util.shouldGenerateLabelProvider(it as Function)) {
				return rObjectFactory.buildRFunction(it as Function)
			} else if (it instanceof RosettaReport) {
				return rObjectFactory.buildRFunction(it)
			}
			null
		].filter[it !== null]
	}
	override protected createTypeRepresentation(RFunction function) {
		typeTranslator.toLabelProviderJavaClass(function)
	}
	override protected generateClass(RFunction function, RGeneratedJavaClass<?> labelClass, String version, JavaClassScope classScope) {
		val functionOrigin = function.EObject
		val attributeToRuleMap = if (functionOrigin instanceof RosettaReport) {
			ruleService.traverse(
				functionOrigin.ruleSource,
				function.output.RMetaAnnotatedType.RType as RDataType,
				newHashMap,
				[map,context|
					if (context.rule !== null && context.rule.identifier !== null) {
						val origin = context.ruleOrigin
						if (origin instanceof RuleReferenceAnnotation) {
							if (origin.path === null) {
								map.put(context.targetAttribute, context.rule)
							}
						}
					}
					map
				]
			)
		} else {
			emptyMap
		}
		
		val constructorScope = classScope.createMethodScope("constructor")

		val Map<RDataType, Map<DottedPath, String>> labelsPerNode = newLinkedHashMap
		val edgesPerNode = newLinkedHashMap
		val outputType = function.output.RMetaAnnotatedType.RType
		val startNode = if (outputType instanceof RChoiceType) {
			outputType.asRDataType
		} else {
			outputType
		}
		if (startNode instanceof RDataType) {
			buildLabelGraph(startNode, labelsPerNode, edgesPerNode, attributeToRuleMap)
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
	
	private def void buildLabelGraph(RDataType currentNode, Map<RDataType, Map<DottedPath, String>> labelsPerNode, Map<RDataType, Map<String, RDataType>> edgesPerNode, Map<RAttribute, RosettaRule> attributeToRuleMap) {
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
				buildLabelGraph(t, labelsPerNode, edgesPerNode, attributeToRuleMap)
			}
			
			// 2. Register legacy `as` annotations from rule references
			val ruleRef = attributeToRuleMap.get(attr)
			if (ruleRef !== null) {
				registerLegacyRuleAsLabel(ruleRef, attrPath, labels)
			}
			
			// 3. Register label annotations
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
	private def void registerLegacyRuleAsLabel(RosettaRule rule, DottedPath attrPath, Map<DottedPath, String> labels) {
		if (rule.identifier !== null) {
			labels.put(attrPath, rule.identifier)
		}
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