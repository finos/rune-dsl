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
import java.util.Set

/**
 * A class that helps determine which dependencies a Rosetta expression needs
 */
class JavaDependencyProvider {
	@Inject RObjectFactory rTypeBuilderFactory
	@Inject RosettaTypeProvider typeProvider
	@Inject extension JavaTypeTranslator

	private def void javaDependencies(RosettaExpression expression, Set<JavaClass<?>> result, Set<RosettaExpression> visited) {
		if (visited.add(expression)) {
			val rosettaSymbols = EcoreUtil2.eAllOfType(expression, RosettaSymbolReference).map[it.symbol]
			val deepFeatureCalls = EcoreUtil2.eAllOfType(expression, RosettaDeepFeatureCall)

			result.addAll(rosettaSymbols.filter(Function).map[rTypeBuilderFactory.buildRFunction(it).toFunctionJavaClass])
			result.addAll(rosettaSymbols.filter(RosettaRule).map[rTypeBuilderFactory.buildRFunction(it).toFunctionJavaClass])
			result.addAll(deepFeatureCalls.map[typeProvider.getRMetaAnnotatedType(receiver)].map[RType].filter(RDataType).map[toDeepPathUtilJavaClass])
		}
	}

	def List<JavaClass<?>> javaDependencies(RosettaExpression expression) {
		val result = newHashSet
		javaDependencies(expression, result, newHashSet)
		result.sortBy[it.simpleName]
	}

	def List<JavaClass<?>> javaDependencies(Iterable<? extends RosettaExpression> expressions) {
		val result = newHashSet
		val visited = newHashSet
		expressions.forEach[javaDependencies(it, result, visited)]
		result.sortBy[it.simpleName]
	}
}
