package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import org.eclipse.emf.ecore.EObject

class ConvertableCardinalityProvider extends CardinalityProvider {

	override boolean isMulti(EObject obj) {
		val multi = super.isMulti(obj)
		return switch obj {
			RosettaFeatureCall case multi:
				!obj.toOne
			RosettaCallableCall case multi:
				!obj.toOne
			default:
				multi
		}
	}
}
