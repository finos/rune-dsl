package com.regnosys.rosetta.scoping

import com.regnosys.rosetta.rosetta.RosettaQualifiableConfiguration
import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider
import org.eclipse.xtext.naming.QualifiedName
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch

class RosettaQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider {

	def protected QualifiedName qualifiedName(RosettaQualifiableConfiguration ele) {
		val modelQName = getFullyQualifiedName(ele.model)
		return modelQName?.append('is' + ele.QType.getName + 'Root')
	}

	def protected QualifiedName qualifiedName(FunctionDispatch ele) {
		val mainQName = computeFullyQualifiedNameFromNameAttribute(ele)
		if (mainQName !== null) {
			val enumValueName =  ele?.value
			if (enumValueName !== null) {
				return mainQName.append('Dispatch')
			}
		}
		return mainQName
	}

}
