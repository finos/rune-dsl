package com.regnosys.rosetta.types

import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaMapPathValue
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase
import com.regnosys.rosetta.rosetta.RosettaTypedFeature
import com.regnosys.rosetta.rosetta.WithCardinality
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
import com.regnosys.rosetta.rosetta.RosettaParameter
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.expression.ThenOperation
import com.regnosys.rosetta.rosetta.RosettaBlueprint
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.OneOfOperation
import com.regnosys.rosetta.rosetta.TypeParameter

class CardinalityProvider {
	
	@Inject extension ImplicitVariableUtil
	
	def boolean isMulti(EObject obj) {
		isMulti(obj, false)
	}
	
	def boolean isMulti(EObject obj, boolean breakOnClosureParameter) {
		if(obj === null) return false
		switch obj {
			RosettaFeatureCall: {
				if (obj.feature.isMulti(breakOnClosureParameter)) 
					true 
				else 
					obj.receiver.isMulti(breakOnClosureParameter)
			}
			RosettaEnumValue:false
			WithCardinality: if(obj.card === null) false else obj.card.isIsMany
			RosettaSymbolReference: {
				obj.symbol.isMulti(breakOnClosureParameter)
			}
			RosettaImplicitVariable: {
				obj.isImplicitVariableMulti
			}
			Function: if(obj.output === null) false else obj.output.isMulti(breakOnClosureParameter)
			RosettaBlueprint: obj.expression.isMulti(breakOnClosureParameter)
			ShortcutDeclaration: obj.expression.isMulti(breakOnClosureParameter)
			RosettaConditionalExpression: obj.ifthen.isMulti(breakOnClosureParameter) || obj.elsethen.isMulti(breakOnClosureParameter) 
			ClosureParameter: {
				if (breakOnClosureParameter) 
					false 
				else 
					obj.isClosureParameterMulti
			}
			ListLiteral: obj.elements.size > 0 // TODO: the type system is currently not strong enough to implement this completely right
			ReduceOperation: false
			FilterOperation: {
				obj.argument.isMulti(breakOnClosureParameter)
			}
			MapOperation: {
				if (obj.function.isMulti(breakOnClosureParameter)) {
					true
				} else {
					obj.argument.isMulti(breakOnClosureParameter)
				}
			}
			ThenOperation: obj.function.isMulti(breakOnClosureParameter)
			SortOperation: true
			InlineFunction: obj.body.isMulti(breakOnClosureParameter)
			FirstOperation,
			LastOperation,
			SumOperation,
			MinOperation,
			MaxOperation,
			RosettaAbsentExpression,
			RosettaCountOperation,
			RosettaExistsExpression,
			RosettaOnlyElement,
			ChoiceOperation,
			OneOfOperation:
				false
			DistinctOperation,
			FlattenOperation,
			ReverseOperation:
				true
			RosettaBinaryOperation: {
				false // check '+' operator
			}
			AsKeyOperation: {
				obj.argument.isMulti(breakOnClosureParameter)
			}
			RosettaLiteral,
			RosettaTypedFeature,
			RosettaFeature,
			RosettaSynonymValueBase,
			RosettaOnlyExistsExpression,
			RosettaRootElement,
			RosettaEnumValueReference,
			RosettaMapPathValue,
			RosettaParameter,
			TypeParameter: false
			default: {println("CardinalityProvider: Cardinality not defined for: " +obj?.eClass?.name)false }
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
			} else if (it instanceof RosettaBlueprint) {
				false
			} else {
				false
			}
		].orElse(false)
	}
	
	/**
	 * ListOperation.firstOrImplicit (e.g. ClosureParameter) can be null if parameter is implicit
	 */
	private def boolean isClosureParameterMulti(ClosureParameter obj) {
		if (obj === null) {
			println("CardinalityProvider: ClosureParameter cardinality cannot be determined for null")
			return false
		}	
		return obj.function.isClosureParameterMulti
	}
	
	/**
	 * ListOperation.firstOrImplicit (e.g. ClosureParameter) can be null if parameter is implicit, so 
	 * better to determine the cardinality from the previous operation
	 */
	def boolean isClosureParameterMulti(InlineFunction obj) {
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
					return previousOperation.function.isMulti(false)
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
}