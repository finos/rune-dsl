package com.regnosys.rosetta.scoping

import com.regnosys.rosetta.rosetta.RosettaPackage
import com.regnosys.rosetta.rosetta.simple.SimplePackage
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.linking.impl.DefaultLinkingService
import org.eclipse.xtext.linking.impl.IllegalNodeException
import org.eclipse.xtext.nodemodel.INode

class RosettaLinkingService extends DefaultLinkingService {

	override getLinkedObjects(EObject context, EReference ref, INode node) throws IllegalNodeException {
		val defaultImpl = super.getLinkedObjects(context, ref, node)
		if (ref === RosettaPackage.Literals.ROSETTA_CALLABLE_WITH_ARGS_CALL__CALLABLE) {
			return defaultImpl.filter[eClass !== SimplePackage.Literals.FUNCTION_DISPATCH].toList
		}
		return defaultImpl
	}

}
