package com.regnosys.rosetta.types

import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral
import com.regnosys.rosetta.rosetta.RosettaTypedFeature
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.expression.ClosureParameter
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.expression.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.naming.IQualifiedNameProvider
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation
import com.regnosys.rosetta.rosetta.expression.FilterOperation
import com.regnosys.rosetta.rosetta.expression.ReverseOperation
import com.regnosys.rosetta.rosetta.expression.FlattenOperation
import com.regnosys.rosetta.rosetta.expression.DistinctOperation
import com.regnosys.rosetta.rosetta.expression.FirstOperation
import com.regnosys.rosetta.rosetta.expression.LastOperation
import com.regnosys.rosetta.rosetta.expression.ReduceOperation
import com.regnosys.rosetta.rosetta.expression.MapOperation
import com.regnosys.rosetta.rosetta.expression.SumOperation
import com.regnosys.rosetta.utils.ImplicitVariableUtil
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation
import com.regnosys.rosetta.rosetta.expression.OneOfOperation
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.ThenOperation
import java.util.Optional
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import com.regnosys.rosetta.rosetta.RosettaSymbol
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaAttributeReferenceSegment
import com.regnosys.rosetta.rosetta.RosettaAttributeReference
import com.regnosys.rosetta.rosetta.RosettaDataReference
import com.regnosys.rosetta.utils.RosettaExpressionSwitch
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation
import com.regnosys.rosetta.rosetta.expression.LogicalOperation
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression
import com.regnosys.rosetta.rosetta.expression.EqualityOperation
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation
import com.regnosys.rosetta.rosetta.expression.JoinOperation
import com.regnosys.rosetta.rosetta.expression.MaxOperation
import com.regnosys.rosetta.rosetta.expression.MinOperation
import com.regnosys.rosetta.rosetta.expression.SortOperation
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation
import com.regnosys.rosetta.rosetta.expression.ToIntOperation
import com.regnosys.rosetta.rosetta.expression.ToNumberOperation
import com.regnosys.rosetta.rosetta.expression.ToStringOperation
import com.regnosys.rosetta.rosetta.expression.ToTimeOperation
import javax.inject.Inject
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression
import com.regnosys.rosetta.rosetta.RosettaRule
import java.math.BigInteger
import javax.inject.Provider
import com.regnosys.rosetta.rosetta.expression.ToDateOperation
import com.regnosys.rosetta.rosetta.expression.ToDateTimeOperation
import com.regnosys.rosetta.rosetta.expression.ToZonedDateTimeOperation
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall
import com.regnosys.rosetta.rosetta.expression.DefaultOperation
import com.regnosys.rosetta.cache.IRequestScopedCache
import com.regnosys.rosetta.rosetta.TypeParameter
import com.regnosys.rosetta.rosetta.expression.SwitchOperation
import com.regnosys.rosetta.rosetta.simple.AssignPathRoot
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.RosettaEcoreUtil
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.rosetta.expression.SwitchCase

class RosettaTypeProvider extends RosettaExpressionSwitch<RType, Map<EObject, RType>> {
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

