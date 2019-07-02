package com.regnosys.rosetta

import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaArguments
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallable
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaGroupByFeatureCall
import com.regnosys.rosetta.rosetta.RosettaQualifiable
import com.regnosys.rosetta.rosetta.RosettaSynonym
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaTyped
import com.regnosys.rosetta.rosetta.RosettaWhenPresentExpression
import java.util.LinkedHashSet
import java.util.Set
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject

class RosettaExtensions {
	
	def Set<RosettaClass> getAllSuperTypes(RosettaClass clazz) {
		doGetSuperTypes(clazz, newLinkedHashSet)
	}
	
	private def Set<RosettaClass> doGetSuperTypes(RosettaClass clazz, Set<RosettaClass> seenClasses) {
		if(clazz !== null && seenClasses.add(clazz)) 
			doGetSuperTypes(clazz.superType, seenClasses)
		return seenClasses
	}

	def getAllAttributes(RosettaClass clazz) {
		clazz.allSuperTypes.map[regularAttributes].flatten	
	}
	
	def Set<RosettaEnumeration> getAllSuperEnumerations(RosettaEnumeration e) {
		doGetSuperEnumerations(e, newLinkedHashSet)
	}
	
	private def Set<RosettaEnumeration> doGetSuperEnumerations(RosettaEnumeration e, Set<RosettaEnumeration> seenEnums) {
		if(e !== null && seenEnums.add(e)) 
			doGetSuperEnumerations(e.superType, seenEnums)
		return seenEnums
	}

	def getAllEnumValues(RosettaEnumeration e) {
		e.allSuperEnumerations.map[enumValues].flatten
	}
	
	def Set<RosettaSynonym> getAllSynonyms(RosettaSynonym s) {
		doGetSynonyms(s, newLinkedHashSet)
	}
		
	private def Set<RosettaSynonym> doGetSynonyms(RosettaSynonym s, Set<RosettaSynonym> seenSynonyms) {
		if(s !== null && seenSynonyms.add(s)) 
			doGetSynonyms(s, seenSynonyms)
		return seenSynonyms		
	}
	
	def classUsages(RosettaArguments arguments) {
		arguments.eAllContents.filter(RosettaCallableCall).filter[callable instanceof RosettaClass].groupBy[callable]
	}
	
	def commonInputClass(RosettaArguments arguments) {
		return arguments.classUsages.keySet.filter(RosettaClass).head
	}
	
	def collectRootCalls(RosettaAlias alias) {
		return doCollectRootCalls(alias)
	}

	def collectRootCalls(RosettaQualifiable rq) {
		return doCollectRootCalls(rq)
	}

	def private LinkedHashSet<RosettaClass> doCollectRootCalls(EObject obj) {
		val classes = newLinkedHashSet
		obj.eAllContents.filter(RosettaCallableCall).forEach [
			collectRootCalls(it, [if(it instanceof RosettaClass && !it.eIsProxy) classes.add(it as RosettaClass)])
		]
		return classes
	}
	
	/**
	 * Collect all callable objects at the root nodes
	 */
	def void collectRootCalls(RosettaExpression expr, (RosettaCallable)=>void visitor) {
		if(expr instanceof RosettaAlias) {
			expr.expression.collectRootCalls(visitor)
		}
		else if(expr instanceof RosettaBinaryOperation) {
			expr.left.collectRootCalls(visitor)
			expr.right.collectRootCalls(visitor)
		}
		else if(expr instanceof RosettaCallableCall) {
			val callable = expr.callable
			if(callable instanceof RosettaAlias) {
				callable.expression.collectRootCalls(visitor)
			} 
			else if(callable instanceof RosettaClass) {
				visitor.apply(callable)
			}
			else {
				throw new IllegalArgumentException("Failed to collect root calls: " + callable)
			}
		}
		else if(expr instanceof RosettaGroupByFeatureCall) {
			expr.call.collectRootCalls(visitor)
		}
		else if(expr instanceof RosettaFeatureCall) {
			// go up to the receiver
			expr.receiver.collectRootCalls(visitor)
		}
		else if(expr instanceof RosettaClass) {
			visitor.apply(expr)
		}
		else {
			throw new IllegalArgumentException("Failed to collect root calls: " + expr)
		}
	}
	
	/**
	 * Collect all object types at the leaf nodes of the expression tree
	 */
	def void collectLeafTypes(RosettaExpression expr, (RosettaType) => void visitor) {
		if(expr instanceof RosettaAlias) {
			expr.expression.collectLeafTypes(visitor)
		}
		else if(expr instanceof RosettaBinaryOperation) {
			expr.left.collectLeafTypes(visitor)
			expr.right.collectLeafTypes(visitor)
		}
		else if(expr instanceof RosettaCallableCall) {
			val callable = expr.callable
			if(callable instanceof RosettaAlias) {
				callable.expression.collectLeafTypes(visitor)
			} 
			else if(callable instanceof RosettaClass) {
				visitor.apply(callable)
			}
			else {
				throw new IllegalArgumentException("Failed to collect leaf type: " + callable)
			}
		}
		else if(expr instanceof RosettaGroupByFeatureCall) {
			expr.call.collectLeafTypes(visitor)
		}
		else if(expr instanceof RosettaFeatureCall) {
			// go down to get the feature type
			visitor.apply(expr.feature.type)
		}
		else if(expr instanceof RosettaExistsExpression) {
			expr.argument.collectLeafTypes(visitor)
		}
		else if(expr instanceof RosettaAbsentExpression) {
			expr.argument.collectLeafTypes(visitor)
		}
		else if(expr instanceof RosettaWhenPresentExpression) {
			// only check right
			expr.right.collectLeafTypes(visitor)
		}
		else if(expr instanceof RosettaEnumValueReference) {
			visitor.apply(expr.enumeration)
		}
		else if(expr instanceof RosettaTyped) {
			visitor.apply(expr.type)
		}
		else {
			throw new IllegalArgumentException("Failed to collect leaf type: " + expr)
		}
	}
	
	/**
	 * Collect all expressions
	 */
	def void collectExpressions(RosettaExpression expr, (RosettaExpression) => void visitor) {
		if(expr instanceof RosettaAlias) {
			expr.expression.collectExpressions(visitor)
		}
		else if(expr instanceof RosettaGroupByFeatureCall) {
			expr.call.collectExpressions(visitor)
		}
		else if(expr instanceof RosettaBinaryOperation) {
			if(expr.operator.equals("or") || expr.operator.equals("and")) {
				expr.left.collectExpressions(visitor)
				expr.right.collectExpressions(visitor)
			}
			else {
				visitor.apply(expr)
			}	
		}
		else {
			visitor.apply(expr)
		}
	}
	
	def boolean isProjectLocal(URI platformResourceURI, URI candidateUri) {
		if (!platformResourceURI.isPlatformResource) {
			// synthetic tests URI
			return true
		}
		val projectName = platformResourceURI.segment(1)
		if (candidateUri.isPlatformResource) {
			return projectName == candidateUri.segment(1)
		}
		return false
	}
}
