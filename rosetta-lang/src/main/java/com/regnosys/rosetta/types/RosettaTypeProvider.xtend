package com.regnosys.rosetta.types

import com.regnosys.rosetta.RosettaExtensions
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
import org.eclipse.xtext.resource.XtextResource
import javax.inject.Provider
import com.regnosys.rosetta.rosetta.expression.ToDateOperation
import com.regnosys.rosetta.rosetta.expression.ToDateTimeOperation
import com.regnosys.rosetta.rosetta.expression.ToZonedDateTimeOperation
import com.regnosys.rosetta.rosetta.translate.TranslationParameter
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall
import com.regnosys.rosetta.rosetta.expression.DefaultOperation

class RosettaTypeProvider extends RosettaExpressionSwitch<RType, Map<EObject, RType>> {
	public static String EXPRESSION_RTYPE_CACHE_KEY = RosettaTypeProvider.canonicalName + ".EXPRESSION_RTYPE"

	// MP - Disabling the Expression Cache due to "ISSUE-919: Errors appear in model after adding a type".
	// The cache can be re-enabled for testing using `-DEXPRESSION_RTYPE_CACHE_KEY_ENABLED=true` in the BSP.
	public static boolean EXPRESSION_RTYPE_CACHE_KEY_ENABLED = Boolean.getBoolean("EXPRESSION_RTYPE_CACHE_KEY_ENABLED");


	@Inject extension RosettaOperators
	@Inject IQualifiedNameProvider qNames
	@Inject RosettaExtensions extensions
	@Inject extension ImplicitVariableUtil
	@Inject extension TypeSystem
	@Inject extension TypeFactory
	@Inject extension RBuiltinTypeService
	
	def RType getRType(RosettaExpression expression) {
		expression.safeRType(newHashMap)
	}
	def RType getRTypeOfFeature(RosettaFeature feature) {
		feature.safeRType(newHashMap)
	}
	def RType getRTypeOfSymbol(RosettaSymbol feature) {
		feature.safeRType(newHashMap)
	}
	def RType getRTypeOfAttributeReference(RosettaAttributeReferenceSegment seg) {
		switch seg {
			RosettaAttributeReference: seg.attribute.typeCall.typeCallToRType
			RosettaDataReference: {
				if (extensions.isResolved(seg.data)) {
					return new RDataType(seg.data)
				} else {
					NOTHING
				}
			}
		}
	}
	
	def Iterable<? extends RosettaFeature> findFeaturesOfImplicitVariable(EObject context) {
		return extensions.allFeatures(typeOfImplicitVariable(context), context)
	}

