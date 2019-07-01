package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.RosettaAttributeBase
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaFunction
import org.eclipse.emf.ecore.EObject

class CardinalityProvider {
	
	def boolean isMulti(EObject obj) {
		switch obj {
			RosettaFeatureCall: if (obj.receiver.isMulti) true else obj.feature.isMulti
			RosettaAttributeBase: obj.card.isIsMany
			RosettaCallableWithArgsCall: obj.callable.isMulti
			RosettaFunction: obj.output.card.isIsMany
			default: false
		}
	} 
}