package com.regnosys.rosetta.generator.util

import java.util.function.Function
import java.util.Iterator
import java.util.NoSuchElementException

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
}
