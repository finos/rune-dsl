package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaAlias
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
import com.regnosys.rosetta.rosetta.RosettaParenthesisCalcExpression
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase
import com.regnosys.rosetta.rosetta.RosettaTypedFeature
import com.regnosys.rosetta.rosetta.WithCardinality
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.Operation
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import org.eclipse.emf.ecore.EObject

class CardinalityProvider {
	
	def boolean isMulti(EObject obj) {
		if(obj === null) return false
		switch obj {
			RosettaFeatureCall: {
				if(obj.toOne) false else {
					if (obj.receiver.isMulti) true else obj.feature.isMulti
				}
			}
			
			RosettaEnumValue:false
			WithCardinality: if(obj.card === null) false else obj.card.isIsMany
			RosettaCallableCall: if(obj.toOne) false else obj.callable.isMulti
			RosettaCallableWithArgsCall: obj.callable.isMulti
			Function: if(obj.output === null) false else obj.output.isMulti
			ShortcutDeclaration: obj.expression.isMulti
			RosettaConditionalExpression: obj.ifthen.multi || obj.elsethen.multi
			RosettaParenthesisCalcExpression: obj.expression.isMulti
			RosettaAlias: obj.expression.isMulti
			RosettaGroupByFeatureCall,
			ListLiteral: true
			RosettaLiteral,
			RosettaBinaryOperation, // check '+' operator
			RosettaTypedFeature,
			RosettaFeature,
			RosettaSynonymValueBase,
			RosettaCountOperation,
			RosettaAbsentExpression,
			RosettaExistsExpression,
			RosettaContainsExpression,
			RosettaDisjointExpression,
			RosettaRootElement,
			RosettaEnumValueReference: false
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
}