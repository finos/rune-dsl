package com.regnosys.rosetta.validation

import com.regnosys.rosetta.rosetta.BlueprintNode
import java.util.List

class TypedBPNode {
		public BlueprintNode node
		public BindableType input = new BindableType
		public BindableType inputKey = new BindableType
		public BindableType output = new BindableType
		public BindableType outputKey = new BindableType

		public List<TypedBPNode> andNodes = newArrayList
		public List<TypedBPNode> ifNodes = newArrayList

		public TypedBPNode next

		override toString() '''
		(«input.either», «inputKey.either»)->(«output.either», «outputKey.either»)'''

		def invert() {
			val result = new TypedBPNode
			result.input = output
			result.inputKey = outputKey
			result.output = input
			result.outputKey = inputKey
			result
		}
		
		static def combine(TypedBPNode in, TypedBPNode out) {
			val result = new TypedBPNode
			result.input = in.input
			result.inputKey = in.inputKey
			result.output = out.output
			result.outputKey = out.outputKey
			result
		}
	}