	def RType getRType(RosettaExpression expression) {
		expression.safeRType(newHashMap)
	}
	def RType getRTypeOfFeature(RosettaFeature feature, EObject context) {
		feature.safeRType(context, newHashMap)
	}
	def RType getRTypeOfSymbol(RosettaSymbol feature, EObject context) {
		feature.safeRType(context, newHashMap)
	}
	def RType getRTypeOfSymbol(AssignPathRoot feature) {
		feature.getRTypeOfSymbol(null)
	}
	def RType getRTypeOfSymbol(RosettaCallableWithArgs feature) {
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

	private def RType safeRType(RosettaSymbol symbol, EObject context,Map<EObject, RType> cycleTracker) {
		if (!extensions.isResolved(symbol)) {
			return NOTHING
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
					MISSING
			}
			RosettaEnumeration: { // @Compat: RosettaEnumeration should not be a RosettaSymbol.
				symbol.buildREnumType
			}
			Function: {
				if (symbol.output !== null) {
					safeRType(symbol.output as RosettaFeature, context, cycleTracker)
				} else {
					MISSING
				}
			}
			RosettaRule: {
				if (symbol.expression !== null) {
					safeRType(symbol.expression, cycleTracker)
				} else {
					MISSING
				}
			}
			RosettaExternalFunction: {
				symbol.typeCall.typeCallToRType
			}
			ShortcutDeclaration: {
				cycleTracker.put(symbol, null)
				val type = symbol.expression.safeRType(cycleTracker)
				cycleTracker.put(symbol, type)
				type
			}
			TypeParameter: {
				symbol.typeCall.typeCallToRType
			}
		}
	}
	private def RType safeRType(RosettaFeature feature, EObject context, Map<EObject, RType> cycleTracker) {
		if (!extensions.isResolved(feature)) {
			return NOTHING
		}
		switch (feature) {
			RosettaTypedFeature: {
				val featureType = if (feature.typeCall === null) {
						NOTHING
					} else {
						feature.typeCall.typeCallToRType
					}
				if (feature instanceof Annotated) {
					if (featureType instanceof RAnnotateType) {
						featureType.withMeta = extensions.hasMetaDataAnnotations(feature)
					}
				}
				featureType
			}
			RosettaEnumValue: {
				if (context instanceof RosettaFeatureCall) {
					context.receiver.safeRType(cycleTracker)
				} else {
					NOTHING
				}
			}
			default:
				new RErrorType("Cannot infer type of feature.")
		}
	}
	private def RType safeRType(RosettaExpression expression, Map<EObject, RType> cycleTracker) {
		getRTypeFromCache(EXPRESSION_RTYPE_CACHE_KEY, expression, [
			if (cycleTracker.containsKey(expression)) {
				val computed = cycleTracker.get(expression)
				if (computed === null) {
					return new RErrorType('''Can't infer type due to a cyclic reference of «qNames.getFullyQualifiedName(expression)»''')
				} else {
					return computed
				}
			}
			if (!extensions.isResolved(expression)) {
				return NOTHING
			}
			doSwitch(expression, cycleTracker)
		])
	}
	private def RType getRTypeFromCache(String cacheKey, EObject object, Provider<RType> typeProvider) {
		if (object === null) {
			return typeProvider.get()
		}
		cache.get(cacheKey -> object, typeProvider)
	}
	
	def typeOfImplicitVariable(EObject context) {
		safeTypeOfImplicitVariable(context, newHashMap)
	}
	
	private def safeTypeOfImplicitVariable(EObject context, Map<EObject,RType> cycleTracker) {
		val definingContainer = context.findContainerDefiningImplicitVariable
		definingContainer.map [
			if (it instanceof Data) {
				buildRDataType
			} else if (it instanceof RosettaFunctionalOperation) {
				safeRType(argument, cycleTracker)
			} else if (it instanceof RosettaRule) {
				input?.typeCallToRType ?: MISSING
			} else if (it instanceof SwitchCase) {
				guard.choiceOptionGuard.RTypeOfSymbol
			}
		].orElse(MISSING)
	}
	
	private def caseBinaryOperation(RosettaBinaryOperation expr, Map<EObject, RType> cycleTracker) {
		val left = expr.left
		var leftType = left.safeRType(cycleTracker)
		if (leftType instanceof RErrorType) {
			return NOTHING
		}
		val right = expr.right
		var rightType = right.safeRType(cycleTracker)
		if (rightType instanceof RErrorType) {
			return NOTHING
		}
		expr.operator.resultType(leftType, rightType)
	}
	
	override protected caseAbsentOperation(RosettaAbsentExpression expr, Map<EObject, RType> cycleTracker) {
		BOOLEAN
	}
	
