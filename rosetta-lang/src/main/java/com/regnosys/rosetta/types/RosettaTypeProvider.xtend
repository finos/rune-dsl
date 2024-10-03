package com.regnosys.rosetta.types

import com.regnosys.rosetta.RosettaEcoreUtil
import com.regnosys.rosetta.cache.IRequestScopedCache
import com.regnosys.rosetta.rosetta.RosettaAttributeReference
import com.regnosys.rosetta.rosetta.RosettaAttributeReferenceSegment
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaDataReference
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaRule
import com.regnosys.rosetta.rosetta.RosettaSymbol
import com.regnosys.rosetta.rosetta.RosettaTypedFeature
import com.regnosys.rosetta.rosetta.TypeParameter
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.ClosureParameter
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation
import com.regnosys.rosetta.rosetta.expression.DefaultOperation
import com.regnosys.rosetta.rosetta.expression.DistinctOperation
import com.regnosys.rosetta.rosetta.expression.EqualityOperation
import com.regnosys.rosetta.rosetta.expression.FilterOperation
import com.regnosys.rosetta.rosetta.expression.FirstOperation
import com.regnosys.rosetta.rosetta.expression.FlattenOperation
import com.regnosys.rosetta.rosetta.expression.JoinOperation
import com.regnosys.rosetta.rosetta.expression.LastOperation
import com.regnosys.rosetta.rosetta.expression.ListLiteral
import com.regnosys.rosetta.rosetta.expression.LogicalOperation
import com.regnosys.rosetta.rosetta.expression.MapOperation
import com.regnosys.rosetta.rosetta.expression.MaxOperation
import com.regnosys.rosetta.rosetta.expression.MinOperation
import com.regnosys.rosetta.rosetta.expression.OneOfOperation
import com.regnosys.rosetta.rosetta.expression.ReduceOperation
import com.regnosys.rosetta.rosetta.expression.ReverseOperation
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import com.regnosys.rosetta.rosetta.expression.SortOperation
import com.regnosys.rosetta.rosetta.expression.SumOperation
import com.regnosys.rosetta.rosetta.expression.SwitchOperation
import com.regnosys.rosetta.rosetta.expression.ThenOperation
import com.regnosys.rosetta.rosetta.expression.ToDateOperation
import com.regnosys.rosetta.rosetta.expression.ToDateTimeOperation
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation
import com.regnosys.rosetta.rosetta.expression.ToIntOperation
import com.regnosys.rosetta.rosetta.expression.ToNumberOperation
import com.regnosys.rosetta.rosetta.expression.ToStringOperation
import com.regnosys.rosetta.rosetta.expression.ToTimeOperation
import com.regnosys.rosetta.rosetta.expression.ToZonedDateTimeOperation
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.simple.AnnotationRef
import com.regnosys.rosetta.rosetta.simple.AssignPathRoot
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import com.regnosys.rosetta.utils.ImplicitVariableUtil
import com.regnosys.rosetta.utils.RosettaExpressionSwitch
import java.math.BigInteger
import java.util.List
import java.util.Map
import java.util.Optional
import javax.inject.Inject
import javax.inject.Provider
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.naming.IQualifiedNameProvider
import com.regnosys.rosetta.rosetta.expression.SwitchCase
import static extension com.regnosys.rosetta.types.RMetaAnnotatedType.withEmptyMeta
import static extension com.regnosys.rosetta.types.RMetaAnnotatedType.withMeta

class RosettaTypeProvider extends RosettaExpressionSwitch<RMetaAnnotatedType, Map<EObject, RMetaAnnotatedType>> {
	public static String EXPRESSION_RTYPE_CACHE_KEY = RosettaTypeProvider.canonicalName + ".EXPRESSION_RTYPE"


	@Inject extension RosettaOperators
	@Inject IQualifiedNameProvider qNames
	@Inject RosettaEcoreUtil extensions
	@Inject extension ImplicitVariableUtil
	@Inject extension TypeSystem
	@Inject extension TypeFactory
	@Inject extension RBuiltinTypeService
	@Inject IRequestScopedCache cache
	@Inject extension RObjectFactory
	@Inject extension ExpectedTypeProvider

