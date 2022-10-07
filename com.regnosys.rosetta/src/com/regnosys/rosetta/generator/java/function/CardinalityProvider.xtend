package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaCallableCall
import com.regnosys.rosetta.rosetta.expression.RosettaCallableWithArgsCall
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
import org.eclipse.xtext.EcoreUtil2
import com.regnosys.rosetta.rosetta.expression.ReduceOperation
import com.regnosys.rosetta.rosetta.expression.FilterOperation
import com.regnosys.rosetta.rosetta.expression.SortOperation
import com.regnosys.rosetta.rosetta.expression.MapOperation
import com.regnosys.rosetta.rosetta.expression.NamedFunctionReference
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
import com.regnosys.rosetta.rosetta.expression.FunctionReference

class CardinalityProvider {
	
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
			RosettaCallableCall: {
				if (obj.implicitReceiver) 
					EcoreUtil2.getContainerOfType(obj, InlineFunction).firstOrImplicit.isMulti(breakOnClosureParameter)
				else 
					obj.callable.isMulti(breakOnClosureParameter)
			}
			RosettaCallableWithArgsCall: {
				obj.callable.isMulti(breakOnClosureParameter)
			}
			Function: if(obj.output === null) false else obj.output.isMulti(breakOnClosureParameter)
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
			FilterOperation: true
			MapOperation: {
				if (obj.functionRef.isMulti(breakOnClosureParameter)) {
					true
				} else {
					obj.argument.isMulti(breakOnClosureParameter)
				}
			}
			SortOperation: true
			NamedFunctionReference: obj.function.isMulti(breakOnClosureParameter)
			InlineFunction: obj.body.isMulti(breakOnClosureParameter)
			FirstOperation,
			LastOperation,
			SumOperation,
			MinOperation,
			MaxOperation,
			RosettaAbsentExpression,
			RosettaCountOperation,
			RosettaExistsExpression,
			RosettaOnlyElement:
				false
			DistinctOperation,
			FlattenOperation,
			ReverseOperation:
				true
			RosettaBinaryOperation: {
				false // check '+' operator
			}
			RosettaLiteral,
			RosettaTypedFeature,
			RosettaFeature,
			RosettaSynonymValueBase,
			RosettaOnlyExistsExpression,
			RosettaRootElement,
			RosettaEnumValueReference,
			RosettaMapPathValue: false
			default: {println("CardinalityProvider: Cardinality not defined for: " +obj?.eClass?.name)false }
		}
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
			return op.argument.isOutputListOfLists
		}
		return false
	}
	
	def isItemMulti(FunctionReference op) {
		if (op instanceof InlineFunction) {
			return op.isClosureParameterMulti
		} else if (op instanceof NamedFunctionReference) {
			val f = op.function
			switch f {
				Function: isMulti(f.inputs.head)
				default: false
			}
		}
	}
	
	/**
	 * Does the body of the previous list operation result in a list.
	 */
	def boolean isPreviousOperationBodyMulti(RosettaUnaryOperation expr) {
		val previousOperation = expr.argument
		if (previousOperation instanceof RosettaUnaryOperation) {
			// only map can increase a closure parameter's cardinality
			switch (previousOperation) {
				MapOperation:
					return previousOperation.functionRef.isMulti(false)
				FlattenOperation:
					return false
				default:
					return previousOperation.isPreviousOperationBodyMulti
			}
		}
		return false
	}
	
	
	/**
	 * List MAP/FILTER operations can handle a list of lists, however it cannot be handled anywhere else (e.g. a list of list cannot be assigned to a func output or alias)
	 */
	def boolean isOutputListOfLists(RosettaExpression op) {
		if (op instanceof FlattenOperation || !(op instanceof CanHandleListOfLists)) {
			false
		}
		else if (op instanceof MapOperation) {
			if (op.functionRef.isItemMulti) {
				op.functionRef.isBodyExpressionMulti
			} else {
				op.functionRef.isBodyExpressionMulti && op.isPreviousOperationMulti
			}
		}
		else if (op instanceof RosettaUnaryOperation) {
			val previousListOp = op.argument
			previousListOp.isOutputListOfLists
		} else {
			false
		}
	}
	
	def isPreviousOperationMulti(RosettaUnaryOperation op) {
		isMulti(op.argument)
	}
	
	/**
	 * Does the list operation body expression increase the cardinality? 
	 * 
	 * E.g., 
	 * - from single to list, or from list to list of lists, would return true.
	 * - from single to single, or from list to list, or from list to single, would return false.
	 */
	def isBodyExpressionMulti(FunctionReference op) {
		if (op instanceof InlineFunction) {
			op.body !== null && isMulti(op.body, false)
		} else {
			isMulti(op, false)
		}
	}

	/**
	 * Nothing handles a list of list of list
	 */
	def boolean isOutputListOfListOfLists(RosettaExpression op) {
		if (op instanceof MapOperation) {
			val previousListOp = op.argument
			previousListOp.isOutputListOfLists && op.functionRef.isBodyExpressionWithSingleInputMulti
		} 
		else {
			false
		}
	}
	
	def isBodyExpressionWithSingleInputMulti(FunctionReference op) {
		if (op instanceof InlineFunction) {
			op.body !== null && isMulti(op.body, true)
		} else {
			isMulti(op, false)
		}
	}
}