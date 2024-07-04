package com.regnosys.rosetta.generator.java.expression

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaPrimitiveType
import java.util.Map
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import javax.inject.Inject
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.utils.DeepFeatureCallUtil
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.rosetta.expression.ExistsModifier
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService
import com.regnosys.rosetta.generator.java.statement.builder.JavaIfThenElseBuilder
import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import java.util.HashSet
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable

class DeepPathUtilGenerator {
	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeTranslator
	@Inject extension DeepFeatureCallUtil
	@Inject extension RosettaExtensions
	@Inject extension ExpressionGenerator
	@Inject extension RosettaTypeProvider
	@Inject extension TypeCoercionService
	@Inject extension JavaIdentifierRepresentationService
	@Inject JavaTypeUtil typeUtil
	
	def void generate(IFileSystemAccess2 fsa, Data choiceType, String version) {
		val javaClass = choiceType.toDeepPathUtilJavaClass
		val fileName =  javaClass.canonicalName.withForwardSlashes + ".java"

		val topScope = new JavaScope(javaClass.packageName)

		val content = buildClass(javaClass.packageName, classBody(choiceType, javaClass, topScope), topScope)

		fsa.generateFile(fileName, content)
	}

	private def StringConcatenationClient classBody(
		Data choiceType,
		JavaClass<?> javaClass,
		JavaScope topScope
	) {		
		val classScope = topScope.classScope(javaClass.simpleName)
		val deepFeatures = new RDataType(choiceType).findDeepFeatures
		val dependencies = new HashSet<JavaClass<?>>()
		val recursiveDeepFeaturesMap = choiceType.allNonOverridesAttributes.toMap([it], [
			val attrType = it.RTypeOfFeature
			deepFeatures.toMap([it], [
				if (attrType instanceof RDataType) {
					if (attrType.findDeepFeatureMap.containsKey(it.name)) {
						dependencies.add(attrType.data.toDeepPathUtilJavaClass)
						return true
					}
				}
				return false
			])
		])
		dependencies.forEach[classScope.createIdentifier(it.toDependencyInstance, it.simpleName.toFirstLower)]
		'''
			public class «javaClass» {
				«IF !dependencies.empty»
				«FOR dependency : dependencies»
				private final «dependency» «classScope.getIdentifierOrThrow(dependency.toDependencyInstance)»;
				«ENDFOR»
				
				@«Inject»
				public «javaClass»(«FOR dependency : dependencies SEPARATOR ', '»«dependency» «classScope.getIdentifierOrThrow(dependency.toDependencyInstance)»«ENDFOR») {
					«FOR dependency : dependencies»
					this.«classScope.getIdentifierOrThrow(dependency.toDependencyInstance)» = «classScope.getIdentifierOrThrow(dependency.toDependencyInstance)»;
					«ENDFOR»
				}
				
				«ENDIF»
				«FOR deepFeature : deepFeatures»
				«val methodName = '''choose«deepFeature.name.toFirstUpper»'''»
				«val deepFeatureScope = classScope.methodScope(methodName)»
				«val inputParameter = new JavaVariable(deepFeatureScope.createUniqueIdentifier(choiceType.name.toFirstLower), new RDataType(choiceType).toJavaReferenceType)»
				«val methodBody = deepFeatureToStatement(choiceType, inputParameter, deepFeature, recursiveDeepFeaturesMap, deepFeatureScope)»
				public «methodBody.expressionType» «methodName»(«inputParameter.expressionType» «inputParameter») «methodBody.completeAsReturn»
				
				«ENDFOR»
			}
		'''
	}

	private def JavaStatementBuilder deepFeatureToStatement(Data choiceType, JavaVariable inputParameter, Attribute deepFeature, Map<Attribute, Map<Attribute, Boolean>> recursiveDeepFeaturesMap, JavaScope scope) {
		val attrs = choiceType.allNonOverridesAttributes
		val receiverType = new RDataType(choiceType)
		var JavaStatementBuilder acc = JavaExpression.NULL
		for (a : attrs.reverseView) {
			val currAcc = acc
			acc = inputParameter
					.featureCall(receiverType, a, false, scope, true)
					.declareAsVariable(true, a.name.toFirstLower, scope)
					.mapExpression[attrVar|
						attrVar.exists(ExistsModifier.NONE, scope)
							.collapseToSingleExpression(scope)
							.addCoercions(JavaPrimitiveType.BOOLEAN, scope)
							.mapExpression[
								val deepFeatureExpr = if (deepFeature.match(a)) {
									attrVar
								} else {
									val attrType = a.RTypeOfFeature
									val needsToGoDownDeeper = recursiveDeepFeaturesMap.get(a).get(deepFeature)
									val actualFeature = if (needsToGoDownDeeper || !(attrType instanceof RDataType)) {
										deepFeature
									} else {
										(attrType as RDataType).data.allNonOverridesAttributes.findFirst[name.equals(deepFeature.name)]
									}
									attrVar.featureCall(attrType, actualFeature, needsToGoDownDeeper, scope, true)
								}
								new JavaIfThenElseBuilder(it, deepFeatureExpr, currAcc, typeUtil)
							]
					]
		}
		val resultType = deepFeature.toExpandedAttribute.toMultiRegularJavaType
		acc.addCoercions(resultType, scope)
	}
}
