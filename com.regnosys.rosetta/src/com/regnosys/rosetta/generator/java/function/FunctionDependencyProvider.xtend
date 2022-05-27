package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.RosettaCountOperation
import com.regnosys.rosetta.rosetta.RosettaDisjointExpression
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.RosettaParenthesisCalcExpression
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ListOperation
import java.util.Set
import org.eclipse.emf.ecore.EObject

import static com.regnosys.rosetta.generator.util.Util.*

/**
 * A class that helps determine which RosettaFunctions a Rosetta object refers to
 */
class FunctionDependencyProvider {


	def Set<RosettaCallableWithArgs> functionDependencies(EObject object) {
		switch object {
			RosettaBinaryOperation: {
				newHashSet(functionDependencies(object.left) + functionDependencies(object.right))
			}
			RosettaConditionalExpression: {
				newHashSet(
					functionDependencies(object.^if) +
					functionDependencies(object.ifthen) +
					functionDependencies(object.elsethen))
			}
			RosettaOnlyExistsExpression: {
				functionDependencies(object.args)
			}
			RosettaExistsExpression: {
				functionDependencies(object.argument)
			}
			RosettaFeatureCall:
				functionDependencies(object.receiver)
			RosettaCallableWithArgsCall: {
				newHashSet(functionDependencies(object.callable) + functionDependencies(object.args))
			}
			Function: {
				newHashSet(object)
			}
			RosettaParenthesisCalcExpression: {
				functionDependencies(object.expression)
			}
			RosettaAbsentExpression: {
				functionDependencies(object.argument)
			}
			RosettaContainsExpression: {
				newHashSet(functionDependencies(object.contained) + functionDependencies(object.container))
			}
			
			RosettaDisjointExpression: {
				newHashSet(functionDependencies(object.disjoint) + functionDependencies(object.container))
			}
			RosettaCountOperation: {
				functionDependencies(object.argument)
			}
			ListOperation: {
				newHashSet(functionDependencies(object.body) + functionDependencies(object.receiver))
			}
			ListLiteral: {
				newHashSet(object.elements.flatMap[functionDependencies])
			},
			RosettaExternalFunction,
			RosettaEnumValueReference,
			RosettaLiteral,
			RosettaCallableCall:
				emptySet()
			default:
				if(object !== null)
					throw new IllegalArgumentException('''«object?.eClass?.name» is not covered yet.''')
				else emptySet()
		}
	}
	
	def Set<RosettaCallableWithArgs> functionDependencies(Iterable<? extends EObject> objects) {
		distinctBy(objects.map[object | functionDependencies(object)].flatten, [f|f.name]).toSet;
	}
}
