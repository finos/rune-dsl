package com.regnosys.rosetta.generator.java.function

import org.eclipse.xtext.generator.IFileSystemAccess2
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.RosettaReport
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.types.RObjectFactory
import javax.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.types.RFunction
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.rosetta.util.types.JavaClass
import com.rosetta.model.lib.functions.LabelProvider
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
import com.regnosys.rosetta.utils.ExternalAnnotationUtil
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.utils.AnnotationPathExpressionUtil

class LabelProviderGenerator {
	@Inject extension ImportManagerExtension
	@Inject RObjectFactory rObjectFactory
	@Inject RosettaTypeProvider typeProvider
	@Inject JavaTypeTranslator typeTranslator
	@Inject DeepFeatureCallUtil deepPathUtil
	@Inject LabelProviderGeneratorUtil util
	@Inject ExternalAnnotationUtil externalAnnotationUtil
	@Inject AnnotationPathExpressionUtil annotationPathUtil
	
	def void generateForFunctionIfApplicable(IFileSystemAccess2 fsa, Function func) {
		if (util.shouldGenerateLabelProvider(func)) {
			val rFunction = rObjectFactory.buildRFunction(func)
			generate(fsa, rFunction, emptyMap)
		}
	}
	def void generateForReport(IFileSystemAccess2 fsa, RosettaReport report) {
		val attributeToRuleMap = externalAnnotationUtil.getAllReportingRules(report)
			.entrySet()
			.stream()
			.collect(Collectors.toMap([e| e.getKey().getAttr()], [e| e.getValue()]));
		val rFunction = rObjectFactory.buildRFunction(report)
		generate(fsa, rFunction, attributeToRuleMap)
	}
	private def void generate(IFileSystemAccess2 fsa, RFunction f, Map<RAttribute, RosettaRule> attributeToRuleMap) {
		val javaClass = typeTranslator.toLabelProviderJavaClass(f)
		val fileName = javaClass.canonicalName.withForwardSlashes + '.java'
		
		val topScope = new JavaScope(javaClass.packageName)
		val StringConcatenationClient classBody = classBody(f, attributeToRuleMap, javaClass, topScope)

		val content = buildClass(javaClass.packageName, classBody, topScope)
		fsa.generateFile(fileName, content)
	}
	
	private def StringConcatenationClient classBody(
		RFunction function,
		Map<RAttribute, RosettaRule> attributeToRuleMap,
		JavaClass<LabelProvider> javaClass,
		JavaScope topScope
	) {
		val className = topScope.createIdentifier(function, javaClass.simpleName)
		val classScope = topScope.classScope(javaClass.simpleName)
		val constructorScope = classScope.methodScope("constructor")

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
			public class «className» extends «GraphBasedLabelProvider» {
				public «className»() {
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
			attr.labelAnnotations.forEach[
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