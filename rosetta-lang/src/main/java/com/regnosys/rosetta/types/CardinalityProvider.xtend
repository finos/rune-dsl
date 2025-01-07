package com.regnosys.rosetta.types

import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.expression.ClosureParameter
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.expression.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import org.eclipse.emf.ecore.EObject
import com.regnosys.rosetta.rosetta.expression.ReduceOperation
import com.regnosys.rosetta.rosetta.expression.FilterOperation
import com.regnosys.rosetta.rosetta.expression.SortOperation
import com.regnosys.rosetta.rosetta.expression.MapOperation
import com.regnosys.rosetta.rosetta.expression.InlineFunction
import com.regnosys.rosetta.rosetta.expression.DistinctOperation
import com.regnosys.rosetta.rosetta.expression.FirstOperation
import com.regnosys.rosetta.rosetta.expression.LastOperation
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement
import com.regnosys.rosetta.rosetta.expression.FlattenOperation
import com.regnosys.rosetta.rosetta.expression.ReverseOperation
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation
import com.regnosys.rosetta.rosetta.expression.SumOperation
import com.regnosys.rosetta.rosetta.expression.MinOperation
import com.regnosys.rosetta.rosetta.expression.MaxOperation
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.expression.CanHandleListOfLists
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable
import com.regnosys.rosetta.utils.ImplicitVariableUtil
import javax.inject.Inject
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.expression.ThenOperation
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.OneOfOperation
import com.regnosys.rosetta.utils.RosettaExpressionSwitch
import com.regnosys.rosetta.rosetta.expression.ArithmeticOperation
import com.regnosys.rosetta.rosetta.expression.LogicalOperation
import com.regnosys.rosetta.rosetta.expression.RosettaBooleanLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaDisjointExpression
import com.regnosys.rosetta.rosetta.expression.EqualityOperation
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral
import com.regnosys.rosetta.rosetta.expression.JoinOperation
import com.regnosys.rosetta.rosetta.expression.RosettaNumberLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral
import com.regnosys.rosetta.rosetta.expression.ToEnumOperation
import com.regnosys.rosetta.rosetta.expression.ToIntOperation
import com.regnosys.rosetta.rosetta.expression.ToNumberOperation
import com.regnosys.rosetta.rosetta.expression.ToStringOperation
import com.regnosys.rosetta.rosetta.expression.ToTimeOperation
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.RosettaSymbol
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.regnosys.rosetta.rosetta.TypeParameter
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression
import com.regnosys.rosetta.rosetta.RosettaRule
import com.regnosys.rosetta.rosetta.expression.ToDateOperation
import com.regnosys.rosetta.rosetta.expression.ToDateTimeOperation
import com.regnosys.rosetta.rosetta.expression.ToZonedDateTimeOperation
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall
import com.regnosys.rosetta.rosetta.expression.DefaultOperation
import com.regnosys.rosetta.rosetta.expression.SwitchOperation
import com.regnosys.rosetta.rosetta.expression.SwitchCase
import com.regnosys.rosetta.rosetta.RosettaParameter

class CardinalityProvider extends RosettaExpressionSwitch<Boolean, Boolean> {
	static Logger LOGGER = LoggerFactory.getLogger(CardinalityProvider)
	
	@Inject extension ImplicitVariableUtil
	@Inject RosettaTypeProvider typeProvider
	
	def boolean isMulti(RosettaExpression expr) {
		isMulti(expr, false)
	}
	def boolean isMulti(RosettaExpression expr, boolean breakOnClosureParameter) {
		if (expr === null) {
			return false
		}
		doSwitch(expr, breakOnClosureParameter)
	}
	def boolean isSymbolMulti(RosettaSymbol symbol) {
		isSymbolMulti(symbol, false)
	}
	def boolean isFeatureMulti(RosettaFeature symbol) {
		isFeatureMulti(symbol, false)
	}
	
	def boolean isSymbolMulti(RosettaSymbol symbol, boolean breakOnClosureParameter) {
		switch symbol {
			RosettaFeature: {
				isFeatureMulti(symbol as RosettaFeature, breakOnClosureParameter)
			}
			RosettaParameter: {
				false
			}
			ClosureParameter: {
				isClosureParameterMulti(symbol.function)
			}
			RosettaEnumeration: { // @Compat: RosettaEnumeration should not be a RosettaSymbol.
				false
			}
			Function: {
				if (symbol.output !== null) {
					isFeatureMulti(symbol.output as RosettaFeature, breakOnClosureParameter)
				} else {
					false
				}
			}
			RosettaRule: {
				if (symbol.expression !== null) {
					isMulti(symbol.expression, breakOnClosureParameter)
				} else {
					false
				}
			}
			RosettaExternalFunction: {
				false
			}
			ShortcutDeclaration: {
				symbol.expression.isMulti(breakOnClosureParameter)
			}
			TypeParameter: {
				false
			}
			default: {
				LOGGER.error("Cardinality not defined for symbol: " + symbol?.eClass?.name)
				false
			}
		}
	}
	def boolean isFeatureMulti(RosettaFeature feature, boolean breakOnClosureParameter) {
		switch (feature) {
			Attribute: {
				if(feature.card === null) {
					false
				} else {
					feature.card.isIsMany
				}
			}
			default:
				false
		}
	}
	
