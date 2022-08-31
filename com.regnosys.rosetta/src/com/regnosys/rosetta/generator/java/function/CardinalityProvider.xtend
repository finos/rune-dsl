package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.RosettaCountOperation
import com.regnosys.rosetta.rosetta.RosettaDisjointExpression
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaMapPathValue
import com.regnosys.rosetta.rosetta.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase
import com.regnosys.rosetta.rosetta.RosettaTypedFeature
import com.regnosys.rosetta.rosetta.WithCardinality
import com.regnosys.rosetta.rosetta.simple.ClosureParameter
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ListOperation
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import com.regnosys.rosetta.rosetta.RosettaOnlyElement

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
					EcoreUtil2.getContainerOfType(obj, ListOperation).firstOrImplicit.isMulti(breakOnClosureParameter)
				else 
					obj.callable.isMulti(breakOnClosureParameter)
			}
			RosettaOnlyElement: {
				false
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
			ListOperation: {
				switch (obj.operationKind) {
					case REDUCE,
					case SUM,
					case JOIN,
					case MIN,
					case MAX,
					case FIRST,
					case LAST:
						false
					case SORT,
					case REVERSE,
					case FILTER,
					case FLATTEN,
					case DISTINCT:
						true
					case MAP: {
						if (obj.body.isMulti(breakOnClosureParameter)) 
							true 
						else 
							obj.receiver.isMulti(breakOnClosureParameter)
					}
				}
			}
			RosettaBinaryOperation: {
				false // check '+' operator
			}
			RosettaLiteral,
			RosettaTypedFeature,
			RosettaFeature,
			RosettaSynonymValueBase,
			RosettaCountOperation,
			RosettaAbsentExpression,
			RosettaOnlyExistsExpression,
			RosettaExistsExpression,
			RosettaContainsExpression,
			RosettaDisjointExpression,
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
		return obj.operation.isClosureParameterMulti
	}
	
	/**
	 * ListOperation.firstOrImplicit (e.g. ClosureParameter) can be null if parameter is implicit, so 
	 * better to determine the cardinality from the previous operation
	 */
	def boolean isClosureParameterMulti(ListOperation obj) {
		return obj.isPreviousOperationBodyMulti
	}
	
	/**
	 * Does the body of the previous list operation result in a list.
	 */
	def boolean isPreviousOperationBodyMulti(RosettaExpression expr) {
		if (expr instanceof ListOperation) {
			val previousOperation = expr.receiver
			if (previousOperation instanceof ListOperation) {
				// only map can increase a closure parameter's cardinality
				switch (previousOperation.operationKind) {
					case MAP:
						return previousOperation.body.isMulti(false)
					case FLATTEN:
						return false
					default:
						return previousOperation.isPreviousOperationBodyMulti
				}
			}
		}
		return false
	}
}