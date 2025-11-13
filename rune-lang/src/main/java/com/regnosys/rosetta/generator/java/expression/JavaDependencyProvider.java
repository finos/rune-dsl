package com.regnosys.rosetta.generator.java.expression;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.rosetta.util.types.JavaClass;

import jakarta.inject.Inject;

/**
 * A class that helps determine which dependencies a Rosetta expression needs
 */
public class JavaDependencyProvider {
	@Inject
	private RObjectFactory rTypeBuilderFactory;
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private JavaTypeTranslator typeTranslator;

	private void javaDependencies(RosettaExpression expression, Set<JavaClass<?>> result, Set<RosettaExpression> visited) {
		if (visited.add(expression)) {
			var rosettaSymbols = 
				EcoreUtil2.eAllOfType(expression, RosettaSymbolReference.class)
					.stream()
					.map(RosettaSymbolReference::getSymbol)
					.toList();
			var deepFeatureCalls = EcoreUtil2.eAllOfType(expression, RosettaDeepFeatureCall.class);

			rosettaSymbols.stream()
				.filter(s -> s instanceof Function)
				.map(s -> rTypeBuilderFactory.buildRFunction((Function)s))
				.map(typeTranslator::toFunctionJavaClass)
				.forEach(result::add);
			rosettaSymbols.stream()
				.filter(s -> s instanceof RosettaRule)
				.map(s -> rTypeBuilderFactory.buildRFunction((RosettaRule)s))
				.map(typeTranslator::toFunctionJavaClass)
				.forEach(result::add);
			rosettaSymbols.stream()
				.filter(s -> s instanceof ShortcutDeclaration)
				.forEach(s -> javaDependencies(((ShortcutDeclaration)s).getExpression(), result, visited));
			deepFeatureCalls.stream()
				.map(dfc -> typeProvider.getRMetaAnnotatedType(dfc.getReceiver()).getRType())
				.map(t -> t instanceof RChoiceType ? ((RChoiceType)t).asRDataType() : t)
				.filter(t -> t instanceof RDataType)
				.map(t -> typeTranslator.toDeepPathUtilJavaClass((RDataType)t))
				.forEach(result::add);
		}
	}

	public List<JavaClass<?>> javaDependencies(RosettaExpression expression) {
		Set<JavaClass<?>> result = new HashSet<>();
		javaDependencies(expression, result, new HashSet<>());
		return result.stream().sorted(Comparator.comparing(dep -> dep.getSimpleName())).toList();
	}

	public List<JavaClass<?>> javaDependencies(Iterable<? extends RosettaExpression> expressions) {
		Set<JavaClass<?>> result = new HashSet<>();
		Set<RosettaExpression> visited = new HashSet<>();
		expressions.forEach(expr -> javaDependencies(expr, result, visited));
		return result.stream().sorted(Comparator.comparing(dep -> dep.getSimpleName())).toList();
	}
}
