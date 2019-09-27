package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.WithCardinality
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import org.eclipse.emf.ecore.EObject
import com.regnosys.rosetta.rosetta.simple.Function

class CardinalityProvider {
	
	def boolean isMulti(EObject obj) {
		if(obj === null) return false
		switch obj {
			RosettaFeatureCall: {
				if(obj.toOne) false else {
					if (obj.receiver.isMulti) true else obj.feature.isMulti
				}
			}
			WithCardinality: if(obj.card === null) false else obj.card.isIsMany
			RosettaCallableCall: if(obj.toOne) false else obj.callable.isMulti
			RosettaCallableWithArgsCall: obj.callable.isMulti
			Function: if(obj.output === null) false else obj.output.isMulti
			ShortcutDeclaration: obj.expression.isMulti
			RosettaConditionalExpression: obj.ifthen.multi || obj.elsethen.multi
			RosettaBinaryOperation: obj.left.isMulti || obj.right.multi // check this 
			ListLiteral: true
			RosettaLiteral,
			RosettaRecordType,
			RosettaEnumValueReference: false
			default: {println(obj?.eClass?.name)false }
		}
	} 
}