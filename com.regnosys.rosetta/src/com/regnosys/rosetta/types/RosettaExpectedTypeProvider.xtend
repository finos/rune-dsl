package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaGroupByFeatureCall
import com.regnosys.rosetta.rosetta.RosettaMapPathValue
import com.regnosys.rosetta.rosetta.simple.Operation
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*
import com.regnosys.rosetta.rosetta.RosettaQualifiable

class RosettaExpectedTypeProvider {
	
	@Inject extension RosettaTypeProvider 
	
	def RType getExpectedType(EObject owner, EReference reference, int idx) {
		switch owner {
			RosettaExistsExpression case reference == ROSETTA_EXISTS_EXPRESSION__ARGUMENT: {
				if(owner.argument instanceof RosettaMapPathValue) {
					RBuiltinType.STRING
				} else {
					new RFeatureCallType(null)
				}
			} 
			RosettaAbsentExpression case reference == ROSETTA_ABSENT_EXPRESSION__ARGUMENT: {
				if(owner.argument instanceof RosettaMapPathValue) {
					RBuiltinType.STRING
				} else {
					new RFeatureCallType(null)
				}
			}
			RosettaQualifiable case reference == ROSETTA_QUALIFIABLE__EXPRESSION,
			RosettaConditionalExpression case reference == ROSETTA_CONDITIONAL_EXPRESSION__IF:
				RBuiltinType.BOOLEAN
			RosettaGroupByFeatureCall case reference == ROSETTA_GROUP_BY_FEATURE_CALL__CALL:
				owner.call.RType
			RosettaCallableWithArgsCall case reference == ROSETTA_CALLABLE_WITH_ARGS_CALL__ARGS: {
				if(idx >= 0 && owner.callable instanceof RosettaExternalFunction) {
					val fun =  (owner.callable as RosettaExternalFunction)
					if(idx >= fun.parameters.size) {
						null // add error type? 
					} else {
						val targetParam = fun.parameters.get(idx)
						targetParam.type.RType
					}
				}
			}
			Operation case reference == OPERATION__EXPRESSION: {
				if(owner.path === null)
					owner.assignRoot.RType
				else owner.pathAsSegmentList.last?.attribute?.RType
			}
		}
	}
	
	def RType getExpectedType(EObject owner, EReference reference) {
		owner.getExpectedType(reference, -1)
	}
}
