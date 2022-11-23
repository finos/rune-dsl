package com.regnosys.rosetta.generator.util

import java.util.function.Function
import java.util.Iterator
import java.util.NoSuchElementException
import com.regnosys.rosetta.rosetta.RosettaType
import java.util.List
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.expression.RosettaCallableCall

class Util {
	static def <T> Iterable<T> distinct(Iterable<T> parentIterable) {
		return new DistinctByIterator(parentIterable, [it])
	}
	static def <T,U> Iterable<T> distinctBy(Iterable<T> parentIterable, Function<T,U> extractFunction) {
		return new DistinctByIterator(parentIterable, extractFunction)
	}
	
	def static <T> boolean exists(Iterable<? super T> iter, Class<T> clazz) {
		!iter.filter(clazz).empty
	}
	
	private static class DistinctByIterator<T, U> implements Iterable<T>{
		val Iterable<T> iterable;
		val Function<T,U> extractFunction;
		
		new(Iterable<T> iterable, Function<T,U> extractFunction) {
			this.iterable = iterable
			this.extractFunction = extractFunction
		}
		
		override iterator() {
			val parentIterator = iterable.iterator
			return new Iterator<T>() {
				val read = newHashSet
				var T readNext;
				override hasNext() {
					//by the ime this method is finished readNext will contain the next readable element or this returns false
					if (readNext!==null) return true;
					while (true){
						if (!parentIterator.hasNext) return false
						readNext = parentIterator.next
						val compareVal = extractFunction.apply(readNext)
						if (!read.contains(compareVal)) {
							read.add(compareVal);
							return true;
						}
					}
				}
				
				override next() {
					if (hasNext) {
						val result = readNext
						readNext = null
						return result
					}
					else {
						throw new NoSuchElementException("read past end of iterator")
					}
				}
				
			}
		}
	}
	
	def static String fullname(RosettaType clazz) '''«clazz.model.name».«clazz.name»'''
	def static String packageName(RosettaType clazz)  {clazz.model.name}
	
	def List<RosettaExpression> getEnclosingScopes(EObject e) {
		val containers = EcoreUtil2.getAllContainers(e)
		val result = newArrayList();
		if (e instanceof RosettaFeatureCall || e instanceof RosettaCallableCall) {
			result.add(e as RosettaExpression);
		}
		var prev = e;
		for (c: containers) {
			if (c instanceof RosettaFunctionalOperation) {
				if (c.functionRef == prev) {
					result.add(c);
				}
			}
			prev = c;
		}
		return result;
	}
	
	def int getScopeDepth(EObject e) {
		e.getEnclosingScopes.length
	}
	
	/**
	 * Prefix code generated variable name with double underscore to avoid name clashes with model names.
	 * Currently only used for list operation variable names, but should be used everywhere.
	 */
	def String toDecoratedName(String name, EObject container) {
		val prefix = '_'.repeat(container.scopeDepth)
		'''«prefix»«name»'''
	}
}
