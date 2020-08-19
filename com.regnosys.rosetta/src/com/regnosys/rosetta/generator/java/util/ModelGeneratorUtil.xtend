package com.regnosys.rosetta.generator.java.util

import com.google.common.html.HtmlEscapers

class ModelGeneratorUtil {
	
	static def javadoc(String definition) '''
		«IF definition !==null && !definition.isEmpty »
			/**
			 * «HtmlEscapers.htmlEscaper().escape(definition)»
			 */
		«ENDIF»
	'''

	static def javadocWithVersion(String definition, String version) '''
		«IF definition !==null && !definition.isEmpty »
			/**
			 * «HtmlEscapers.htmlEscaper().escape(definition)»
			 * 
			 * @version «version»
			 */
			«ELSE»
			«emptyJavadocWithVersion(version)»
		«ENDIF»
	'''

	static def emptyJavadocWithVersion(String version) '''
		/**
		 * @version «version»
		 */
	'''
	
	static def escape(String definition) 
	'''«IF definition !==null && !definition.isEmpty»«HtmlEscapers.htmlEscaper().escape(definition)»«ENDIF»'''
}