	def isImplicitVariableMulti(EObject context) {
		isImplicitVariableMulti(context, false)
	}
	
	def isImplicitVariableMulti(EObject context, boolean breakOnClosureParameter) {
		val definingContainer = context.findContainerDefiningImplicitVariable
		definingContainer.map [
			if (it instanceof Data) {
				false
			} else if (it instanceof RosettaFunctionalOperation) {
				isClosureParameterMulti(it.function)
			} else if (it instanceof RosettaRule) {
				false
			} else if (it instanceof SwitchCase) {
				false
			} else {
				false
			}
		].orElse(false)
	}
	
	private def boolean isFeatureOfImplicitVariable(EObject context, RosettaFeature feature) {
		typeProvider.findFeaturesOfImplicitVariable(context).contains(feature)
	}
	
	private def boolean isClosureParameterMulti(InlineFunction obj) {
		val op = obj.eContainer
		if (op instanceof RosettaFunctionalOperation) {
			if (op instanceof ThenOperation) {
				return op.argument.isMulti
			}
			return op.argument.isOutputListOfLists
		}
		return false
	}
	
	def isItemMulti(InlineFunction op) {
		op.isClosureParameterMulti
	}
	
	/**
	 * Does the body of the previous list operation result in a list.
	 */
	def boolean isPreviousOperationBodyMulti(RosettaUnaryOperation expr) {
		val previousOperation = expr.argument
		if (previousOperation instanceof RosettaUnaryOperation) {
			// only map and extract-all can increase a closure parameter's cardinality
			switch (previousOperation) {
				ThenOperation:
					return previousOperation.isMulti
				MapOperation:
					return previousOperation.function.body.isMulti(false)
				FlattenOperation:
					return false
				default:
					return previousOperation.isPreviousOperationBodyMulti
			}
		}
		return false
	}
	
	
	/**
	 * List MAP/FILTER/Extract-all operations can handle a list of lists, however it cannot be handled anywhere else (e.g. a list of list cannot be assigned to a func output or alias)
	 */
	def boolean isOutputListOfLists(RosettaExpression op) {
		if (op instanceof FlattenOperation) {
			false
		}
		else if (op instanceof MapOperation) {
			if (op.function === null) {
				false
			} else if (op.function.isItemMulti) {
				op.function.isBodyExpressionMulti
			} else {
				op.function.isBodyExpressionMulti && op.isPreviousOperationMulti
			}
		}
		else if (op instanceof ThenOperation) {
			val f = op.function
			switch f {
				InlineFunction:
					f.body.isOutputListOfLists
				default:
					false
			}
		}
		else if (op instanceof RosettaSymbolReference) {
			val s = op.symbol
			if (s instanceof ClosureParameter) {
				val f = s.function
				val enclosed = f.eContainer
				if (enclosed instanceof ThenOperation) {
					return enclosed.argument.isOutputListOfLists
				} else {
					false
				}
			} else {
				false
			}
		}
		else if (op instanceof RosettaImplicitVariable) {
			val definingContainer = op.findContainerDefiningImplicitVariable
			definingContainer.map [
				if (it instanceof ThenOperation)
					(it as RosettaFunctionalOperation).argument.isOutputListOfLists
				else
					false
			].orElse(false)
		}
		else if (op instanceof CanHandleListOfLists) {
			val previousListOp = op.argument
			previousListOp.isOutputListOfLists
		} else {
			false
		}
	}
	
	def isPreviousOperationMulti(RosettaUnaryOperation op) {
		isMulti(op.argument)
	}
	
	def isBodyExpressionMulti(InlineFunction op) {
		op.body !== null && isMulti(op.body, false)
	}

	/**
	 * Nothing handles a list of list of list
	 */
	def boolean isOutputListOfListOfLists(RosettaExpression op) {
		false // The output of an expression never results a list of lists of lists.
	}
	
	/**
	 * Does the list operation body expression increase the cardinality? 
	 * 
	 * E.g., 
	 * - from single to list, or from list to list of lists, would return true.
	 * - from single to single, or from list to list, or from list to single, would return false.
	 */
	def isBodyExpressionWithSingleInputMulti(InlineFunction op) {
		op.body !== null && isMulti(op.body, true)
	}
	
