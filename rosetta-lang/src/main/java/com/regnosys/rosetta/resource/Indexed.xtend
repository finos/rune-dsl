package com.regnosys.rosetta.resource

import com.google.common.collect.Maps
import com.google.inject.Inject
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.resource.EObjectDescription
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.naming.IQualifiedNameProvider

class Indexed {
	
	public static val ATTRIBUTE_OUT = new IndexedAttributeOut
	
	public static val INDEXED_FEATURES = #[
		Indexed.ATTRIBUTE_OUT
	]

	@Inject IQualifiedNameProvider qNames

	def IEObjectDescription createDescription(EObject obj) {
		createDescription(obj, qNames.getFullyQualifiedName(obj))
	}

	def IEObjectDescription createDescription(EObject obj, QualifiedName name) {
		if (name === null)
			return null

		val Map<String, String> userData = Maps.newHashMapWithExpectedSize(INDEXED_FEATURES.size)
		for (idx : Indexed.INDEXED_FEATURES) {
			idx.index(obj, userData)
		}

		return EObjectDescription.create(name, obj, userData)
	}

}
