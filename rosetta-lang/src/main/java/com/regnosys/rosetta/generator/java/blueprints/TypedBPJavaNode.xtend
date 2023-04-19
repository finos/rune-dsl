package com.regnosys.rosetta.generator.java.blueprints

import com.regnosys.rosetta.generator.java.types.JavaType
import com.regnosys.rosetta.validation.TypedBPNode
import java.util.List

class TypedBPJavaNode {
	public TypedBPNode original
	
	public JavaType input
	public JavaType inputKey
	public JavaType output
	public JavaType outputKey

	public TypedBPJavaNode next
	
	public List<TypedBPJavaNode> orNodes = newArrayList

	override toString() '''
	(«input», «inputKey»)->(«output», «outputKey»)'''
}
