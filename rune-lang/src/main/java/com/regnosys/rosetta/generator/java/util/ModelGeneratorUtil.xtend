package com.regnosys.rosetta.generator.java.util

import com.google.common.html.HtmlEscapers
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaDefinable
import com.regnosys.rosetta.rosetta.simple.References
import java.util.List
import com.regnosys.rosetta.rosetta.RosettaDocReference
import com.regnosys.rosetta.rosetta.simple.AnnotationPathExpression
import com.regnosys.rosetta.utils.AnnotationPathExpressionUtil
import jakarta.inject.Inject

class ModelGeneratorUtil {
	
	@Inject
	AnnotationPathExpressionUtil annotationPathUtil
	
	def javadoc(RosettaNamed named) '''
		«javadoc(named, null)»
	'''
	
	private def javadoc(RosettaNamed named, String version) {
		val definition = if (named instanceof RosettaDefinable) named.definition else ""
		val docRef = if (named instanceof References) named.references else emptyList
		javadoc(definition, docRef, version)
	}

	def javadoc(String definition, List<RosettaDocReference> docRef, String version) {
		return if (definition === null && docRef.isEmpty && version === null)
			null
		else
			'''
				/**
				«javadocDefinition(definition)»
				«javadocVersion(version)»
				«javadocDocRef(docRef)»
				 */
			'''
	}
	
	def emptyJavadocWithVersion(String version) '''
		/**
		 * @version «version»
		 */
	'''
	
	def escape(String definition) 
	'''«IF definition !==null && !definition.isEmpty»«HtmlEscapers.htmlEscaper().escape(definition)»«ENDIF»'''
		
	private def javadocDefinition(String definition) '''
		«IF definition !==null && !definition.isEmpty» * «HtmlEscapers.htmlEscaper().escape(definition)»«ENDIF»
	'''
	
	private def javadocVersion(String version) '''
		«IF version !==null && !version.isEmpty» * @version «version»«ENDIF»
	'''
	
	private def javadocDocRef(List<RosettaDocReference> references) '''
		«IF references !==null && !references.isEmpty»
			«FOR reference : references»
			 *
			«IF reference.path !== null» * «reference.path.javadocPath»«ENDIF»
			 * Body «reference.docReference.body.name»
			«FOR mandate : reference.docReference.corpusList» * Corpus «mandate.getCorpusType» «mandate.name» «IF mandate.getDisplayName !== null»«HtmlEscapers.htmlEscaper().escape(mandate.getDisplayName)»«ENDIF» «IF mandate.definition !== null»"«HtmlEscapers.htmlEscaper().escape(mandate.definition)»"«ENDIF» «ENDFOR»
			«FOR segment : reference.docReference.segments» * «segment.segment.name» "«HtmlEscapers.htmlEscaper().escape(segment.segmentRef)»"«ENDFOR»
			 *
			 * Provision «reference.getProvision»
			 *
			«ENDFOR»
		«ENDIF»
	'''
	
	private def String javadocPath(AnnotationPathExpression expr) {
		return annotationPathUtil.fold(
			expr,
			[a|'''«a.name»'''],
			[a|'''item'''],
			[r,p|'''«r» -> «p.attribute.name»'''],
			[r,dp|'''«r» ->> «dp.attribute.name»''']
		)
	}
}