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
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.types.RObjectFactory

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
			isEligibleForDeepFeatureCall || asChoiceType.map[hasImpliedKey].orElse(false)
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
		val hasImpliedKey = choiceType.asChoiceType.map[hasImpliedKey].orElse(false)
		if (hasImpliedKey) {
			// `metaChooseKey` delegates to the `metaChooseKey` of each nested choice option, so we depend on their utils.
			choiceType.allAttributes.map[RMetaAnnotatedType.RType].filter(RChoiceType).forEach[
				dependencies.add(asRDataType.toDeepPathUtilJavaClass)
			]
		}
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
				«IF hasImpliedKey»
					«val metaChooseKeyScope = classScope.createMethodScope('metaChooseKey')»
					«val keyInputParam = new JavaVariable(metaChooseKeyScope.createUniqueIdentifier(choiceType.name.toFirstLower), choiceType.toJavaReferenceType)»
					«val keyMethodBody = metaChooseKeyToStatement(choiceType, keyInputParam, metaChooseKeyScope.bodyScope)»
					public «keyMethodBody.expressionType» metaChooseKey(«keyInputParam.expressionType» «keyInputParam») «keyMethodBody.completeAsReturn»

					«val metaChooseGlobalKeyScope = classScope.createMethodScope('metaChooseGlobalKey')»
					«val globalKeyInputParam = new JavaVariable(metaChooseGlobalKeyScope.createUniqueIdentifier(choiceType.name.toFirstLower), choiceType.toJavaReferenceType)»
					«val globalKeyMethodBody = metaChooseGlobalKeyToStatement(choiceType, globalKeyInputParam, metaChooseGlobalKeyScope.bodyScope)»
					public «globalKeyMethodBody.expressionType» metaChooseGlobalKey(«globalKeyInputParam.expressionType» «globalKeyInputParam») «globalKeyMethodBody.completeAsReturn»

				«ENDIF»
			}
		'''
	}

	/**
	 * Generates the body of {@code metaChooseKey}, which returns the externalKey of whichever option of the choice is populated.
	 *
	 * A choice holds exactly one populated option, so we build a chain of if-then-else expressions: for each option,
	 * if it is populated then read its key, otherwise fall through to the next option.
	 */
	private def JavaStatementBuilder metaChooseKeyToStatement(RDataType choiceType, JavaVariable choiceParameter, JavaStatementScope scope) {
		buildOptionChain(choiceType, choiceParameter, [option, optionValue |
			optionValue.mapExpression[keyAccess(option, it, scope)]
		], scope).addCoercions(typeUtil.STRING, scope)
	}

	/**
	 * Generates the body of {@code metaChooseGlobalKey}, which returns the globalKey of whichever option of the choice is populated.
	 */
	private def JavaStatementBuilder metaChooseGlobalKeyToStatement(RDataType choiceType, JavaVariable choiceParameter, JavaStatementScope scope) {
		buildOptionChain(choiceType, choiceParameter, [option, optionValue |
			optionValue.mapExpression[globalKeyAccess(option, it, scope)]
		], scope).addCoercions(typeUtil.STRING, scope)
	}

	/**
	 * Builds the shared if-then-else chain over choice options (back-to-front). For each option, if it is populated
	 * the result is computed by {@code readStrategy(option, optionVar)}; otherwise the chain falls through to the
	 * next option. Callers are responsible for adding the final coercion to the desired result type.
	 */
	private def JavaStatementBuilder buildOptionChain(
		RDataType choiceType,
		JavaVariable inputParameter,
		(RAttribute, JavaStatementBuilder) => JavaStatementBuilder readStrategy,
		JavaStatementScope scope
	) {
		val options = choiceType.allAttributes.toList
		var JavaStatementBuilder elseBranch = JavaLiteral.NULL
		for (option : options.reverseView) {
			val nextOptionBranch = elseBranch
			elseBranch = inputParameter
					.attributeCall(choiceType.withNoMeta, option, false, option.toMetaJavaType, scope)
					.declareAsVariable(true, option.name.toFirstLower, scope)
					.mapExpression[optionValue |
						optionValue.exists(ExistsModifier.NONE, scope)
							.collapseToSingleExpression(scope)
							.addCoercions(JavaPrimitiveType.BOOLEAN, scope)
							.mapExpression[optionIsPopulated |
								val readExpr = readStrategy.apply(option, optionValue)
								new JavaIfThenElseBuilder(optionIsPopulated, readExpr, nextOptionBranch, typeUtil)
							]
					]
		}
		elseBranch
	}

	/**
	 * Builds the expression that reads the key from a single, populated choice option.
	 * <ul>
	 *   <li>A leaf option carries {@code [metadata key]} on its type, so we read it straight from the
	 *       option's metadata ({@code getMeta().getExternalKey()}).</li>
	 *   <li>A nested choice option has no key of its own, so we delegate to that choice's generated
	 *       {@code metaChooseKey(...)}, which in turn recurses into its populated option.</li>
	 * </ul>
	 */
	private def JavaStatementBuilder keyAccess(RAttribute option, JavaExpression optionMapper, JavaStatementScope scope) {
		val optionType = option.RMetaAnnotatedType.RType
		if (optionType instanceof RChoiceType) {
			val nestedUtil = optionType.asRDataType.toDeepPathUtilJavaClass
			val nestedChoice = scope.lambdaScope.createUniqueIdentifier("nestedChoice")
			return JavaExpression.from(
				'''«optionMapper».map("metaChooseKey", «nestedChoice» -> «scope.getIdentifierOrThrow(nestedUtil.toDependencyInstance)».metaChooseKey(«nestedChoice»)).get()''',
				typeUtil.STRING)
		}
		return leafKeyAccess(optionMapper, option.RMetaAnnotatedType.hasAttributeMeta, scope)
	}

	/**
	 * Reads the key from a leaf option via the shared meta-chain builder, which routes through
	 * {@code PojoPropertyUtil.toPojoPropertyName} to locate the correct getter.
	 * When the option additionally carries field-level metadata its value is unwrapped via
	 * {@code getValue()} before reading the metadata.
	 */
	private def JavaStatementBuilder leafKeyAccess(JavaStatementBuilder optionMapper, boolean hasFieldMeta, JavaStatementScope scope) {
		val keyChain = buildMetaChain("key", scope)
		if (hasFieldMeta) {
			val fieldWithMeta = scope.lambdaScope.createUniqueIdentifier("fieldWithMeta")
			return optionMapper.mapExpression[JavaExpression.from('''«it».map("getValue", «fieldWithMeta» -> «fieldWithMeta».getValue())«keyChain».get()''', typeUtil.STRING)]
		}
		return optionMapper.mapExpression[JavaExpression.from('''«it»«keyChain».get()''', typeUtil.STRING)]
	}

	private def JavaStatementBuilder globalKeyAccess(RAttribute option, JavaExpression optionMapper, JavaStatementScope scope) {
		val optionType = option.RMetaAnnotatedType.RType
		if (optionType instanceof RChoiceType) {
			val nestedUtil = optionType.asRDataType.toDeepPathUtilJavaClass
			val nestedChoice = scope.lambdaScope.createUniqueIdentifier("nestedChoice")
			return JavaExpression.from(
				'''«optionMapper».map("metaChooseGlobalKey", «nestedChoice» -> «scope.getIdentifierOrThrow(nestedUtil.toDependencyInstance)».metaChooseGlobalKey(«nestedChoice»)).get()''',
				typeUtil.STRING)
		}
		return leafGlobalKeyAccess(optionMapper, option.RMetaAnnotatedType.hasAttributeMeta, scope)
	}

	private def JavaStatementBuilder leafGlobalKeyAccess(JavaStatementBuilder optionMapper, boolean hasFieldMeta, JavaStatementScope scope) {
		val globalKeyChain = buildMetaChain("globalKey", scope)
		if (hasFieldMeta) {
			val fieldWithMeta = scope.lambdaScope.createUniqueIdentifier("fieldWithMeta")
			return optionMapper.mapExpression[JavaExpression.from('''«it».map("getValue", «fieldWithMeta» -> «fieldWithMeta».getValue())«globalKeyChain».get()''', typeUtil.STRING)]
		}
		return optionMapper.mapExpression[JavaExpression.from('''«it»«globalKeyChain».get()''', typeUtil.STRING)]
	}

	private def JavaStatementBuilder deepFeatureToStatement(RDataType choiceType, JavaVariable inputParameter, RAttribute deepFeature, Map<RAttribute, Map<RAttribute, Boolean>> recursiveDeepFeaturesMap, JavaStatementScope scope) {
		val deepFeatureType = deepFeature.toMetaJavaType
		buildOptionChain(choiceType, inputParameter, [option, attrVar |
			if (deepFeature.match(option)) {
				attrVar
			} else {
				val metaRType = option.RMetaAnnotatedType
				var attrType = metaRType.RType
				if (attrType instanceof RChoiceType) {
					attrType = attrType.asRDataType
				}
				val needsToGoDownDeeper = recursiveDeepFeaturesMap.get(option).get(deepFeature)
				val actualFeature = if (needsToGoDownDeeper || !(attrType instanceof RDataType)) {
					deepFeature
				} else {
					(attrType as RDataType).allAttributes.findFirst[name.equals(deepFeature.name)]
				}
				attrVar.attributeCall(metaRType, actualFeature, needsToGoDownDeeper, deepFeatureType, scope)
			}
		], scope).addCoercions(deepFeatureType, scope)
	}
}
