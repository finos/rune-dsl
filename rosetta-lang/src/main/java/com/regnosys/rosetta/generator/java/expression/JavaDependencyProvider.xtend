package com.regnosys.rosetta.generator.java.expression

import com.regnosys.rosetta.rosetta.simple.Function

import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import org.eclipse.xtext.EcoreUtil2
import javax.inject.Inject
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.rosetta.RosettaRule
import com.rosetta.util.types.JavaClass
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RDataType
import java.util.List
import com.regnosys.rosetta.rosetta.expression.TranslateDispatchOperation
import com.regnosys.rosetta.utils.TranslateUtil
import com.regnosys.rosetta.types.TypeSystem

/**
 * A class that helps determine which RosettaFunctions a Rosetta object refers to
 */
class JavaDependencyProvider {
	@Inject RObjectFactory rTypeBuilderFactory
	@Inject RosettaTypeProvider typeProvider
	@Inject TypeSystem typeSystem
	@Inject extension JavaTypeTranslator
	@Inject TranslateUtil translateUtil

	def List<JavaClass<?>> javaDependencies(RosettaExpression expression) {
		val rosettaSymbols = EcoreUtil2.eAllOfType(expression, RosettaSymbolReference).map[it.symbol]
		val deepFeatureCalls = EcoreUtil2.eAllOfType(expression, RosettaDeepFeatureCall)
		val translateDispatchOperations = EcoreUtil2.eAllOfType(expression, TranslateDispatchOperation)
		val actualDispatches = newArrayList
		for (op : translateDispatchOperations) {
			val inputTypes = op.inputs.map[typeProvider.getRType(it)]
			val outputType = typeSystem.typeCallToRType(op.outputType)
			if (inputTypes.size !== 1 || !typeSystem.isSubtypeOf(inputTypes.head, outputType)) {
				val match = translateUtil.findMatches(translateUtil.getSource(op), outputType, inputTypes).last
				actualDispatches.add(rTypeBuilderFactory.buildRFunction(match))
			}
		}
		(
			rosettaSymbols.filter(Function).map[rTypeBuilderFactory.buildRFunction(it).toFunctionJavaClass] +
			rosettaSymbols.filter(RosettaRule).map[rTypeBuilderFactory.buildRFunction(it).toFunctionJavaClass] +
			deepFeatureCalls.map[typeProvider.getRType(receiver)].filter(RDataType).map[data.toDeepPathUtilJavaClass] +
			actualDispatches.map[toFunctionJavaClass]
		).toSet.sortBy[it.simpleName]
	}

	def List<JavaClass<?>> javaDependencies(Iterable<? extends RosettaExpression> expressions) {
		expressions.flatMap [
			javaDependencies
		].toSet.sortBy[it.simpleName]
	}
}
