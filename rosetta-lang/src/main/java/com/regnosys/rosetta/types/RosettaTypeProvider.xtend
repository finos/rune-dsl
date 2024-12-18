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
import java.util.Optional
import javax.inject.Inject
import javax.inject.Provider
import org.eclipse.emf.ecore.EObject
import com.regnosys.rosetta.rosetta.expression.SwitchCase
import static extension com.regnosys.rosetta.types.RMetaAnnotatedType.withMeta
import com.regnosys.rosetta.rosetta.RosettaParameter
import com.regnosys.rosetta.types.builtin.RStringType
import com.regnosys.rosetta.types.builtin.RNumberType
import com.regnosys.rosetta.utils.OptionalUtil
import java.util.Map
import static extension com.regnosys.rosetta.types.RMetaAnnotatedType.withNoMeta
import com.regnosys.rosetta.rosetta.simple.Attribute


class RosettaTypeProvider extends RosettaExpressionSwitch<RMetaAnnotatedType, Map<RosettaSymbol, RMetaAnnotatedType>> {
	public static String EXPRESSION_RTYPE_CACHE_KEY = RosettaTypeProvider.canonicalName + ".EXPRESSION_RTYPE"


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
		return extensions.allFeatures(typeOfImplicitVariable(context), context)
	}
	
	def List<RMetaAttribute> getRMetaAttributesOfSymbol(RosettaSymbol symbol) {
		if (symbol instanceof Attribute) {
			if (symbol.isOverride) {
				return (extensions.getParentAttribute(symbol).RMetaAttributesOfSymbol + symbol.annotations.RMetaAttributes).toList
			}
		}
		if (symbol instanceof Annotated) {
			return symbol.annotations.RMetaAttributes
		}
		#[]
	}
	
	def List<RMetaAttribute> getRMetaAttributesOfFeature(RosettaFeature feature) {
		if (feature instanceof RosettaSymbol) {
			return feature.RMetaAttributesOfSymbol
		}
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
	
	private def RMetaAnnotatedType safeRType(RosettaSymbol symbol, EObject context, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		if (!extensions.isResolved(symbol)) {
			return NOTHING_WITH_NO_META
		}
		val existing = cycleTracker.get(symbol)
		if (existing !== null) {
			return existing
		}
		cycleTracker.put(symbol, NOTHING_WITH_NO_META)
		val result =
			switch symbol {
				RosettaFeature: {
					safeRType(symbol as RosettaFeature, context, cycleTracker)
				}
				RosettaParameter: {
					symbol.typeCall.typeCallToRType.withNoMeta
				}
				ClosureParameter: {
					val setOp = symbol.function.eContainer as RosettaFunctionalOperation
					if(setOp !== null) {
						setOp.argument.safeRType(cycleTracker)
					} else
						NOTHING_WITH_NO_META
				}
				RosettaEnumeration: { // @Compat: RosettaEnumeration should not be a RosettaSymbol.
					symbol.buildREnumType.withMeta(symbol.RMetaAttributesOfSymbol)
				}
				Function: {
					if (symbol.output !== null) {
						safeRType(symbol.output as RosettaFeature, context, cycleTracker)
					} else {
						NOTHING_WITH_NO_META
					}
				}
				RosettaRule: {
					if (symbol.expression !== null) {
						safeRType(symbol.expression, cycleTracker)
					} else {
						NOTHING_WITH_NO_META
					}
				}
				RosettaExternalFunction: {
					symbol.typeCall.typeCallToRType.withNoMeta
				}
				ShortcutDeclaration: {
					val type = symbol.expression.safeRType(cycleTracker)
					type
				}
				TypeParameter: {
					symbol.typeCall.typeCallToRType.withNoMeta
				}
			}
		cycleTracker.put(symbol, result)
		return result
	}
	private def RMetaAnnotatedType safeRType(RosettaFeature feature, EObject context, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		if (!extensions.isResolved(feature)) {
			return NOTHING_WITH_NO_META
		}
		switch (feature) {
			RosettaTypedFeature: {
				val featureType = if (feature.typeCall === null) {
						NOTHING_WITH_NO_META
					} else {
						feature.typeCall.typeCallToRType.withMeta(feature.RMetaAttributesOfFeature)
					}
				featureType
			}
			RosettaEnumValue: {
				if (context instanceof RosettaFeatureCall) {
					context.receiver.safeRType(cycleTracker)
				} else {
					context.expectedTypeFromContainer ?: NOTHING_WITH_NO_META
				}
			}
			default:
				NOTHING_WITH_NO_META
		}
	}
	
	private def RMetaAnnotatedType safeRType(RosettaExpression expression, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		if (expression === null) {
			return NOTHING_WITH_NO_META
		}
		getRTypeFromCache(EXPRESSION_RTYPE_CACHE_KEY, expression, [doSwitch(expression, cycleTracker)])
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
	
	private def RMetaAnnotatedType safeTypeOfImplicitVariable(EObject context, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		val definingContainer = context.findContainerDefiningImplicitVariable
		definingContainer.map [
			if (it instanceof Data) {
				buildRDataType.withNoMeta
			} else if (it instanceof RosettaFunctionalOperation) {
				safeRType(argument, cycleTracker)
			} else if (it instanceof RosettaRule) {
				input?.typeCallToRType?.withNoMeta ?: NOTHING_WITH_NO_META
			} else if (it instanceof SwitchCase) {
				guard.choiceOptionGuard.RTypeOfSymbol
			}
		].orElse(NOTHING_WITH_NO_META)
	}
	
	override protected caseAbsentOperation(RosettaAbsentExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseAddOperation(ArithmeticOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		val left = expr.left.safeRType(cycleTracker)
		val right = expr.right.safeRType(cycleTracker)
		if (left.isSubtypeOf(NOTHING_WITH_NO_META)) {
			NOTHING_WITH_NO_META
		} else if (left.isSubtypeOf(DATE_WITH_NO_META)) {
			DATE_TIME_WITH_NO_META
		} else if (left.isSubtypeOf(UNCONSTRAINED_STRING_WITH_NO_META)) {
			keepTypeAliasIfPossible(left.RType, right.RType, [l,r|
				if (l instanceof RStringType && r instanceof RStringType) {
					val s1 = l as RStringType
					val s2 = r as RStringType
					val newInterval = s1.interval.add(s2.interval)
					new RStringType(newInterval, Optional.empty())
				} else {
					NOTHING
				}
			]).withNoMeta
		} else if (left.isSubtypeOf(UNCONSTRAINED_NUMBER_WITH_NO_META)) {
			keepTypeAliasIfPossible(left.RType, right.RType, [l,r|
				if (l instanceof RNumberType && r instanceof RNumberType) {
					val n1 = l as RNumberType
					val n2 = r as RNumberType
					val newFractionalDigits = OptionalUtil.zipWith(n1.fractionalDigits, n2.fractionalDigits, [a,b|Math.max(a,b)])
					val newInterval = n1.interval.add(n2.interval)
					new RNumberType(Optional.empty(), newFractionalDigits, newInterval, Optional.empty())
				} else {
					NOTHING
				}
			]).withNoMeta
		} else {
			NOTHING_WITH_NO_META
		}
	}
	
	override protected caseAndOperation(LogicalOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseAsKeyOperation(AsKeyOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseBooleanLiteral(RosettaBooleanLiteral expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseChoiceOperation(ChoiceOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseConditionalExpression(RosettaConditionalExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		val ifT = expr.ifthen.safeRType(cycleTracker)
		val elseT = expr.elsethen.safeRType(cycleTracker)
		val joined = joinMetaAnnotatedTypes(ifT, elseT)
		if (ANY_WITH_NO_META.isSubtypeOf(joined)) {
			NOTHING_WITH_NO_META
		} else {
			joined
		}
	}
	
	override protected caseContainsOperation(RosettaContainsExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseDefaultOperation(DefaultOperation expr,  Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		val left = expr.left.safeRType(cycleTracker)
		val right = expr.right.safeRType(cycleTracker)
		val result = left.joinMetaAnnotatedTypes(right)
		if (ANY_WITH_NO_META.isSubtypeOf(result)) {
			NOTHING_WITH_NO_META
		} else {
			result
		}
	}
	
	override protected caseCountOperation(RosettaCountOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		constrainedInt(Optional.empty(), Optional.of(BigInteger.ZERO), Optional.empty()).withNoMeta
	}
	
	override protected caseDisjointOperation(RosettaDisjointExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseDistinctOperation(DistinctOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseDivideOperation(ArithmeticOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		UNCONSTRAINED_NUMBER_WITH_NO_META
	}
	
	override protected caseEqualsOperation(EqualityOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseExistsOperation(RosettaExistsExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseFeatureCall(RosettaFeatureCall expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		val feature = expr.feature
		if (!extensions.isResolved(feature)) {
			return NOTHING_WITH_NO_META
		}
		if (feature instanceof RosettaEnumValue) {
			expr.receiver.safeRType(cycleTracker)
		} else {
			feature.safeRType(expr, cycleTracker)
		}
	}
	
	override protected caseDeepFeatureCall(RosettaDeepFeatureCall expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		val feature = expr.feature
		if (!extensions.isResolved(feature)) {
			return NOTHING_WITH_NO_META
		}
		(feature as RosettaFeature).safeRType(expr, cycleTracker)
	}
	
	override protected caseFilterOperation(FilterOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseFirstOperation(FirstOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseFlattenOperation(FlattenOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseGreaterThanOperation(ComparisonOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseGreaterThanOrEqualOperation(ComparisonOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseImplicitVariable(RosettaImplicitVariable expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		safeTypeOfImplicitVariable(expr, cycleTracker)
	}
	
	override protected caseIntLiteral(RosettaIntLiteral expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		constrainedInt(if (expr.value.signum >= 0) expr.value.toString.length else expr.value.toString.length - 1, expr.value, expr.value).withNoMeta
	}
	
	override protected caseJoinOperation(JoinOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		UNCONSTRAINED_STRING_WITH_NO_META
	}
	
	override protected caseLastOperation(LastOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseLessThanOperation(ComparisonOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseLessThanOrEqualOperation(ComparisonOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseListLiteral(ListLiteral expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		val types = expr.elements.map[RMetaAnnotatedType].filter[it !== null]
		val joined = types.joinMetaAnnotatedTypes
		if (ANY_WITH_NO_META.isSubtypeOf(joined)) {
			NOTHING_WITH_NO_META
		} else {
			joined
		}
	}
	
	override protected caseMapOperation(MapOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.function?.body?.safeRType(cycleTracker) ?: NOTHING_WITH_NO_META
	}
	
	override protected caseMaxOperation(MaxOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseMinOperation(MinOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseMultiplyOperation(ArithmeticOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		val left = expr.left.safeRType(cycleTracker)
		val right = expr.right.safeRType(cycleTracker)
		keepTypeAliasIfPossible(left.RType, right.RType, [l,r|
			if (l instanceof RNumberType && r instanceof RNumberType) {
				val n1 = l as RNumberType
				val n2 = r as RNumberType
				val newFractionalDigits = OptionalUtil.zipWith(n1.fractionalDigits, n2.fractionalDigits, [a,b|a+b])
				val newInterval = n1.interval.multiply(n2.interval)
				new RNumberType(Optional.empty(), newFractionalDigits, newInterval, Optional.empty())
			} else {
				NOTHING
			}
		]).withNoMeta
	}
	
	override protected caseNotEqualsOperation(EqualityOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseNumberLiteral(RosettaNumberLiteral expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		if (expr.value === null) { // In case of a parse error
			return NOTHING_WITH_NO_META
		}
		constrainedNumber(expr.value.toPlainString.replaceAll("\\.|\\-", "").length, Math.max(0, expr.value.scale), expr.value, expr.value).withNoMeta
	}
	
	override protected caseOneOfOperation(OneOfOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseOnlyElementOperation(RosettaOnlyElement expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseOnlyExists(RosettaOnlyExistsExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseOrOperation(LogicalOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		BOOLEAN_WITH_NO_META
	}
	
	override protected caseReduceOperation(ReduceOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.function?.body?.safeRType(cycleTracker) ?: NOTHING_WITH_NO_META
	}
	
	override protected caseReverseOperation(ReverseOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseSortOperation(SortOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseStringLiteral(RosettaStringLiteral expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		constrainedString(expr.value.length, expr.value.length).withNoMeta
	}
	
	override protected caseSubtractOperation(ArithmeticOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		val left = expr.left.safeRType(cycleTracker)
		val right = expr.right.safeRType(cycleTracker)
		if (left.isSubtypeOf(NOTHING_WITH_NO_META)) {
			NOTHING_WITH_NO_META
		} else if (left.isSubtypeOf(DATE_WITH_NO_META)) {
			UNCONSTRAINED_INT_WITH_NO_META
		} else if (left.isSubtypeOf(UNCONSTRAINED_NUMBER_WITH_NO_META)) {
			keepTypeAliasIfPossible(left.RType, right.RType, [l,r|
				if (l instanceof RNumberType && r instanceof RNumberType) {
					val n1 = l as RNumberType
					val n2 = r as RNumberType
					val newFractionalDigits = OptionalUtil.zipWith(n1.fractionalDigits, n2.fractionalDigits, [a,b|Math.max(a,b)])
					val newInterval = n1.interval.subtract(n2.interval)
					new RNumberType(Optional.empty(), newFractionalDigits, newInterval, Optional.empty())
				} else {
					NOTHING
				}
			]).withNoMeta
		} else {
			NOTHING_WITH_NO_META
		}
	}
	
	override protected caseSumOperation(SumOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseSymbolReference(RosettaSymbolReference expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		if (expr.symbol instanceof RosettaExternalFunction) {
			val fun = expr.symbol as RosettaExternalFunction
			val returnType = fun.safeRType(expr, cycleTracker)
			// TODO: this is a hack
			// Generic return type for number type e.g. Min(1,2) or Max(2,6)
			val argTypes = expr.args.map[safeRType(cycleTracker)]
			if (argTypes.forall[isSubtypeOf(returnType)]) {
				argTypes.joinMetaAnnotatedTypes
			} else {
				returnType
			}
		} else {
			safeRType(expr.symbol, expr, cycleTracker)
		}
	}
	
	override protected caseThenOperation(ThenOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.function?.body?.safeRType(cycleTracker) ?: NOTHING_WITH_NO_META
	}
	
	override protected caseToEnumOperation(ToEnumOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.enumeration.buildREnumType.withNoMeta
	}
	
	override protected caseToIntOperation(ToIntOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		UNCONSTRAINED_INT_WITH_NO_META
	}
	
	override protected caseToNumberOperation(ToNumberOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		UNCONSTRAINED_NUMBER_WITH_NO_META
	}
	
	override protected caseToStringOperation(ToStringOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		UNCONSTRAINED_STRING_WITH_NO_META
	}
	
	override protected caseToTimeOperation(ToTimeOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		TIME_WITH_NO_META
	}
	
	override protected caseConstructorExpression(RosettaConstructorExpression expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.typeCall.typeCallToRType.withNoMeta
	}
	
	override protected caseToDateOperation(ToDateOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		DATE_WITH_NO_META
	}
	
	override protected caseToDateTimeOperation(ToDateTimeOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		DATE_TIME_WITH_NO_META
	}
	
	override protected caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		ZONED_DATE_TIME_WITH_NO_META
	}
	
	override protected caseSwitchOperation(SwitchOperation expr, Map<RosettaSymbol, RMetaAnnotatedType> cycleTracker) {
		expr.cases
			.map[it.expression.safeRType(cycleTracker)]
			.joinMetaAnnotatedTypes
 	}

}
 