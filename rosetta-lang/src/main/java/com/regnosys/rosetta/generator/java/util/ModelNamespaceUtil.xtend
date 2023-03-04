package com.regnosys.rosetta.generator.java.util

import com.google.common.collect.LinkedHashMultimap
import com.regnosys.rosetta.rosetta.RosettaModel
import java.util.List

class ModelNamespaceUtil {

	def namespaceToDescriptionMap(List<RosettaModel> elements) {

		val namespaceToDescription = LinkedHashMultimap.<String, String>create

		elements.filter[definition !== null].forEach [ RosettaModel model |
			namespaceToDescription.put(model.name, model.definition)
		]

		namespaceToDescription
	}

	def namespaceToModelUriMap(List<RosettaModel> elements) {

		val namespaceToModelUri = LinkedHashMultimap.<String, String>create

		elements.forEach [ RosettaModel model |
			val resourceURI = model.eResource.URI.toString
			val resourceShortName = resourceURI.substring(resourceURI.lastIndexOf('/') + 1)
			namespaceToModelUri.put(model.name, resourceShortName)				
		]

		namespaceToModelUri
	}

}
