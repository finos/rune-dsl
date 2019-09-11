package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.RosettaAttributeBase
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaFunction
import org.eclipse.emf.ecore.EObject
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.simple.Function

class CardinalityProvider {
	
	def boolean isMulti(EObject obj) {
		switch obj {
			RosettaFeatureCall: 
				if (obj.receiver.isMulti) true else obj.feature.isMulti
			RosettaAttributeBase: obj.card.isIsMany
			RosettaCallableCall: obj.callable.isMulti
			RosettaCallableWithArgsCall: obj.callable.isMulti
			RosettaFunction: obj.output.card.isIsMany
			Function: obj.output?.isMulti
			Attribute: obj.card.isMany
			default: {println(obj.eClass) false}
		}
	} 
}