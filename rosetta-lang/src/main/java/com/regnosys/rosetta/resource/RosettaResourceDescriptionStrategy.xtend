package com.regnosys.rosetta.resource

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaNamed
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy
import org.eclipse.xtext.util.IAcceptor

class RosettaResourceDescriptionStrategy extends DefaultResourceDescriptionStrategy {

	@Inject extension IQualifiedNameProvider
	@Inject Indexed indexed

	override createEObjectDescriptions(EObject eObject, IAcceptor<IEObjectDescription> acceptor) {
		val qName = eObject.fullyQualifiedName
		if (qName === null) {
			return super.createEObjectDescriptions(eObject, acceptor)
		}
		return switch (eObject) {
			RosettaNamed: {
				val descr = indexed.createDescription(eObject)
				if (descr !== null)
					acceptor.accept(descr)
				true
			}
			default:
				super.createEObjectDescriptions(eObject, acceptor)
		}
	}
}
