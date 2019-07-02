package com.regnosys.rosetta.scoping

import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaArgumentFeature
import com.regnosys.rosetta.rosetta.RosettaArguments
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaPackage
import com.regnosys.rosetta.rosetta.RosettaQualifiableConfiguration
import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.nodemodel.util.NodeModelUtils

class RosettaQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider {

	def protected QualifiedName qualifiedName(RosettaArgumentFeature ele) {
		ele.createQNameForArgumentsChild(ele.eContainer as RosettaArguments)
	}

	def protected QualifiedName qualifiedName(RosettaAlias ele) {
		if (ele.eContainer.eClass == RosettaPackage.Literals.ROSETTA_ARGUMENTS) {
			return ele.createQNameForArgumentsChild(ele.eContainer as RosettaArguments)
		}
	}

	def protected QualifiedName qualifiedName(RosettaQualifiableConfiguration ele) {
		return QualifiedName.create('is' + ele.QType.getName + 'Root')
	}

	/**
	 * calculations and arguments should share the same namespace
	 */
	def private QualifiedName createQNameForArgumentsChild(RosettaNamed ele, RosettaArguments arguments) {
		val nodes = NodeModelUtils.findNodesForFeature(arguments,
			RosettaPackage.Literals.ROSETTA_ARGUMENT_FEATURE__EXPRESSION)
		if (!nodes.isNullOrEmpty) {
			val parentQname = QualifiedName.create(nodes.join('', [text.trim]).split('\\.'))
			return parentQname.append(ele.name)
		}
	}

}
