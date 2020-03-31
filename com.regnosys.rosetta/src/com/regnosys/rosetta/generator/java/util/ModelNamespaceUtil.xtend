package com.regnosys.rosetta.generator.java.util

import com.google.common.collect.LinkedListMultimap
import com.regnosys.rosetta.rosetta.RosettaModel
import java.util.List

class ModelNamespaceUtil {
	
	def generateNamespaceDescriptionMap(List<RosettaModel> elements, String version) {
		
		val namespaceToDescription = LinkedListMultimap.<String, String>create
		
		elements.forEach[RosettaModel model |
				namespaceToDescription.put(model.name, model.definition)
		]
		
		namespaceToDescription
	}
	
}