	def RMetaAnnotatedType getRMetaAnnotatedType(RosettaExpression expression) {
		expression.safeRType(newHashMap)
	}
	def RMetaAnnotatedType getRTypeOfFeature(RosettaFeature feature, EObject context) {
		feature.safeRType(context, newHashMap)
	}
	def RMetaAnnotatedType getRTypeOfSymbol(RosettaSymbol feature, EObject context) {
		feature.safeRType(context, newHashMap)
	}
	def RMetaAnnotatedType getRTypeOfSymbol(AssignPathRoot feature) {
		feature.getRTypeOfSymbol(null)
	}
	def RMetaAnnotatedType getRTypeOfSymbol(RosettaCallableWithArgs feature) {
		feature.getRTypeOfSymbol(null)
	}
	def RType getRTypeOfAttributeReference(RosettaAttributeReferenceSegment seg) {
		switch seg {
			RosettaAttributeReference: seg.attribute.typeCall.typeCallToRType
			RosettaDataReference: {
				if (extensions.isResolved(seg.data)) {
					return seg.data.buildRDataType
				} else {
					NOTHING
				}
			}
		}
	}
	def Iterable<? extends RosettaFeature> findFeaturesOfImplicitVariable(EObject context) {
		return extensions.allFeatures(typeOfImplicitVariable(context).RType, context)
	}
	
	def List<RMetaAttribute> getRMetaAttributesOfSymbol(RosettaSymbol symbol) {
		if (symbol instanceof Annotated) {
			return symbol.annotations.RMetaAttributes
		}
		#[]
	}
	
	def List<RMetaAttribute> getRMetaAttributesOfFeature(RosettaFeature feature) {
		if (feature instanceof Annotated) {
			return feature.annotations.RMetaAttributes
		}
		#[]
	} 

	def List<RMetaAttribute> getRMetaAttributes(List<AnnotationRef> annotations) {
		annotations
			.filter[it.annotation.name.equals("metadata") && it.attribute !== null]
			.map[new RMetaAttribute(it.attribute.name, it.attribute.RTypeOfSymbol.RType, it.attribute)]
			.toList
	}
	
	
	private def RMetaAnnotatedType safeRType(RosettaSymbol symbol, EObject context,Map<EObject, RMetaAnnotatedType> cycleTracker) {
		if (!extensions.isResolved(symbol)) {
			return NOTHING.withEmptyMeta
		}
		switch symbol {
			RosettaFeature: {
				safeRType(symbol as RosettaFeature, context, cycleTracker)
			}
			ClosureParameter: {
				val setOp = symbol.function.eContainer as RosettaFunctionalOperation
				if(setOp !== null) {
					setOp.argument.safeRType(cycleTracker)
				} else
					MISSING.withEmptyMeta
			}
			RosettaEnumeration: { // @Compat: RosettaEnumeration should not be a RosettaSymbol.
				symbol.buildREnumType.withMeta(symbol.RMetaAttributesOfSymbol)
			}
			Function: {
				if (symbol.output !== null) {
					safeRType(symbol.output as RosettaFeature, context, cycleTracker)
				} else {
					MISSING.withEmptyMeta
				}
			}
			RosettaRule: {
				if (symbol.expression !== null) {
					safeRType(symbol.expression, cycleTracker)
				} else {
					MISSING.withEmptyMeta
				}
			}
			RosettaExternalFunction: {
				symbol.typeCall.typeCallToRType.withEmptyMeta
			}
			ShortcutDeclaration: {
				cycleTracker.put(symbol, null)
				val type = symbol.expression.safeRType(cycleTracker)
				cycleTracker.put(symbol, type)
				type
			}
			TypeParameter: {
				symbol.typeCall.typeCallToRType.withEmptyMeta
			}
		}
	}
	private def RMetaAnnotatedType safeRType(RosettaFeature feature, EObject context, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		if (!extensions.isResolved(feature)) {
			return NOTHING.withEmptyMeta
		}
		switch (feature) {
			RosettaTypedFeature: {
				val featureType = if (feature.typeCall === null) {
						NOTHING.withEmptyMeta
					} else {
						feature.typeCall.typeCallToRType.withMeta(feature.RMetaAttributesOfFeature)
					}
				featureType
			}
			RosettaEnumValue: {
				if (context instanceof RosettaFeatureCall) {
					context.receiver.safeRType(cycleTracker)
				} else {
					context.expectedTypeFromContainer ?: NOTHING.withEmptyMeta
				}
			}
			default:
				new RErrorType("Cannot infer type of feature.").withEmptyMeta
		}
	}
	
	private def RMetaAnnotatedType safeRType(RosettaExpression expression, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		getRTypeFromCache(EXPRESSION_RTYPE_CACHE_KEY, expression, [
			if (cycleTracker.containsKey(expression)) {
				val computed = cycleTracker.get(expression)
				if (computed === null) {
					return new RErrorType('''Can't infer type due to a cyclic reference of «qNames.getFullyQualifiedName(expression)»''').withEmptyMeta
				} else {
					return computed
				}
			}
			if (!extensions.isResolved(expression)) {
				return NOTHING.withEmptyMeta
			}
			doSwitch(expression, cycleTracker)
		])
	}
	
