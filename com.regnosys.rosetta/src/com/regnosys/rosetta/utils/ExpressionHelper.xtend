package com.regnosys.rosetta.utils

import com.regnosys.rosetta.rosetta.RosettaCallable
import com.regnosys.rosetta.rosetta.expression.RosettaCallableCall
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import java.util.List
import java.util.Stack
import org.eclipse.emf.ecore.EObject
import com.regnosys.rosetta.rosetta.simple.SimplePackage

class ExpressionHelper {

	def usesOutputParameter(RosettaExpression expr) {
		return !expr.findOutputRef(new Stack).nullOrEmpty
	}

	def List<RosettaCallable> findOutputRef(EObject ele, Stack<String> trace) {
		if (ele === null) {
			return emptyList
		}
		switch (ele) {
			ShortcutDeclaration: {
				trace.push(ele.name)
				val result = findOutputRef(ele.expression, trace)
				if (result.empty)
					trace.pop()
				return result
			}
			RosettaCallableCall: {
				if (ele.callable instanceof Attribute &&
					ele.callable.eContainingFeature === SimplePackage.Literals.FUNCTION__OUTPUT)
					return #[ele.callable]
				return findOutputRef(ele.callable, trace)
			}
		}
		return (ele.eContents + ele.eCrossReferences.filter [
			it instanceof RosettaExpression || it instanceof ShortcutDeclaration
		]).flatMap [
			findOutputRef(trace)
		].toList
	}
	
}
