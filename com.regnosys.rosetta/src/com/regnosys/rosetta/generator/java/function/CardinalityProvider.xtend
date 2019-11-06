package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaParenthesisCalcExpression
import com.regnosys.rosetta.rosetta.RosettaRootElement
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
			RosettaBinaryOperation: obj.left.isMulti || obj.right.multi // check this
			RosettaParenthesisCalcExpression: obj.expression.isMulti
			RosettaAlias: obj.expression.isMulti
			ListLiteral: true
			RosettaLiteral,
			RosettaRootElement,
			RosettaEnumValueReference: false
			default: {println(obj?.eClass?.name)false }
		}
	}
	
	def boolean expectedCardinalityMany(Operation op) {
		val assignTarget = if(op.path === null) op.assignRoot else op.pathAsSegmentList.last.attribute
		return assignTarget.isMulti
	}
}