package com.regnosys.rosetta.generator.java.expression

import com.regnosys.rosetta.generator.java.function.CardinalityProvider
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.generator.util.Util
import com.regnosys.rosetta.rosetta.simple.ListOperation
import com.regnosys.rosetta.types.RosettaTypeProvider
import javax.inject.Inject

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType

class ListOperationExtensions {
	
	
	@Inject protected RosettaTypeProvider typeProvider
	@Inject CardinalityProvider cardinalityProvider
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension Util
	
	def isItemMulti(ListOperation op) {
		cardinalityProvider.isClosureParameterMulti(op)
	}
	
	def getItemType(ListOperation op) {
		'''«IF funcExt.needsBuilder(op.receiver)»? extends «ENDIF»«typeProvider.getRType(op.receiver).name.toJavaType»'''
	}
	
	def getItemName(ListOperation op) {
		op.firstOrImplicit.getNameOrDefault.toDecoratedName
	}

	def isOutputMulti(ListOperation op) {
		cardinalityProvider.isMulti(op.body)
	}
	
	def getOutputRawType(ListOperation op) {
		'''«typeProvider.getRType(op.body).name.toJavaType»'''
	}
	
	def getOutputType(ListOperation op) {
		'''«IF funcExt.needsBuilder(op.body)»? extends «ENDIF»«op.outputRawType»'''
	}
	
	/**
	 * List MAP/FILTER operations can handle a list of lists, however it cannot be handled anywhere else (e.g. a list of list cannot be assigned to a func output or alias)
	 */
	def isOutputListOfLists(ListOperation op) {
		!op.isItemMulti && op.body !== null && op.isBodyExpressionMulti
	}
	
	/**
	 * List MAP/FILTER operations cannot handle a list of list of list
	 */
	def isOutputListOfListOfLists(ListOperation op) {
		op.isItemMulti && op.body !== null && op.isBodyExpressionMulti
	}
	
	def getPreviousListOperation(ListOperation op) {
		val previousOperation = op.receiver
		if (previousOperation instanceof ListOperation) {
			return previousOperation
		}
		return null
	}
	
	/**
	 * Does the list operation body expression increase the cardinality? 
	 * 
	 * E.g., 
	 * - from single to list, or from list to list of lists, would return true.
	 * - from single to single, or from list to list, or from list to single, would return false.
	 */
	private def isBodyExpressionMulti(ListOperation op) {
		cardinalityProvider.isMulti(op.body, true)
	}
}