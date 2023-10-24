package com.regnosys.rosetta.generator.java.function

import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
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
import com.regnosys.rosetta.rosetta.expression.InlineFunction
import com.regnosys.rosetta.rosetta.expression.RosettaReference
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import com.regnosys.rosetta.rosetta.RosettaSymbol
import com.regnosys.rosetta.types.RFunction
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import org.eclipse.xtext.EcoreUtil2
import javax.inject.Inject
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.rosetta.RosettaRule

/**
 * A class that helps determine which RosettaFunctions a Rosetta object refers to
 */
class FunctionDependencyProvider {
	@Inject RObjectFactory rTypeBuilderFactory

	def Set<Function> functionDependencies(EObject object) {
		switch object {
			RosettaBinaryOperation: {
				newHashSet(functionDependencies(object.left) + functionDependencies(object.right))
			}
			RosettaConditionalExpression: {
				newHashSet(
					functionDependencies(object.^if) + functionDependencies(object.ifthen) +
						functionDependencies(object.elsethen))
			}
			RosettaOnlyExistsExpression: {
				functionDependencies(object.args)
			}
			RosettaFunctionalOperation: {
				newHashSet(functionDependencies(object.argument) + functionDependencies(object.function))
			}
			RosettaUnaryOperation: {
				functionDependencies(object.argument)
			}
			RosettaFeatureCall:
				functionDependencies(object.receiver)
			RosettaSymbolReference: {
				newHashSet(functionDependencies(object.symbol) + functionDependencies(object.args))
			}
			Function: {
				newHashSet(object)
			}
			InlineFunction: {
				functionDependencies(object.body)
			}
			ListLiteral: {
				newHashSet(object.elements.flatMap[functionDependencies])
			}
			,
			RosettaExternalFunction,
			RosettaEnumValueReference,
			RosettaLiteral,
			RosettaReference,
			RosettaSymbol:
				emptySet()
			default:
				if (object !== null)
					throw new IllegalArgumentException('''«object?.eClass?.name» is not covered yet.''')
				else
					emptySet()
		}
	}

	def Set<Function> functionDependencies(Iterable<? extends EObject> objects) {
		distinctBy(objects.map[object|functionDependencies(object)].flatten, [f|f.name]).toSet;
	}

	def Set<RFunction> rFunctionDependencies(RosettaExpression expression) {
		val rosettaSymbols = EcoreUtil2.eAllOfType(expression, RosettaSymbolReference).map[it.symbol]
		(rosettaSymbols.filter(Function).map[rTypeBuilderFactory.buildRFunction(it)] +
			rosettaSymbols.filter(RosettaRule).map[rTypeBuilderFactory.buildRFunction(it)]).toSet
	}

	def Set<RFunction> rFunctionDependencies(Iterable<? extends RosettaExpression> expressions) {
		expressions.flatMap [
			rFunctionDependencies
		].toSet
	}
}
