package com.regnosys.rosetta.generator.util

import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import org.eclipse.xtext.EcoreUtil2

class RosettaFunctionExtensions {
	/** 
	 * spec functions do not have operation hence, do not provide an implementation
	 */
	def Boolean handleAsSpecFunction(Function function) {
		function.operations.nullOrEmpty && !function.isDispatchingFunction() && !function.handleAsEnumFunction
	}

	def Boolean handleAsEnumFunction(Function function) {
		function.operations.nullOrEmpty && !function.dispatchingFunctions.empty
	}
	
	def Boolean isDispatchingFunction(Function function) {
		(function instanceof FunctionDispatch)
	}


	def getDispatchingFunctions(Function function) {
		// TODO Look-up other Rosetta files?
		EcoreUtil2.getSiblingsOfType(function, FunctionDispatch).filter[it.name == function.name]
	}

	def getMainFunction(Function function) {
		// TODO Look-up other Rosetta files?
		if (function.isDispatchingFunction) {
			return EcoreUtil2.getSiblingsOfType(function, Function).filter [
				it.name == function.name && it.operations.nullOrEmpty
			].head
		}
	}

	dispatch def getOutput(Function function) {
		return function.output
	}

	dispatch def getOutput(FunctionDispatch function) {
		val mainFunction = function.mainFunction
		if (mainFunction !== null) {
			return mainFunction.output
		}
	}

	dispatch def getInputs(Function function) {
		return function.inputs
	}

	dispatch def getInputs(FunctionDispatch function) {
		val mainFunction = function.mainFunction
		if (mainFunction !== null) {
			return mainFunction.inputs
		}
	}
}
