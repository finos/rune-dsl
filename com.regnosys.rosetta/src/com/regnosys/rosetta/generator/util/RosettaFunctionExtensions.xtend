package com.regnosys.rosetta.generator.util

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaTyped
import com.regnosys.rosetta.rosetta.simple.AssignPathRoot
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.rosetta.simple.SimplePackage
import com.regnosys.rosetta.types.RClassType
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RosettaTypeProvider
import org.eclipse.xtext.EcoreUtil2

class RosettaFunctionExtensions {

	@Inject RosettaTypeProvider typeProvider

	/** 
	 * 
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
		emptyList
	}

	def inputsAsArgs(ShortcutDeclaration alias) {
		val func = EcoreUtil2.getContainerOfType(alias, Function)
		getInputs(func).join(', ')[name]
	}

	dispatch def boolean needsBuilder(Void ele) {
		false
	}

	dispatch def boolean needsBuilder(RosettaTyped ele) {
		needsBuilder(ele.type)
	}

	dispatch def boolean needsBuilder(ShortcutDeclaration alias) {
		needsBuilder(typeProvider.getRType(alias.expression))
	}

	dispatch def boolean needsBuilder(AssignPathRoot root) {
		switch (root) {
			Attribute: root.type.needsBuilder
			ShortcutDeclaration: typeProvider.getRType(root.expression).needsBuilder
			default: false
		}
	}

	dispatch def boolean needsBuilder(RosettaType type) {
		switch (type) {
			RosettaClass,
			Data: true
			default: false
		}
	}

	dispatch def boolean needsBuilder(RType type) {
		switch (type) {
			RClassType,
			RDataType: true
			default: false
		}
	}

	def boolean isOutput(Attribute attr) {
		attr.eContainingFeature === SimplePackage.Literals.FUNCTION__OUTPUT
	}

	def implicitFirstArgument(RosettaCallableWithArgsCall element) {
		val dataContainer = EcoreUtil2.getContainerOfType(element, Data)
		val callable = element.callable
		if (callable instanceof Function) {
			if (dataContainer !== null) {
				if (callable.inputs.head !== null && callable.inputs.head.type == dataContainer) {
					return dataContainer
				}
			}
		}
	}
}
