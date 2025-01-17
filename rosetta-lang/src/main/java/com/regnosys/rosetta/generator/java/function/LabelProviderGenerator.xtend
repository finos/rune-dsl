package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
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
import com.rosetta.model.lib.path.RosettaPath
import java.util.Map
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RDataType
import com.rosetta.util.DottedPath
import com.regnosys.rosetta.rosetta.simple.LabelAnnotation
import com.regnosys.rosetta.rosetta.RosettaRule
import com.regnosys.rosetta.rosetta.simple.AnnotationPathAttributeReference
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable
import com.regnosys.rosetta.rosetta.simple.AnnotationPath
import com.regnosys.rosetta.rosetta.simple.AnnotationDeepPath
import java.util.List
import com.regnosys.rosetta.rosetta.simple.AnnotationPathExpression
import com.regnosys.rosetta.utils.DeepFeatureCallUtil
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.types.RChoiceType
import java.util.HashMap
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions

class LabelProviderGenerator {
	@Inject extension ImportManagerExtension
	@Inject RObjectFactory rObjectFactory
	@Inject RosettaTypeProvider typeProvider
	@Inject JavaTypeTranslator typeTranslator
	@Inject DeepFeatureCallUtil deepPathUtil
	@Inject RosettaFunctionExtensions funcExtensions
	
	def void generateForFunctionIfApplicable(RootPackage root, IFileSystemAccess2 fsa, Function func) {
		if (!funcExtensions.getTransformAnnotations(func).isEmpty) {
			val rFunction = rObjectFactory.buildRFunction(func)
			generate(root, fsa, rFunction)
		}
	}
	def void generateForReport(RootPackage root, IFileSystemAccess2 fsa, RosettaReport report) {
		val rFunction = rObjectFactory.buildRFunction(report)
		generate(root, fsa, rFunction)
	}
	private def void generate(RootPackage root, IFileSystemAccess2 fsa, RFunction f) {
		val javaClass = typeTranslator.toLabelProviderJavaClass(f)
		val fileName = javaClass.canonicalName.withForwardSlashes + '.java'
		
		val topScope = new JavaScope(javaClass.packageName)
		val StringConcatenationClient classBody = classBody(f, javaClass, topScope)

		val content = buildClass(javaClass.packageName, classBody, topScope)
		fsa.generateFile(fileName, content)
	}
	
	private def StringConcatenationClient classBody(
		RFunction function,
		JavaClass<LabelProvider> javaClass,
		JavaScope topScope
	) {
		val className = topScope.createIdentifier(function, javaClass.simpleName)

		val labelMap = newLinkedHashMap
		gatherLabels(function.output.RMetaAnnotatedType.RType, null, labelMap)

		'''
			public class «className» implements «LabelProvider» {
				private final «Map»<«RosettaPath», «String»> labelMap;
				
				public «className»() {
					labelMap = new «HashMap»<>();
					
					«FOR path : labelMap.keySet»
						labelMap.put(«RosettaPath».valueOf("«path.withDots»"), «labelMap.get(path)»)
					«ENDFOR»
				}
				
				@Override
				public «String» getLabel(«RosettaPath» path) {
					RosettaPath normalized = path.toIndexless();
					return labelMap.get(normalized);
				}
			}
		'''
	}
	
	private def void gatherLabels(RType currentType, DottedPath currentPath, Map<DottedPath, String> labels) {
		val t = if (currentType instanceof RChoiceType) {
			currentType.asRDataType
		} else {
			currentType
		}
		if (t instanceof RDataType) {
			for (attr : t.allAttributes) {
				val attrPath = currentPath.child(attr.name)
				// 1. Register labels on the type of this attribute
				var attrType = attr.RMetaAnnotatedType.RType
				gatherLabels(attrType, attrPath, labels)
				
				// 2. Register legacy `as` annotations from rule references
				if (attr.ruleReference !== null) {
					registerLegacyRuleAsLabel(attr.ruleReference, attrPath, labels)
				}
				
				// 3. Register label annotations
				attr.labelAnnotations.forEach[
					registerLabelAnnotation(it, attrPath, labels)
				]
			}
		}
	}
	
	private def void registerLabelAnnotation(LabelAnnotation ann, DottedPath attrPath, Map<DottedPath, String> labels) {
		evaluateAnnotationPathExpression(#[attrPath], ann.path)
			.forEach[
				labels.put(it, ann.label)
			]
	}
	private def void registerLegacyRuleAsLabel(RosettaRule rule, DottedPath attrPath, Map<DottedPath, String> labels) {
		if (rule.identifier !== null) {
			labels.put(attrPath, rule.identifier)
		}
	}
	
	private def List<DottedPath> evaluateAnnotationPathExpression(List<DottedPath> currentPaths, AnnotationPathExpression expr) {
		if (expr === null) {
			currentPaths
		} else switch expr {
			AnnotationPathAttributeReference: currentPaths.map[child(expr.attribute.name)],
			RosettaImplicitVariable: currentPaths,
			AnnotationPath: currentPaths.evaluateAnnotationPathExpression(expr.receiver).map[child(expr.attribute.name)],
			AnnotationDeepPath: {
				val rawType = typeProvider.getRMetaAnnotatedType(expr.receiver).RType
				val t = if (rawType instanceof RChoiceType) {
						rawType.asRDataType
					} else {
						rawType
					}
				if (t instanceof RDataType) {
					currentPaths.evaluateAnnotationPathExpression(expr.receiver).flatMap[p|
						deepPathUtil.findDeepFeaturePaths(t, rObjectFactory.buildRAttribute(expr.attribute))
							.map[deepPath|
								p.concat(DottedPath.of(deepPath.map[name]))
							]
					].toList
				} else {
					#[]
				}
			} 
		}
	}
}