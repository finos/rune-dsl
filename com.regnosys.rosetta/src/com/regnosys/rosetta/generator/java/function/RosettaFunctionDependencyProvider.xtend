package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaParenthesisCalcExpression
import com.regnosys.rosetta.rosetta.simple.Function
import org.eclipse.emf.ecore.EObject

import static com.regnosys.rosetta.generator.util.Util.*
import com.regnosys.rosetta.rosetta.RosettaCountOperation

/**
 * A class that helps determine which RosettaFunctions a Rosetta object refers to
 */
class RosettaFunctionDependencyProvider {


	def Iterable<RosettaCallableWithArgs> functionDependencies(EObject object) {
		switch object {
			RosettaBinaryOperation: {
				functionDependencies(object.left) + functionDependencies(object.right)
			}
			RosettaConditionalExpression: {
				functionDependencies(object.^if) +
				functionDependencies(object.ifthen) +
				functionDependencies(object.elsethen)
			}
			RosettaExistsExpression: {
				functionDependencies(object.argument)
			}
			RosettaAlias: {
				functionDependencies(object.expression)
			}
			RosettaFeatureCall:
				functionDependencies(object.receiver)
			RosettaCallableWithArgsCall: {
				functionDependencies(object.callable) + functionDependencies(object.args)
			}
			Function: {
				newArrayList(object)
			}
			RosettaParenthesisCalcExpression: {
				functionDependencies(object.expression)
			}
			RosettaAbsentExpression: {
				functionDependencies(object.argument)
			}
			RosettaContainsExpression: {
				functionDependencies(object.contained) + functionDependencies(object.container)
			}
			RosettaCountOperation:{
				functionDependencies(object.argument)
			}
			RosettaExternalFunction,
			RosettaEnumValueReference,
			RosettaLiteral,
			RosettaCallableCall:
				emptyList()
			default:
				if(object !== null)
					throw new IllegalArgumentException('''«object?.eClass?.name» is not covered yet.''')
				else emptyList()
		}
	}
	
	def Iterable<RosettaCallableWithArgs> functionDependencies(Iterable<? extends EObject> objects) {
		distinctBy(objects.map[object | functionDependencies(object)].flatten, [f|f.name]);
	}
}
