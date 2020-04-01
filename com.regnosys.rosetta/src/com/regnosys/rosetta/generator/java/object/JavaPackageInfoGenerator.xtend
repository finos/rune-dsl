package com.regnosys.rosetta.generator.java.object

import java.util.Collection
import java.util.Map
import org.eclipse.xtext.generator.IFileSystemAccess2

class JavaPackageInfoGenerator {

	def generatePackageInfoClasses(IFileSystemAccess2 fsa, Map<String, Collection<String>> modelDescriptionMap) {

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
