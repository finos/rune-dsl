package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.expression.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.expression.ListLiteral
import java.util.Set
import org.eclipse.emf.ecore.EObject

import static com.regnosys.rosetta.generator.util.Util.*
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation
import com.regnosys.rosetta.rosetta.expression.NamedFunctionReference
import com.regnosys.rosetta.rosetta.expression.InlineFunction

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
			RosettaFunctionalOperation: {
				newHashSet(functionDependencies(object.argument) + functionDependencies(object.functionRef))
			}
			RosettaUnaryOperation: {
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
			NamedFunctionReference: {
				functionDependencies(object.function)
			}
			InlineFunction: {
				functionDependencies(object.body)
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