	override protected caseAbsentOperation(RosettaAbsentExpression expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseAddOperation(ArithmeticOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseAndOperation(LogicalOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseAsKeyOperation(AsKeyOperation expr, Boolean breakOnClosureParameter) {
		expr.argument.isMulti(breakOnClosureParameter)
	}
	
	override protected caseBooleanLiteral(RosettaBooleanLiteral expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseChoiceOperation(ChoiceOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseConditionalExpression(RosettaConditionalExpression expr, Boolean breakOnClosureParameter) {
		expr.ifthen.isMulti(breakOnClosureParameter) || expr.elsethen.isMulti(breakOnClosureParameter)
	}
	
	override protected caseContainsOperation(RosettaContainsExpression expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseCountOperation(RosettaCountOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseDisjointOperation(RosettaDisjointExpression expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseDefaultOperation(DefaultOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseDistinctOperation(DistinctOperation expr, Boolean breakOnClosureParameter) {
		expr.argument.isMulti(breakOnClosureParameter)
	}
	
	override protected caseDivideOperation(ArithmeticOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseEqualsOperation(EqualityOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseExistsOperation(RosettaExistsExpression expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseFeatureCall(RosettaFeatureCall expr, Boolean breakOnClosureParameter) {
		if (expr.feature.isFeatureMulti(breakOnClosureParameter)) 
			true 
		else 
			expr.receiver.isMulti(breakOnClosureParameter)
	}
	
	override protected caseDeepFeatureCall(RosettaDeepFeatureCall expr, Boolean breakOnClosureParameter) {
		if (expr.feature.isFeatureMulti(breakOnClosureParameter)) 
			true 
		else 
			expr.receiver.isMulti(breakOnClosureParameter)
	}
	
	override protected caseFilterOperation(FilterOperation expr, Boolean breakOnClosureParameter) {
		expr.argument.isMulti(breakOnClosureParameter)
	}
	
	override protected caseFirstOperation(FirstOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseFlattenOperation(FlattenOperation expr, Boolean breakOnClosureParameter) {
		true
	}
	
	override protected caseGreaterThanOperation(ComparisonOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseGreaterThanOrEqualOperation(ComparisonOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseImplicitVariable(RosettaImplicitVariable expr, Boolean breakOnClosureParameter) {
		expr.isImplicitVariableMulti(breakOnClosureParameter)
	}
	
	override protected caseIntLiteral(RosettaIntLiteral expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseJoinOperation(JoinOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseLastOperation(LastOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseLessThanOperation(ComparisonOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseLessThanOrEqualOperation(ComparisonOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseListLiteral(ListLiteral expr, Boolean breakOnClosureParameter) {
		expr.elements.size > 0 // TODO: the type system is currently not strong enough to implement this completely right
	}
	
	override protected caseMapOperation(MapOperation expr, Boolean breakOnClosureParameter) {
		if (expr.function !== null && expr.function.body.isMulti(breakOnClosureParameter)) {
			true
		} else {
			expr.argument.isMulti(breakOnClosureParameter)
		}
	}
	
	override protected caseMaxOperation(MaxOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseMinOperation(MinOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseMultiplyOperation(ArithmeticOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseNotEqualsOperation(EqualityOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseNumberLiteral(RosettaNumberLiteral expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseOneOfOperation(OneOfOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseOnlyElementOperation(RosettaOnlyElement expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseOnlyExists(RosettaOnlyExistsExpression expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseOrOperation(LogicalOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseReduceOperation(ReduceOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseReverseOperation(ReverseOperation expr, Boolean breakOnClosureParameter) {
		true
	}
	
	override protected caseSortOperation(SortOperation expr, Boolean breakOnClosureParameter) {
		true
	}
	
	override protected caseStringLiteral(RosettaStringLiteral expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseSubtractOperation(ArithmeticOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseSumOperation(SumOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseSymbolReference(RosettaSymbolReference expr, Boolean breakOnClosureParameter) {
		val s = expr.symbol
		if (s instanceof RosettaFeature) {
			if (isFeatureOfImplicitVariable(expr, s) && isImplicitVariableMulti(expr, breakOnClosureParameter)) {
				return true
			}
		}
		return s.isSymbolMulti(breakOnClosureParameter)
	}
	
	override protected caseThenOperation(ThenOperation expr, Boolean breakOnClosureParameter) {
		if (expr.function !== null) {
			expr.function.body.isMulti(breakOnClosureParameter)
		} else {
			false
		}
	}
	
	override protected caseToEnumOperation(ToEnumOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseToIntOperation(ToIntOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseToNumberOperation(ToNumberOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseToStringOperation(ToStringOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseToTimeOperation(ToTimeOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseConstructorExpression(RosettaConstructorExpression expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseToDateOperation(ToDateOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseToDateTimeOperation(ToDateTimeOperation expr, Boolean breakOnClosureParameter) {
		false
	}
	
	override protected caseToZonedDateTimeOperation(ToZonedDateTimeOperation expr, Boolean breakOnClosureParameter) {
		false
	}	
	
	override protected caseSwitchOperation(SwitchOperation expr, Boolean breakOnClosureParameter) {
		if (expr.^default.isMulti) {
			return true
		}
		for (switchCase : expr.cases) {
			if (switchCase.expression.isMulti) {
				return true
			}
		}
 		false
 	}
}