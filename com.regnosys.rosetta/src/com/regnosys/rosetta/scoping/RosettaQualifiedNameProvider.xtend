package com.regnosys.rosetta.scoping

import com.regnosys.rosetta.rosetta.RosettaQualifiableConfiguration
import com.regnosys.rosetta.rosetta.simple.Annotation
import com.regnosys.rosetta.rosetta.simple.Attribute
import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider
import org.eclipse.xtext.naming.QualifiedName

class RosettaQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider {

	def protected QualifiedName qualifiedName(RosettaQualifiableConfiguration ele) {
		return QualifiedName.create('is' + ele.QType.getName + 'Root')
	}

	def protected QualifiedName qualifiedName(Attribute ele) {
		if (ele.eContainer instanceof Annotation)
			return QualifiedName.create(ele.name)
	}

}
