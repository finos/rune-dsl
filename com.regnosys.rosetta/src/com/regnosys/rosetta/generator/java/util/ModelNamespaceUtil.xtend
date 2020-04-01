package com.regnosys.rosetta.generator.java.util

import com.google.common.collect.LinkedHashMultimap
import com.regnosys.rosetta.rosetta.RosettaModel
import java.util.List

class ModelNamespaceUtil {
	
	def generateNamespaceDescriptionMap(List<RosettaModel> elements) {
		
		val namespaceToDescription = LinkedHashMultimap.<String, String>create
		
		elements.filter[definition !== null].forEach[RosettaModel model |
				namespaceToDescription.put(model.name, model.definition)
		]
		
		namespaceToDescription
	}
	
}