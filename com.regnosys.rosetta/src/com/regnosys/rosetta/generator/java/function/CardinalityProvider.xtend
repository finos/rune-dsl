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
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaGroupByFeatureCall
import com.regnosys.rosetta.rosetta.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaMapPathValue
import com.regnosys.rosetta.rosetta.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.RosettaParenthesisCalcExpression
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase
import com.regnosys.rosetta.rosetta.RosettaTypedFeature
import com.regnosys.rosetta.rosetta.WithCardinality
import com.regnosys.rosetta.rosetta.simple.ClosureParameter
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ListOperation
import com.regnosys.rosetta.rosetta.simple.Operation
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2

class CardinalityProvider {
	
	def boolean isMulti(EObject obj) {
		if(obj === null) return false
		switch obj {
			RosettaFeatureCall: {
				if(obj.toOne) false else {
					if (obj.feature.isMulti) 
						true 
					else 
						obj.receiver.isMulti
				}
			}
			RosettaEnumValue:false
			WithCardinality: if(obj.card === null) false else obj.card.isIsMany
			RosettaCallableCall: {
				if(obj.toOne) 
					false 
				else if (obj.implicitReceiver) 
					EcoreUtil2.getContainerOfType(obj, ListOperation).firstOrImplicit.isMulti
				else 
					obj.callable.isMulti
			}
			RosettaCallableWithArgsCall: obj.callable.isMulti
			Function: if(obj.output === null) false else obj.output.isMulti
			ShortcutDeclaration: obj.expression.isMulti
			RosettaConditionalExpression: obj.ifthen.multi || obj.elsethen.multi
			RosettaParenthesisCalcExpression: obj.expression.isMulti
			ClosureParameter: obj.isClosureParameterMulti
			RosettaGroupByFeatureCall,
			ListLiteral,
			ListOperation: true
			RosettaLiteral,
			RosettaBinaryOperation, // check '+' operator
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
	
	def boolean expectedCardinalityMany(Operation op) {
		return if (op.path === null)
			op.assignRoot.isMulti
		else {
			val lastSegment = op.pathAsSegmentList.last
			if (lastSegment.index !== null) {
				false
			} else
				lastSegment.attribute.isMulti
		}
	}
	
	def boolean isClosureParameterMulti(ClosureParameter obj) {
		val previousOperation = obj.operation.receiver
		if (previousOperation instanceof ListOperation) {
			switch(previousOperation.operationKind) {
				case MAP:
					return previousOperation.body.isMulti
				case FILTER: {
					// Filter operation does not change cardinality, so check the next previous operation's cardinality
					val nextPreviousOperation = previousOperation.receiver
					return (nextPreviousOperation instanceof ListOperation) ? 
						nextPreviousOperation.isMulti : 
						false
				}	
				case FLATTEN:
					return false
				default: {
					println("CardinalityProviderisClosureParameterMulti: Cardinality not defined for operationKind: " + previousOperation.operationKind)
					return false
				}
			}
		}
		return false
	}
}