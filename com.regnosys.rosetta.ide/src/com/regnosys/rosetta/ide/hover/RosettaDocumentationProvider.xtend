package com.regnosys.rosetta.ide.hover

import org.eclipse.xtext.documentation.IEObjectDocumentationProvider
import org.eclipse.emf.ecore.EObject
import com.regnosys.rosetta.rosetta.RosettaDefinable

class RosettaDocumentationProvider implements IEObjectDocumentationProvider {

	// If we want to the hover to show the type/cardinality, then we can use the below providers.
	// @Inject extension RosettaTypeProvider
	// @Inject extension CardinalityProvider

	override getDocumentation(EObject o) {
		return if(o instanceof RosettaDefinable) o.definition else null
	}

}
