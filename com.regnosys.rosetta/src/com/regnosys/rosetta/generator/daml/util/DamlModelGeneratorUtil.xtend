package com.regnosys.rosetta.generator.daml.util

import com.google.common.html.HtmlEscapers
import org.apache.commons.lang3.text.WordUtils

class DamlModelGeneratorUtil {
	
	static def fileComment(String version) {
		comment('This file is auto-generated from the ISDA Common Domain Model, do not edit.', '-- |') + '''
		--   @version «version»'''
	}
		
	static def classComment(String definition) {
		comment(definition, '-- |')
	}
	
	static def methodComment(String definition) {
		comment(definition, '-- ^')
	}
	
	private static def comment(String definition, String prefix) '''
		«IF definition !==null && !definition.isEmpty »
			«prefix» «definition.removeNewLinesAndTabs.escape.wrap»
		«ENDIF»
	'''
	
	private static def removeNewLinesAndTabs(String definition) {
		definition.replace('\n', '').replace('\t', '')
	}
	
	private static def escape(String definition) {
		HtmlEscapers.htmlEscaper().escape(definition)
	}
	
	private static def wrap(String definition) {
		WordUtils.wrap(definition, 53, '\n--   ', false)
	}
	
	static def replaceTabsWithSpaces(CharSequence code) {
		code.toString.replace('\t', '    ')
	}
}
