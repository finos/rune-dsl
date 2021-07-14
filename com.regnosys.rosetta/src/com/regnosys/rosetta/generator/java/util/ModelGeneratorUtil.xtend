package com.regnosys.rosetta.generator.java.util

import com.google.common.html.HtmlEscapers
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaDefinable
import com.regnosys.rosetta.rosetta.simple.References
import com.regnosys.rosetta.rosetta.RosettaRegulatoryReference
import java.util.List

class ModelGeneratorUtil {
	
	static def javadoc(RosettaNamed named) '''
		«javadoc(named, null)»
	'''
	
	static def javadoc(RosettaNamed named, String version) {
		val definition = if (named instanceof RosettaDefinable) named.definition else ""
		val docRef = if (named instanceof References) named.references else emptyList
		javadoc(definition, docRef, version)
	}

	static def javadoc(String definition, List<RosettaRegulatoryReference> docRef, String version) '''
		/**
		«javadocDefinition(definition)»
		«javadocVersion(version)»
		«javadocDocRef(docRef)»
		 */
	'''
		
	static def emptyJavadocWithVersion(String version) '''
		/**
		 * @version «version»
		 */
	'''
	
	static def escape(String definition) 
	'''«IF definition !==null && !definition.isEmpty»«HtmlEscapers.htmlEscaper().escape(definition)»«ENDIF»'''
		
	private static def javadocDefinition(String definition) '''
		«IF definition !==null && !definition.isEmpty» * «HtmlEscapers.htmlEscaper().escape(definition)»«ENDIF»
	'''
	
	private static def javadocVersion(String version) '''
		«IF version !==null && !version.isEmpty» * @version «version»«ENDIF»
	'''
	
	private static def javadocDocRef(List<RosettaRegulatoryReference> references) '''
		«IF references !==null && !references.isEmpty»
			«FOR reference : references»
			 *
			 * Body «reference.regRegime.name»
			«FOR mandate : reference.mandates» * Corpus «mandate.corpusType» «mandate.name» «IF mandate.displayName !== null»«HtmlEscapers.htmlEscaper().escape(mandate.displayName)»«ENDIF» «IF mandate.definition !== null»"«HtmlEscapers.htmlEscaper().escape(mandate.definition)»"«ENDIF» «ENDFOR»
			«FOR segment : reference.segments» * «segment.segment.name» "«HtmlEscapers.htmlEscaper().escape(segment.segmentRef)»"«ENDFOR»
			 *
			 * Provision «reference.provision»
			 *
			«ENDFOR»
		«ENDIF»
	'''
}