	private def RMetaAnnotatedType getRTypeFromCache(String cacheKey, EObject object, Provider<RMetaAnnotatedType> typeProvider) {
		if (object === null) {
			return typeProvider.get()
		}
		cache.get(cacheKey -> object, typeProvider)
	}
	
	def typeOfImplicitVariable(EObject context) {
		safeTypeOfImplicitVariable(context, newHashMap)
	}
	
	private def RMetaAnnotatedType safeTypeOfImplicitVariable(EObject context, Map<EObject,RMetaAnnotatedType> cycleTracker) {
		val definingContainer = context.findContainerDefiningImplicitVariable
		definingContainer.map [
			if (it instanceof Data) {
				buildRDataType.withMeta(it.annotations.RMetaAttributes)
			} else if (it instanceof RosettaFunctionalOperation) {
				safeRType(argument, cycleTracker)
			} else if (it instanceof RosettaRule) {
				input?.typeCallToRType.withEmptyMeta ?: MISSING.withEmptyMeta
			} else if (it instanceof SwitchCase) {
				guard.choiceOptionGuard.RTypeOfSymbol
			}
		].orElse(MISSING.withEmptyMeta)
	}
	
	private def caseBinaryOperation(RosettaBinaryOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		val left = expr.left
		var leftType = left.safeRType(cycleTracker)
		if (leftType.RType instanceof RErrorType) {
			return NOTHING.withEmptyMeta
		}
		val right = expr.right
		var rightType = right.safeRType(cycleTracker)
		if (rightType.RType instanceof RErrorType) {
			return NOTHING.withEmptyMeta
		}
		expr.operator.resultType(leftType.RType, rightType.RType).withEmptyMeta
	}
	
	override protected caseAbsentOperation(RosettaAbsentExpression expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN.withEmptyMeta
	}
	