	private def RType safeRType(RosettaSymbol symbol, Map<EObject, RType> cycleTracker) {
		switch symbol {
			RosettaFeature: {
				safeRType(symbol as RosettaFeature, cycleTracker)
			}
			ClosureParameter: {
				val setOp = symbol.function.eContainer as RosettaFunctionalOperation
				if(setOp !== null) {
					setOp.argument.safeRType(cycleTracker)
				} else
					MISSING
			}
			RosettaEnumeration: { // @Compat: RosettaEnumeration should not be a RosettaSymbol.
				new REnumType(symbol)
			}
			Function: {
				if (symbol.output !== null) {
					safeRType(symbol.output as RosettaFeature, cycleTracker)
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
			TranslationParameter: {
			    symbol.typeCall.typeCallToRType
			}
		}
	}
	private def RType safeRType(RosettaFeature feature, Map<EObject, RType> cycleTracker) {
		switch (feature) {
			RosettaTypedFeature: {
				val featureType = if (feature.isTypeInferred) {
						new RErrorType("Cannot infer type of feature.")
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
				new REnumType(feature.enumeration)
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
				return null
			}
			doSwitch(expression, cycleTracker)
		])
	}
	private def RType getRTypeFromCache(String cacheKey, EObject object, Provider<RType> typeProvider) {
		if (!EXPRESSION_RTYPE_CACHE_KEY_ENABLED) {
			return typeProvider.get()
		}
		if (object === null) {
			return typeProvider.get()
		}
		val resource = object.eResource
		if (resource instanceof XtextResource) {
			resource.cache.get(cacheKey -> object, resource, [typeProvider.get])
		} else {
			typeProvider.get()
		}
	}
	
	def typeOfImplicitVariable(EObject context) {
		safeTypeOfImplicitVariable(context, newHashMap)
	}
	
	private def safeTypeOfImplicitVariable(EObject context, Map<EObject,RType> cycleTracker) {
		val definingContainer = context.findObjectDefiningImplicitVariable
		definingContainer.map [
			if (it instanceof Data) {
				new RDataType(it)
			} else if (it instanceof RosettaFunctionalOperation) {
				safeRType(argument, cycleTracker)
			} else if (it instanceof RosettaRule) {
				input?.typeCallToRType
			} else if (it instanceof TranslationParameter) {
			    typeCall.typeCallToRType
			}
		].orElse(MISSING)
	}
	
	private def caseBinaryOperation(RosettaBinaryOperation expr, Map<EObject, RType> context) {
		val left = expr.left
		var leftType = left.safeRType(context)
		if (leftType === null || leftType instanceof RErrorType) {
			return leftType
		}
		val right = expr.right
		var rightType = right.safeRType(context)
		if (rightType === null || rightType instanceof RErrorType) {
			return rightType
		}
		expr.operator.resultType(leftType, rightType)
	}
	
	override protected caseAbsentOperation(RosettaAbsentExpression expr, Map<EObject, RType> context) {
		BOOLEAN
	}
	
	override protected caseAddOperation(ArithmeticOperation expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseAndOperation(LogicalOperation expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseAsKeyOperation(AsKeyOperation expr, Map<EObject, RType> context) {
		expr.argument.safeRType(context)
	}
	
	override protected caseBooleanLiteral(RosettaBooleanLiteral expr, Map<EObject, RType> context) {
		BOOLEAN
	}
	
	override protected caseChoiceOperation(ChoiceOperation expr, Map<EObject, RType> context) {
		BOOLEAN
	}
	
	override protected caseConditionalExpression(RosettaConditionalExpression expr, Map<EObject, RType> context) {
		val ifT = expr.ifthen.safeRType(context)
		val elseT = expr.elsethen.safeRType(context)
		if (ifT === null || ifT instanceof RErrorType) {
			elseT
		} else if (elseT === null || elseT instanceof RErrorType) {
			ifT
		} else {
			val joined = join(ifT, elseT)
			if (joined == ANY) {
				new RErrorType("Can not infer common type for '" + ifT.name + "' and " + elseT.name + "'.")
			} else {
				joined
			}
		}
	}
	
	override protected caseContainsOperation(RosettaContainsExpression expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseDefaultOperation(DefaultOperation expr,  Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseCountOperation(RosettaCountOperation expr, Map<EObject, RType> context) {
		constrainedInt(Optional.empty(), Optional.of(BigInteger.ZERO), Optional.empty())
	}
	
	override protected caseDisjointOperation(RosettaDisjointExpression expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseDistinctOperation(DistinctOperation expr, Map<EObject, RType> context) {
		expr.argument.safeRType(context)
	}
	
	override protected caseDivideOperation(ArithmeticOperation expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseEqualsOperation(EqualityOperation expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseExistsOperation(RosettaExistsExpression expr, Map<EObject, RType> context) {
		BOOLEAN
	}
	
	override protected caseFeatureCall(RosettaFeatureCall expr, Map<EObject, RType> context) {
		val feature = expr.feature
		if (!extensions.isResolved(feature)) {
			return null
		}
		if (feature instanceof RosettaEnumValue) {
			expr.receiver.safeRType(context)
		} else {
			feature.safeRType(context)
		}
	}
	
	override protected caseDeepFeatureCall(RosettaDeepFeatureCall expr, Map<EObject, RType> context) {
		val feature = expr.feature
		if (!extensions.isResolved(feature)) {
			return null
		}
		(feature as RosettaFeature).safeRType(context)
	}
	
	override protected caseFilterOperation(FilterOperation expr, Map<EObject, RType> context) {
		expr.argument.safeRType(context)
	}
	
	override protected caseFirstOperation(FirstOperation expr, Map<EObject, RType> context) {
		expr.argument.safeRType(context)
	}
	
	override protected caseFlattenOperation(FlattenOperation expr, Map<EObject, RType> context) {
		expr.argument.safeRType(context)
	}
	
	override protected caseGreaterThanOperation(ComparisonOperation expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseGreaterThanOrEqualOperation(ComparisonOperation expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseImplicitVariable(RosettaImplicitVariable expr, Map<EObject, RType> context) {
		safeTypeOfImplicitVariable(expr, context)
	}
	
	override protected caseIntLiteral(RosettaIntLiteral expr, Map<EObject, RType> context) {
		constrainedInt(if (expr.value.signum >= 0) expr.value.toString.length else expr.value.toString.length - 1, expr.value, expr.value)
	}
	
	override protected caseJoinOperation(JoinOperation expr, Map<EObject, RType> context) {
		UNCONSTRAINED_STRING
	}
	
	override protected caseLastOperation(LastOperation expr, Map<EObject, RType> context) {
		expr.argument.safeRType(context)
	}
	
	override protected caseLessThanOperation(ComparisonOperation expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseLessThanOrEqualOperation(ComparisonOperation expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseListLiteral(ListLiteral expr, Map<EObject, RType> context) {
		val types = expr.elements.map[RType].filter[it !== null]
		val joined = types.join
		if (joined == ANY) {
			new RErrorType(types.groupBy[name].keySet.join(', '))
		} else {
			joined
		}
	}
	
	override protected caseMapOperation(MapOperation expr, Map<EObject, RType> context) {
		expr.function?.body?.safeRType(context)
	}
	
	override protected caseMaxOperation(MaxOperation expr, Map<EObject, RType> context) {
		expr.argument.safeRType(context)
	}
	
	override protected caseMinOperation(MinOperation expr, Map<EObject, RType> context) {
		expr.argument.safeRType(context)
	}
	
	override protected caseMultiplyOperation(ArithmeticOperation expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseNotEqualsOperation(EqualityOperation expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseNumberLiteral(RosettaNumberLiteral expr, Map<EObject, RType> context) {
		constrainedNumber(expr.value.toPlainString.replaceAll("\\.|\\-", "").length, Math.max(0, expr.value.scale), expr.value, expr.value)
	}
	
	override protected caseOneOfOperation(OneOfOperation expr, Map<EObject, RType> context) {
		BOOLEAN
	}
	
	override protected caseOnlyElementOperation(RosettaOnlyElement expr, Map<EObject, RType> context) {
		expr.argument.safeRType(context)
	}
	
	override protected caseOnlyExists(RosettaOnlyExistsExpression expr, Map<EObject, RType> context) {
		BOOLEAN
	}
	
	override protected caseOrOperation(LogicalOperation expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseReduceOperation(ReduceOperation expr, Map<EObject, RType> context) {
		expr.function?.body?.safeRType(context)
	}
	
	override protected caseReverseOperation(ReverseOperation expr, Map<EObject, RType> context) {
		expr.argument.safeRType(context)
	}
	
	override protected caseSortOperation(SortOperation expr, Map<EObject, RType> context) {
		expr.argument.safeRType(context)
	}
	
	override protected caseStringLiteral(RosettaStringLiteral expr, Map<EObject, RType> context) {
		constrainedString(expr.value.length, expr.value.length)
	}
	
	override protected caseSubtractOperation(ArithmeticOperation expr, Map<EObject, RType> context) {
		caseBinaryOperation(expr, context)
	}
	
	override protected caseSumOperation(SumOperation expr, Map<EObject, RType> context) {
		expr.argument.safeRType(context)
	}
	
	override protected caseSymbolReference(RosettaSymbolReference expr, Map<EObject, RType> context) {
		if (expr.symbol instanceof RosettaExternalFunction) {
			val fun = expr.symbol as RosettaExternalFunction
			val returnType = fun.safeRType(context)
			// TODO: this is a hack
			// Generic return type for number type e.g. Min(1,2) or Max(2,6)
			val argTypes = expr.args.map[safeRType(context)]
			if (argTypes.forall[isSubtypeOf(returnType)]) {
				argTypes.join
			} else {
				returnType
			}
		} else {
			safeRType(expr.symbol, context)
		}
	}
	
	override protected caseThenOperation(ThenOperation expr, Map<EObject, RType> context) {
		expr.function?.body?.safeRType(context)
	}
	
	override protected caseToEnumOperation(ToEnumOperation expr, Map<EObject, RType> context) {
		new REnumType(expr.enumeration)
	}
	
	override protected caseToIntOperation(ToIntOperation expr, Map<EObject, RType> context) {
		UNCONSTRAINED_INT
	}
	
	override protected caseToNumberOperation(ToNumberOperation expr, Map<EObject, RType> context) {
		UNCONSTRAINED_NUMBER
	}
	
	override protected caseToStringOperation(ToStringOperation expr, Map<EObject, RType> context) {
		UNCONSTRAINED_STRING
	}
	
	override protected caseToTimeOperation(ToTimeOperation expr, Map<EObject, RType> context) {
		TIME
	}
	
	override protected caseConstructorExpression(RosettaConstructorExpression expr, Map<EObject, RType> context) {
		expr.typeCall.typeCallToRType
	}
	
	override protected caseToDateOperation(ToDateOperation expr, Map<EObject, RType> context) {
		DATE
	}
	
	override protected caseToDateTimeOperation(ToDateTimeOperation expr, Map<EObject, RType> context) {
		DATE_TIME
	}
	
	override protected caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Map<EObject, RType> context) {
		ZONED_DATE_TIME
	}
	
}
