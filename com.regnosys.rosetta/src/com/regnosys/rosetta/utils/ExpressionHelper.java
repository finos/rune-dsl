package com.regnosys.rosetta.utils;

import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import com.google.common.collect.Streams;
import com.regnosys.rosetta.rosetta.RosettaCallable;
import com.regnosys.rosetta.rosetta.expression.RosettaCallableCall;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;

public class ExpressionHelper {
	public boolean usesOutputParameter(RosettaExpression expr) {
		return !this.findOutputRef(expr, new Stack<String>()).isEmpty();
	}

	public List<RosettaCallable> findOutputRef(EObject ele, Stack<String> trace) {
		if (ele == null) {
			return Collections.emptyList();
		}
		if (ele instanceof ShortcutDeclaration) {
			ShortcutDeclaration sd = (ShortcutDeclaration)ele;
			trace.push(sd.getName());
			List<RosettaCallable> result = findOutputRef(sd.getExpression(), trace);
			if (result.isEmpty())
				trace.pop();
			return result;
		} else if (ele instanceof RosettaCallableCall) {
			RosettaCallableCall cc = (RosettaCallableCall)ele;
			if (cc.getCallable() instanceof Attribute &&
					cc.getCallable().eContainingFeature() == SimplePackage.Literals.FUNCTION__OUTPUT)
				return Collections.singletonList(cc.getCallable());
			return findOutputRef(cc.getCallable(), trace);
		} else {
			return Streams.concat(
						ele.eContents().stream(),
						ele.eCrossReferences().stream()
							.filter(ref -> (ref instanceof RosettaExpression) || (ref instanceof ShortcutDeclaration)))
					.<RosettaCallable>flatMap(ref -> this.findOutputRef(ref, trace).stream())
					.collect(Collectors.toList());
		}
	}

	public RosettaExpression getParentExpression(RosettaExpression e) {
		if (e instanceof RosettaFeatureCall) {
			return ((RosettaFeatureCall)e).getReceiver();
		} else if (e instanceof RosettaCallableCall) {
			return null;
		} else {
			throw new UnsupportedOperationException("Only exists expression type unsupported: " + e.getClass().getSimpleName());
		}
	}
	
	public boolean hasImplicitParent(RosettaExpression e) {
		return this.getImplicitParent(e) != null;
	}
	
	public Data getImplicitParent(RosettaExpression e) {
		return EcoreUtil2.getContainerOfType(e, Data.class);
	}
}
