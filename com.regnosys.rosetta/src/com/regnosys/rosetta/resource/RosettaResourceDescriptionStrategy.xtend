package com.regnosys.rosetta.resource

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaQualifiableConfiguration
import com.regnosys.rosetta.rosetta.RosettaQualifiableType
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.resource.EObjectDescription
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy
import org.eclipse.xtext.util.IAcceptor

class RosettaResourceDescriptionStrategy extends DefaultResourceDescriptionStrategy {

	@Inject extension IQualifiedNameProvider

	public static val SOURCE_NAME = 'sourceName'
	public static val ROOT_CLASS = 'rootClass'
	public static val CONF_ROOT_TYPE = 'confRootType'

	override createEObjectDescriptions(EObject eObject, IAcceptor<IEObjectDescription> acceptor) {
		val qName = eObject.fullyQualifiedName
		if (qName === null) {
			return super.createEObjectDescriptions(eObject, acceptor)
		}
		
		if (eObject instanceof RosettaClass) {
			if (eObject.root) {
				acceptor.accept(EObjectDescription.create(
					qName,
					eObject,
					#{ROOT_CLASS -> String.valueOf(eObject.root)}
				))
			} else {
				super.createEObjectDescriptions(eObject, acceptor)
			}
			return true
		} else if (eObject instanceof RosettaQualifiableConfiguration) {
			if (eObject.QType !== RosettaQualifiableType.NONE) {
				acceptor.accept(EObjectDescription.create(
					qName,
					eObject,
					#{
						CONF_ROOT_TYPE -> String.valueOf(eObject.QType.getName)
					}
				))
				return false
			}
		}
		return super.createEObjectDescriptions(eObject, acceptor)
	}
}
