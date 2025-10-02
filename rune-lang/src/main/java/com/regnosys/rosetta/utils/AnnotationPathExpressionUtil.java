package com.regnosys.rosetta.utils;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.simple.AnnotationDeepPath;
import com.regnosys.rosetta.rosetta.simple.AnnotationPath;
import com.regnosys.rosetta.rosetta.simple.AnnotationPathAttributeReference;
import com.regnosys.rosetta.rosetta.simple.AnnotationPathExpression;
import com.regnosys.rosetta.rosetta.simple.Attribute;

public class AnnotationPathExpressionUtil {
	public <T> T fold(
			AnnotationPathExpression expr,
			Function<Attribute, T> caseAttributeReference,
			Function<Attribute, T> caseImplicitVariable,
			BiFunction<T, AnnotationPath, T> casePath,
			BiFunction<T, AnnotationDeepPath, T> caseDeepPath
	) {
		Attribute targetAttr = getTargetAttribute(expr);
		return doSwitch(
				expr,
				(ref) -> caseAttributeReference.apply(targetAttr),
				(item) -> caseImplicitVariable.apply(targetAttr),
				(path) -> casePath.apply(fold(path.getReceiver(), caseAttributeReference, caseImplicitVariable, casePath, caseDeepPath), path),
				(deepPath) -> caseDeepPath.apply(fold(deepPath.getReceiver(), caseAttributeReference, caseImplicitVariable, casePath, caseDeepPath), deepPath)
			);
	}
	
	public Attribute getTargetAttribute(AnnotationPathExpression expr) {
		return doSwitch(
				expr,
				(ref) -> ref.getAttribute(),
				(item) -> EcoreUtil2.getContainerOfType(expr, Attribute.class),
				(path) -> path.getAttribute(),
				(deepPath) -> deepPath.getAttribute()
			);
	}
	
	private <T> T doSwitch(
			AnnotationPathExpression expr,
			Function<AnnotationPathAttributeReference, T> caseAttributeReference,
			Function<RosettaImplicitVariable, T> caseImplicitVariable,
			Function<AnnotationPath, T> casePath,
			Function<AnnotationDeepPath, T> caseDeepPath
	) {
		if (expr instanceof AnnotationPathAttributeReference) {
			return caseAttributeReference.apply((AnnotationPathAttributeReference) expr);
		} else if (expr instanceof RosettaImplicitVariable) {
			return caseImplicitVariable.apply((RosettaImplicitVariable) expr);
		} else if (expr instanceof AnnotationPath) {
			return casePath.apply((AnnotationPath) expr);
		} else if (expr instanceof AnnotationDeepPath) {
			return caseDeepPath.apply((AnnotationDeepPath) expr);
		}
		throw new IllegalArgumentException("Path expression " + expr + " not supported");
	}
}
