package com.regnosys.rosetta.generator.java.expression

import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaPrimitiveType
import java.util.Map

import jakarta.inject.Inject
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.utils.DeepFeatureCallUtil
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.rosetta.expression.ExistsModifier
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService
import com.regnosys.rosetta.generator.java.statement.builder.JavaIfThenElseBuilder
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import java.util.HashSet
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.types.RChoiceType
import static extension com.regnosys.rosetta.types.RMetaAnnotatedType.*
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.regnosys.rosetta.generator.java.statement.builder.JavaLiteral
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope
import com.regnosys.rosetta.generator.java.RObjectJavaClassGenerator
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope
import com.regnosys.rosetta.rosetta.simple.Choice
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil

class DeepPathUtilGenerator extends RObjectJavaClassGenerator<RDataType, JavaClass<?>> {
	@Inject extension JavaTypeTranslator
	@Inject extension DeepFeatureCallUtil
	@Inject extension ExpressionGenerator
	@Inject extension TypeCoercionService
	@Inject extension JavaIdentifierRepresentationService
	@Inject JavaTypeUtil typeUtil
	@Inject extension RObjectFactory
	
	override protected streamObjects(RosettaModel model) {
		model.elements.stream.filter[it instanceof Data].map[it as Data].map[buildRDataType].filter[
			isEligibleForDeepFeatureCall || (EObject instanceof Choice && (EObject as Choice).buildRChoiceType.hasImpliedKey)
		]
	}
	override protected createTypeRepresentation(RDataType choiceType) {
		choiceType.toDeepPathUtilJavaClass
	}
	override protected generateClass(RDataType choiceType, JavaClass<?> javaClass, String version, JavaClassScope classScope) {
		val deepFeatures = choiceType.findDeepFeatures
		val dependencies = new HashSet<JavaClass<?>>()
		val recursiveDeepFeaturesMap = choiceType.allAttributes.toMap([it], [
			val attrType = it.RMetaAnnotatedType.RType
			deepFeatures.toMap([it], [
				var t = attrType
				if (t instanceof RChoiceType) {
					t = t.asRDataType
				}
				if (t instanceof RDataType) {
					if (t.findDeepFeatureMap.containsKey(it.name)) {
						dependencies.add(t.toDeepPathUtilJavaClass)
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
				
				@«javax.inject.Inject»
				public «javaClass»(«FOR dependency : dependencies SEPARATOR ', '»«dependency» «classScope.getIdentifierOrThrow(dependency.toDependencyInstance)»«ENDFOR») {
					«FOR dependency : dependencies»
					this.«classScope.getIdentifierOrThrow(dependency.toDependencyInstance)» = «classScope.getIdentifierOrThrow(dependency.toDependencyInstance)»;
					«ENDFOR»
				}
				
				«ENDIF»
				«FOR deepFeature : deepFeatures»
					«val deepFeatureScope = classScope.createMethodScope('''choose«deepFeature.name.toFirstUpper»''')»
					«val inputParameter = new JavaVariable(deepFeatureScope.createUniqueIdentifier(choiceType.name.toFirstLower), choiceType.toJavaReferenceType)»
					«val methodBody = deepFeatureToStatement(choiceType, inputParameter, deepFeature, recursiveDeepFeaturesMap, deepFeatureScope.bodyScope)»
					public «methodBody.expressionType» choose«deepFeature.name.toFirstUpper»(«inputParameter.expressionType» «inputParameter») «methodBody.completeAsReturn»
					
				«ENDFOR»
 			    «IF choiceType.EObject instanceof Choice && (choiceType.EObject as Choice).buildRChoiceType.hasImpliedKey»
					«val chooseKeyScope = classScope.createMethodScope('chooseKey')»
					«val keyInputParam = new JavaVariable(chooseKeyScope.createUniqueIdentifier(choiceType.name.toFirstLower), choiceType.toJavaReferenceType)»
					«val keyMethodBody = chooseKeyToStatement(choiceType, keyInputParam, chooseKeyScope.bodyScope)»
					public «keyMethodBody.expressionType» chooseKey(«keyInputParam.expressionType» «keyInputParam») «keyMethodBody.completeAsReturn»
					
				«ENDIF»
			}
		'''
	}

	private def JavaStatementBuilder chooseKeyToStatement(RDataType choiceType, JavaVariable inputParameter, JavaStatementScope scope) {
		val attrs = choiceType.allAttributes.toList
		val resultType = typeUtil.STRING
		var JavaStatementBuilder acc = JavaLiteral.NULL
		for (a : attrs.reverseView) {
			val currAcc = acc
			acc = inputParameter
					.attributeCall(choiceType.withNoMeta, a, false, a.toMetaJavaType, scope)
					.declareAsVariable(true, a.name.toFirstLower, scope)
					.mapExpression[attrVar|
						attrVar.exists(ExistsModifier.NONE, scope)
							.collapseToSingleExpression(scope)
							.addCoercions(JavaPrimitiveType.BOOLEAN, scope)
						.mapExpression[
							val hasAttrMeta = a.RMetaAnnotatedType.hasAttributeMeta
							val lambdaScope1 = scope.lambdaScope
							val lp1 = lambdaScope1.createUniqueIdentifier("a")
							val lambdaScope2 = scope.lambdaScope
							val lp2 = lambdaScope2.createUniqueIdentifier("a")
							val lambdaScope3 = scope.lambdaScope
							val lp3 = lambdaScope3.createUniqueIdentifier("a")
 						val keyExpr = attrVar.mapExpression[v |
								if (hasAttrMeta) {
									JavaExpression.from('''«v».map("getValue", «lp1»->«lp1».getValue()).map("getMeta", «lp2»->«lp2».getMeta()).map("getExternalKey", «lp3»->«lp3».getExternalKey()).get()''', resultType)
								} else {
									JavaExpression.from('''«v».map("getMeta", «lp1»->«lp1».getMeta()).map("getExternalKey", «lp2»->«lp2».getExternalKey()).get()''', resultType)
								}
							]
							new JavaIfThenElseBuilder(it, keyExpr, currAcc, typeUtil)
						]
					]
		}
		acc.addCoercions(resultType, scope)
	}

	private def JavaStatementBuilder deepFeatureToStatement(RDataType choiceType, JavaVariable inputParameter, RAttribute deepFeature, Map<RAttribute, Map<RAttribute, Boolean>> recursiveDeepFeaturesMap, JavaStatementScope scope) {
		val attrs = choiceType.allAttributes.toList
		val deepFeatureType = deepFeature.toMetaJavaType
		var JavaStatementBuilder acc = JavaLiteral.NULL
		for (a : attrs.reverseView) {
			val currAcc = acc
			acc = inputParameter
					.attributeCall(choiceType.withNoMeta, a, false, a.toMetaJavaType, scope)
					.declareAsVariable(true, a.name.toFirstLower, scope)
					.mapExpression[attrVar|
						attrVar.exists(ExistsModifier.NONE, scope)
							.collapseToSingleExpression(scope)
							.addCoercions(JavaPrimitiveType.BOOLEAN, scope)
							.mapExpression[
								val deepFeatureExpr = if (deepFeature.match(a)) {
									attrVar
								} else {
									val metaRType = a.RMetaAnnotatedType
									var attrType = metaRType.RType
									if (attrType instanceof RChoiceType) {
										attrType = attrType.asRDataType
									}
									val needsToGoDownDeeper = recursiveDeepFeaturesMap.get(a).get(deepFeature)
									val actualFeature = if (needsToGoDownDeeper || !(attrType instanceof RDataType)) {
										deepFeature
									} else {
										(attrType as RDataType).allAttributes.findFirst[name.equals(deepFeature.name)]
									}
									attrVar.attributeCall(metaRType, actualFeature, needsToGoDownDeeper, deepFeatureType, scope)
								}
								new JavaIfThenElseBuilder(it, deepFeatureExpr, currAcc, typeUtil)
							]
					]
		}
		val resultType = deepFeature.toMetaJavaType
		acc.addCoercions(resultType, scope)
	}
}
