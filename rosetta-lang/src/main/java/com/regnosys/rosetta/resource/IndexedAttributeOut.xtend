package com.regnosys.rosetta.resource

import org.eclipse.emf.ecore.EObject

import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*
import com.regnosys.rosetta.resource.IndexedFeature.IndexedSingleKeyFeature

class IndexedAttributeOut extends IndexedSingleKeyFeature<Boolean> {

	override protected getKey() {
		'outAttr'
	}

	override getValue(EObject object) {
		if (object.eClass === ATTRIBUTE) {
			return object.eContainingFeature === FUNCTION__OUTPUT
		}
		return null
	}

	override protected toString(Boolean value) {
		if(value) Boolean.TRUE.toString else Boolean.FALSE.toString
	}

	override protected toValue(String string) {
		Boolean.valueOf(string)
	}

}
