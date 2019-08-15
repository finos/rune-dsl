package com.regnosys.rosetta.generator.util

import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import org.eclipse.xtext.EcoreUtil2

class RosettaFunctionExtensions {

	def Boolean handleAsSpecFunction(Function function) {
		function.operations.nullOrEmpty && (!function.conditions.nullOrEmpty || !function.postConditions.nullOrEmpty) &&
			!(function instanceof FunctionDispatch)
	}

	def Boolean handleAsExternalFunction(Function function) {
		function.operations.nullOrEmpty && function.conditions.nullOrEmpty && function.postConditions.nullOrEmpty &&
			!(function instanceof FunctionDispatch) && function.getDispatchingFunctions().nullOrEmpty

	}

	def getDispatchingFunctions(Function function) {
		// TODO Look-up other Rosetta files?
		EcoreUtil2.getSiblingsOfType(function, FunctionDispatch).filter[it.name == function.name]
	}
}
