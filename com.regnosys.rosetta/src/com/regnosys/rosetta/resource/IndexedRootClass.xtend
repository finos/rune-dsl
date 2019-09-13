package com.regnosys.rosetta.resource

import com.regnosys.rosetta.rosetta.RosettaClass
import org.eclipse.emf.ecore.EObject

class IndexedRootClass extends IndexedFeature.IndexedStringFeature {

	override protected getKey() {
		'rootClass'
	}

	override getValue(EObject object) {
		if (object instanceof RosettaClass) {
			return String.valueOf(object.root)
		}
		null
	}

}