	override protected caseAddOperation(ArithmeticOperation expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseAndOperation(LogicalOperation expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseAsKeyOperation(AsKeyOperation expr, Map<EObject, RType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseBooleanLiteral(RosettaBooleanLiteral expr, Map<EObject, RType> cycleTracker) {
		BOOLEAN
	}
	
	override protected caseChoiceOperation(ChoiceOperation expr, Map<EObject, RType> cycleTracker) {
		BOOLEAN
	}
	
	override protected caseConditionalExpression(RosettaConditionalExpression expr, Map<EObject, RType> cycleTracker) {
		val ifT = expr.ifthen.safeRType(cycleTracker)
		if (ifT instanceof RErrorType) {
			return NOTHING
		}
		val elseT = expr.elsethen.safeRType(cycleTracker)
		if (elseT instanceof RErrorType) {
			return NOTHING
		}
		val joined = join(ifT, elseT)
		if (joined == ANY) {
			new RErrorType('''Types `«ifT»` and `«elseT»` do not have a common supertype.''')
		} else {
			joined
		}
	}
	
	override protected caseContainsOperation(RosettaContainsExpression expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseDefaultOperation(DefaultOperation expr,  Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseCountOperation(RosettaCountOperation expr, Map<EObject, RType> cycleTracker) {
		constrainedInt(Optional.empty(), Optional.of(BigInteger.ZERO), Optional.empty())
	}
	
	override protected caseDisjointOperation(RosettaDisjointExpression expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseDistinctOperation(DistinctOperation expr, Map<EObject, RType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseDivideOperation(ArithmeticOperation expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseEqualsOperation(EqualityOperation expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseExistsOperation(RosettaExistsExpression expr, Map<EObject, RType> cycleTracker) {
		BOOLEAN
	}
	
	override protected caseFeatureCall(RosettaFeatureCall expr, Map<EObject, RType> cycleTracker) {
		val feature = expr.feature
		if (!extensions.isResolved(feature)) {
			return NOTHING
		}
		if (feature instanceof RosettaEnumValue) {
			expr.receiver.safeRType(cycleTracker)
		} else {
			feature.safeRType(expr, cycleTracker)
		}
	}
	
	override protected caseDeepFeatureCall(RosettaDeepFeatureCall expr, Map<EObject, RType> cycleTracker) {
		val feature = expr.feature
		if (!extensions.isResolved(feature)) {
			return NOTHING
		}
		(feature as RosettaFeature).safeRType(expr, cycleTracker)
	}
	
	override protected caseFilterOperation(FilterOperation expr, Map<EObject, RType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseFirstOperation(FirstOperation expr, Map<EObject, RType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseFlattenOperation(FlattenOperation expr, Map<EObject, RType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseGreaterThanOperation(ComparisonOperation expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseGreaterThanOrEqualOperation(ComparisonOperation expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseImplicitVariable(RosettaImplicitVariable expr, Map<EObject, RType> cycleTracker) {
		safeTypeOfImplicitVariable(expr, cycleTracker)
	}
	
	override protected caseIntLiteral(RosettaIntLiteral expr, Map<EObject, RType> cycleTracker) {
		constrainedInt(if (expr.value.signum >= 0) expr.value.toString.length else expr.value.toString.length - 1, expr.value, expr.value)
	}
	
	override protected caseJoinOperation(JoinOperation expr, Map<EObject, RType> cycleTracker) {
		UNCONSTRAINED_STRING
	}
	
	override protected caseLastOperation(LastOperation expr, Map<EObject, RType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseLessThanOperation(ComparisonOperation expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseLessThanOrEqualOperation(ComparisonOperation expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseListLiteral(ListLiteral expr, Map<EObject, RType> cycleTracker) {
		val types = expr.elements.map[RType].filter[it !== null]
		val joined = types.join
		val unique = newLinkedHashSet(types)
		val StringConcatenationClient failedList = '''«FOR t: unique.take(unique.size-1) SEPARATOR ", "»`«t»`«ENDFOR» and `«unique.last»`'''
		if (joined == ANY) {
			new RErrorType('''Types «failedList» do not have a common supertype.''')
		} else {
			joined
		}
	}
	
	override protected caseMapOperation(MapOperation expr, Map<EObject, RType> cycleTracker) {
		expr.function?.body?.safeRType(cycleTracker)
	}
	
	override protected caseMaxOperation(MaxOperation expr, Map<EObject, RType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseMinOperation(MinOperation expr, Map<EObject, RType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseMultiplyOperation(ArithmeticOperation expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseNotEqualsOperation(EqualityOperation expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseNumberLiteral(RosettaNumberLiteral expr, Map<EObject, RType> cycleTracker) {
		if (expr.value === null) { // In case of a parse error
			return NOTHING
		}
		constrainedNumber(expr.value.toPlainString.replaceAll("\\.|\\-", "").length, Math.max(0, expr.value.scale), expr.value, expr.value)
	}
	
	override protected caseOneOfOperation(OneOfOperation expr, Map<EObject, RType> cycleTracker) {
		BOOLEAN
	}
	
	override protected caseOnlyElementOperation(RosettaOnlyElement expr, Map<EObject, RType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseOnlyExists(RosettaOnlyExistsExpression expr, Map<EObject, RType> cycleTracker) {
		BOOLEAN
	}
	
	override protected caseOrOperation(LogicalOperation expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseReduceOperation(ReduceOperation expr, Map<EObject, RType> cycleTracker) {
		expr.function?.body?.safeRType(cycleTracker)
	}
	
	override protected caseReverseOperation(ReverseOperation expr, Map<EObject, RType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseSortOperation(SortOperation expr, Map<EObject, RType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseStringLiteral(RosettaStringLiteral expr, Map<EObject, RType> cycleTracker) {
		constrainedString(expr.value.length, expr.value.length)
	}
	
	override protected caseSubtractOperation(ArithmeticOperation expr, Map<EObject, RType> cycleTracker) {
		caseBinaryOperation(expr, cycleTracker)
	}
	
	override protected caseSumOperation(SumOperation expr, Map<EObject, RType> cycleTracker) {
		expr.argument.safeRType(cycleTracker)
	}
	
	override protected caseSymbolReference(RosettaSymbolReference expr, Map<EObject, RType> cycleTracker) {
		if (expr.symbol instanceof RosettaExternalFunction) {
			val fun = expr.symbol as RosettaExternalFunction
			val returnType = fun.safeRType(expr, cycleTracker)
			// TODO: this is a hack
			// Generic return type for number type e.g. Min(1,2) or Max(2,6)
			val argTypes = expr.args.map[safeRType(cycleTracker)]
			if (argTypes.forall[isSubtypeOf(returnType)]) {
				argTypes.join
			} else {
				returnType
			}
		} else {
			safeRType(expr.symbol, expr, cycleTracker)
		}
	}
	
	override protected caseThenOperation(ThenOperation expr, Map<EObject, RType> cycleTracker) {
		expr.function?.body?.safeRType(cycleTracker)
	}
	
	override protected caseToEnumOperation(ToEnumOperation expr, Map<EObject, RType> cycleTracker) {
		expr.enumeration.buildREnumType
	}
	
	override protected caseToIntOperation(ToIntOperation expr, Map<EObject, RType> cycleTracker) {
		UNCONSTRAINED_INT
	}
	
	override protected caseToNumberOperation(ToNumberOperation expr, Map<EObject, RType> cycleTracker) {
		UNCONSTRAINED_NUMBER
	}
	
	override protected caseToStringOperation(ToStringOperation expr, Map<EObject, RType> cycleTracker) {
		UNCONSTRAINED_STRING
	}
	
	override protected caseToTimeOperation(ToTimeOperation expr, Map<EObject, RType> cycleTracker) {
		TIME
	}
	
	override protected caseConstructorExpression(RosettaConstructorExpression expr, Map<EObject, RType> cycleTracker) {
		expr.typeCall.typeCallToRType
	}
	
	override protected caseToDateOperation(ToDateOperation expr, Map<EObject, RType> cycleTracker) {
		DATE
	}
	
	override protected caseToDateTimeOperation(ToDateTimeOperation expr, Map<EObject, RType> cycleTracker) {
		DATE_TIME
	}
	
	override protected caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Map<EObject, RType> cycleTracker) {
		ZONED_DATE_TIME
	}
	
	override protected caseSwitchOperation(SwitchOperation expr, Map<EObject, RType> context) {
		expr.cases
			.map[it.expression.RType]
			.join
 	}

}
