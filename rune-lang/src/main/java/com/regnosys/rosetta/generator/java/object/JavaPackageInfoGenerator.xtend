package com.regnosys.rosetta.generator.java.object

import java.util.Collection
import org.eclipse.xtext.generator.IFileSystemAccess2
import java.util.List
import com.regnosys.rosetta.rosetta.RosettaModel
import com.google.common.collect.LinkedHashMultimap

class JavaPackageInfoGenerator {

	def namespaceToDescriptionMap(List<RosettaModel> elements) {
		val namespaceToDescription = LinkedHashMultimap.<String, String>create

		elements.filter[definition !== null].forEach [ RosettaModel model |
			namespaceToDescription.put(model.name, model.definition)
		]

		namespaceToDescription
	}

	def generatePackageInfoClasses(IFileSystemAccess2 fsa, List<RosettaModel> elements) {
		val modelDescriptionMap = namespaceToDescriptionMap(elements).asMap
		modelDescriptionMap.forEach[packageName, descriptions | 
			if (descriptions !== null) {
				fsa.generateFile('''«packageName.replace('.', '/')»/package-info.java''', generatePackageInfo(packageName, descriptions))		
			}
		]
	}

	private def generatePackageInfo(String packageName, Collection<String> descriptions) {
		return '''
			/**
			*	«FOR desc : descriptions»
			*	«desc»
			*	<p>
			*	«ENDFOR»
			*
			*/
			
			package «packageName»;
		'''
	}

}
