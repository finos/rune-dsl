package com.regnosys.rosetta.types

import com.google.inject.Inject
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
import java.util.List
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
import com.regnosys.rosetta.rosetta.expression.ComparingFunctionalOperation
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
import com.regnosys.rosetta.rosetta.expression.RosettaPatternLiteral
import com.regnosys.rosetta.rosetta.RosettaBlueprint

class RosettaTypeProvider {

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
			RosettaDataReference: new RDataType(seg.data)
		}
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
			Data: { // @Compat: Data should not be a RosettaSymbol.
				new RDataType(symbol)
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
			RosettaExternalFunction: {
				symbol.typeCall.typeCallToRType
			}
			ShortcutDeclaration: {
				cycleTracker.put(symbol, null)
				val type = symbol.expression.safeRType(cycleTracker)
				cycleTracker.put(symbol, type)
				type
			}
		}
	}
	private def RType safeRType(RosettaFeature feature, Map<EObject, RType> cycleTracker) {
		switch (feature) {
			RosettaTypedFeature: {
				val featureType = if (feature.isTypeInferred) {
						feature.safeRType(cycleTracker)
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
			RosettaEnumValue:
				new REnumType(feature.enumeration)
			default:
				new RErrorType("Cannot infer type of feature.")
		}
	}
	private def RType safeRType(RosettaExpression expression, Map<EObject, RType> cycleTracker) {
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
		switch expression {
			RosettaSymbolReference: {
				if (expression.symbol instanceof RosettaExternalFunction) {
					val fun = expression.symbol as RosettaExternalFunction
					val returnType = fun.safeRType(cycleTracker)
					// TODO: this is a hack
					// Generic return type for number type e.g. Min(1,2) or Max(2,6)
					val argTypes = expression.args.map[safeRType(cycleTracker)]
					if (argTypes.forall[isSubtypeOf(returnType)]) {
						argTypes.join
					} else {
						returnType
					}
				} else {
					safeRType(expression.symbol, cycleTracker)
				}
			}
			RosettaImplicitVariable: {
				safeTypeOfImplicitVariable(expression, cycleTracker)
			}
			RosettaFeatureCall: {
				val feature = expression.feature
				if (!extensions.isResolved(feature)) {
					return null
				}
				feature.safeRType(cycleTracker)
			}
			RosettaOnlyElement: {
				safeRType(expression.argument, cycleTracker)
			}
			RosettaBinaryOperation: {
				val left = expression.left
				var leftType = left.safeRType(cycleTracker)
				if (leftType instanceof RErrorType) {
					return leftType
				}
				val right = expression.right
				var rightType = right.safeRType(cycleTracker)
				if (rightType instanceof RErrorType) {
					return rightType
				}
				expression.operator.resultType(leftType, rightType)
			}
			RosettaCountOperation: {
				constrainedInt(Optional.empty(), Optional.of(0), Optional.empty())
			}
			RosettaOnlyExistsExpression,
			RosettaExistsExpression,
			RosettaAbsentExpression,
			RosettaBooleanLiteral,
			OneOfOperation,
			ChoiceOperation:
				BOOLEAN
			RosettaStringLiteral:
				constrainedString(expression.value.length, expression.value.length)
			RosettaIntLiteral:
				constrainedInt(if (expression.value >= 0) expression.value.toString.length else expression.value.toString.length - 1, expression.value, expression.value)
			RosettaNumberLiteral:
				constrainedNumber(expression.value.toPlainString.replaceAll("\\.|\\-", "").length, Math.max(0, expression.value.scale), expression.value, expression.value)
			RosettaPatternLiteral:
				PATTERN
			ListLiteral:
				listType(expression.elements)
			RosettaConditionalExpression: {
				val ifT = expression.ifthen.safeRType(cycleTracker)
				val elseT = expression.elsethen.safeRType(cycleTracker)
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
			ReverseOperation,
			FlattenOperation,
			DistinctOperation,
			ComparingFunctionalOperation,
			SumOperation,
			FirstOperation,
			LastOperation,
			FilterOperation,
			AsKeyOperation:
				expression.argument.safeRType(cycleTracker)
			ReduceOperation,
			MapOperation,
			ThenOperation:
				expression.function?.body?.safeRType(cycleTracker)
			default:
				MISSING
		}
	}
	
	def typeOfImplicitVariable(EObject context) {
		safeTypeOfImplicitVariable(context, newHashMap)
	}
	
	private def safeTypeOfImplicitVariable(EObject context, Map<EObject,RType> cycleTracker) {
		val definingContainer = context.findContainerDefiningImplicitVariable
		definingContainer.map [
			if (it instanceof Data) {
				new RDataType(it)
			} else if (it instanceof RosettaFunctionalOperation) {
				safeRType(argument, cycleTracker)
			} else if (it instanceof RosettaBlueprint) {
				input?.typeCallToRType ?: ANY
			}
		].orElse(MISSING)
	}
	
	private def listType(List<RosettaExpression> exp) {
		val types = exp.map[RType]
		val joined = types.join
		if (joined == ANY) {
			new RErrorType(types.groupBy[name].keySet.join(', '))
		} else {
			joined
		}
	}
}
