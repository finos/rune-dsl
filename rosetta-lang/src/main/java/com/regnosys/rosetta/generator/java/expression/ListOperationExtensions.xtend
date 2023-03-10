package com.regnosys.rosetta.generator.java.expression

import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.types.RosettaTypeProvider
import javax.inject.Inject

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType
import com.regnosys.rosetta.rosetta.expression.InlineFunction
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation

class ListOperationExtensions {
	
	@Inject protected RosettaTypeProvider typeProvider
	@Inject RosettaFunctionExtensions funcExt
	
	def String getInputRawType(RosettaUnaryOperation op) {
		typeProvider.getRType(op.argument).name.toJavaType
	}
	
	def String getInputType(RosettaUnaryOperation op) {
		'''«IF funcExt.needsBuilder(op.argument)»? extends «ENDIF»«op.inputRawType»'''
	}
	
	def String getBodyRawType(InlineFunction op) {
		typeProvider.getRType(op).name.toJavaType
	}
	
	def String getOutputType(InlineFunction op) {
		'''«IF funcExt.needsBuilder(op.body)»? extends «ENDIF»«op.bodyRawType»'''
	}
}