	override protected caseAddOperation(ArithmeticOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseAndOperation(LogicalOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseAsKeyOperation(AsKeyOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseBooleanLiteral(RosettaBooleanLiteral expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN.withEmptyMeta
	}
	
	override protected caseChoiceOperation(ChoiceOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN.withEmptyMeta
	}
	
	override protected caseConditionalExpression(RosettaConditionalExpression expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		val ifT = expr.ifthen.safeRType(cycleTracker)
		if (ifT.RType instanceof RErrorType) {
			return NOTHING.withEmptyMeta
		}
		val elseT = expr.elsethen.safeRType(cycleTracker)
		if (elseT.RType instanceof RErrorType) {
			return NOTHING.withEmptyMeta
		}
		val joined = join(ifT, elseT)
		if (joined == ANY) {
			new RErrorType('''Types `«ifT»` and `«elseT»` do not have a common supertype.''').withEmptyMeta
		} else {
			joined
		}
	}
	
	override protected caseContainsOperation(RosettaContainsExpression expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseDefaultOperation(DefaultOperation expr,  Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseCountOperation(RosettaCountOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		constrainedInt(Optional.empty(), Optional.of(BigInteger.ZERO), Optional.empty()).withEmptyMeta
	}
	
	override protected caseDisjointOperation(RosettaDisjointExpression expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseDistinctOperation(DistinctOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseDivideOperation(ArithmeticOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseEqualsOperation(EqualityOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseExistsOperation(RosettaExistsExpression expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN.withEmptyMeta
	}
	
	override protected caseFeatureCall(RosettaFeatureCall expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		val feature = expr.feature
		if (!extensions.isResolved(feature)) {
			return NOTHING.withEmptyMeta
		}
		if (feature instanceof RosettaEnumValue) {
			expr.receiver.safeRType(cycleTracker)
		} else {
			feature.safeRType(expr, cycleTracker)
		}
	}
	
	override protected caseDeepFeatureCall(RosettaDeepFeatureCall expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		val feature = expr.feature
		if (!extensions.isResolved(feature)) {
			return NOTHING.withEmptyMeta
		}
		(feature as RosettaFeature).safeRType(expr, cycleTracker)
	}
	
	override protected caseFilterOperation(FilterOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseFirstOperation(FirstOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseFlattenOperation(FlattenOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseGreaterThanOperation(ComparisonOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseGreaterThanOrEqualOperation(ComparisonOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseImplicitVariable(RosettaImplicitVariable expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		safeTypeOfImplicitVariable(expr, cycleTracker)
	}
	
	override protected caseIntLiteral(RosettaIntLiteral expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		constrainedInt(if (expr.value.signum >= 0) expr.value.toString.length else expr.value.toString.length - 1, expr.value, expr.value).withEmptyMeta
	}
	
	override protected caseJoinOperation(JoinOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		UNCONSTRAINED_STRING.withEmptyMeta
	}
	
	override protected caseLastOperation(LastOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseLessThanOperation(ComparisonOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseLessThanOrEqualOperation(ComparisonOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseListLiteral(ListLiteral expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		val types = expr.elements.map[RMetaAnnotatedType].filter[it !== null]
		val joined = types.joinMetaAnnotatedType
		val unique = newLinkedHashSet(types)
		val StringConcatenationClient failedList = '''«FOR t: unique.take(unique.size-1) SEPARATOR ", "»`«t»`«ENDFOR» and `«unique.last»`'''
		if (joined == ANY) {
			new RErrorType('''Types «failedList» do not have a common supertype.''').withEmptyMeta
		} else {
			joined
		}
	}
	
	override protected caseMapOperation(MapOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.function?.body?.safeRType(cycleTracker)
	}
	
	override protected caseMaxOperation(MaxOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseMinOperation(MinOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseMultiplyOperation(ArithmeticOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseNotEqualsOperation(EqualityOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseNumberLiteral(RosettaNumberLiteral expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		if (expr.value === null) { // In case of a parse error
			return NOTHING.withEmptyMeta
		}
		constrainedNumber(expr.value.toPlainString.replaceAll("\\.|\\-", "").length, Math.max(0, expr.value.scale), expr.value, expr.value).withEmptyMeta
	}
	
	override protected caseOneOfOperation(OneOfOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN.withEmptyMeta
	}
	
	override protected caseOnlyElementOperation(RosettaOnlyElement expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseOnlyExists(RosettaOnlyExistsExpression expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN.withEmptyMeta
	}
	
	override protected caseOrOperation(LogicalOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseReduceOperation(ReduceOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.function?.body?.safeRType(cycleTracker)
	}
	
	override protected caseReverseOperation(ReverseOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseSortOperation(SortOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseStringLiteral(RosettaStringLiteral expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		constrainedString(expr.value.length, expr.value.length).withEmptyMeta
	}
	
	override protected caseSubtractOperation(ArithmeticOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseSumOperation(SumOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseSymbolReference(RosettaSymbolReference expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		if (expr.symbol instanceof RosettaExternalFunction) {
			val fun = expr.symbol as RosettaExternalFunction
			val returnType = fun.safeRType(expr, cycleTracker)
			// TODO: this is a hack
			// Generic return type for number type e.g. Min(1,2) or Max(2,6)
			val argTypes = expr.args.map[safeRType(cycleTracker)]
			if (argTypes.forall[isSubtypeOf(returnType)]) {
				argTypes.joinMetaAnnotatedType
			} else {
				returnType
			}
		} else {
			safeRType(expr.symbol, expr, cycleTracker)
		}
	}
	
	override protected caseThenOperation(ThenOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.function?.body?.safeRType(cycleTracker)
	}
	
	override protected caseToEnumOperation(ToEnumOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.enumeration.buildREnumType.withEmptyMeta
	}
	
	override protected caseToIntOperation(ToIntOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		UNCONSTRAINED_INT.withEmptyMeta
	}
	
	override protected caseToNumberOperation(ToNumberOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		UNCONSTRAINED_NUMBER.withEmptyMeta
	}
	
	override protected caseToStringOperation(ToStringOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		UNCONSTRAINED_STRING.withEmptyMeta
	}
	
	override protected caseToTimeOperation(ToTimeOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		TIME.withEmptyMeta
	}
	
	override protected caseConstructorExpression(RosettaConstructorExpression expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		expr.typeCall.typeCallToRType.withEmptyMeta
	}
	
	override protected caseToDateOperation(ToDateOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		DATE.withEmptyMeta
	}
	
	override protected caseToDateTimeOperation(ToDateTimeOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		DATE_TIME.withEmptyMeta
	}
	
	override protected caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Map<EObject, RMetaAnnotatedType> cycleTracker) {
		ZONED_DATE_TIME.withEmptyMeta
	}
	
	override protected caseSwitchOperation(SwitchOperation expr, Map<EObject, RMetaAnnotatedType> context) {
		expr.cases
			.map[it.expression.RMetaAnnotatedType]
			.joinMetaAnnotatedType
 	}

}
 