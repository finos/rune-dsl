package com.regnosys.rosetta.scoping

import com.regnosys.rosetta.rosetta.RosettaQualifiableConfiguration
import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider
import org.eclipse.xtext.naming.QualifiedName

class RosettaQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider {


	def protected QualifiedName qualifiedName(RosettaQualifiableConfiguration ele) {
		val modelQName = getFullyQualifiedName(ele.model)
		return modelQName?.append('is' + ele.QType.getName + 'Root')
	}

}
