package com.regnosys.rosetta.generator.java.calculation

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaArgumentFeature
import com.regnosys.rosetta.rosetta.RosettaArguments
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaFunction
import com.regnosys.rosetta.types.RUnionType
import com.regnosys.rosetta.types.RosettaTypeProvider
import org.eclipse.emf.ecore.EObject

import static com.regnosys.rosetta.generator.util.Util.*
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs

/**
 * A class that helps determine which RosettaFunctions a Rosetta object refers to
 */
class RosettaFunctionDependencyProvider {

	@Inject extension RosettaTypeProvider

	def Iterable<RosettaCallableWithArgs> functionDependencies(EObject object) {
		switch object {
			RosettaBinaryOperation: {
				functionDependencies(object.left) + functionDependencies(object.right)
			}
			RosettaArguments: {
				functionDependencies(object.features.filter(RosettaArgumentFeature)) +
				functionDependencies(object.aliases.filter(RosettaAlias))
			}
			RosettaArgumentFeature: {
				val rtype = object.expression.getRType

				if (rtype instanceof RUnionType) {
					functionDependencies(rtype.converter)
				} else {
					functionDependencies(object.expression)
				}
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
				val rtype = object.expression.getRType

				if (rtype instanceof RUnionType) {
					functionDependencies(rtype.converter)
				} else {
					functionDependencies(object.expression)
				}
			}
			RosettaFeatureCall:
				functionDependencies(object.receiver)
			RosettaCallableWithArgsCall: {
				functionDependencies(object.callable) + functionDependencies(object.args)
			}
			RosettaFunction: {
				newArrayList(object)
			}
			Function: {
				newArrayList(object)
			}
			default: newArrayList
		}
	}
	
	def Iterable<RosettaCallableWithArgs> functionDependencies(Iterable<? extends EObject> objects) {
		distinctBy(objects.map[object | functionDependencies(object)].flatten, [f|f.name]);
	}
}
