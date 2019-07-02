package com.regnosys.rosetta.generator.daml.enums

import com.google.inject.Inject
import com.regnosys.rosetta.generator.RosettaOutputConfigurationProvider
import com.regnosys.rosetta.generator.daml.object.DamlModelObjectBoilerPlate
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import java.util.ArrayList
import java.util.List
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.daml.util.DamlModelGeneratorUtil.*

class DamlEnumGenerator {
	
	@Inject extension DamlModelObjectBoilerPlate
	
	static final String FILENAME = 'Org/Isda/Cdm/Enums.daml'
		
	def generate(IFileSystemAccess2 fsa, Iterable<RosettaEnumeration> rosettaEnums, String version) {
		if (!fsa.isFile(FILENAME, RosettaOutputConfigurationProvider.SRC_GEN_DAML_OUTPUT)) {
			val enums = rosettaEnums.sortBy[name].generateEnums(version).replaceTabsWithSpaces
			fsa.generateFile(FILENAME, RosettaOutputConfigurationProvider.SRC_GEN_DAML_OUTPUT, enums)
		}
	}

	def static toJavaEnumName(RosettaEnumeration enumeration, RosettaEnumValue rosettaEnumValue) {
		return enumeration.name + '.' + convertValues(rosettaEnumValue)
	}

	private def allEnumsValues(RosettaEnumeration enumeration) {
		val enumValues = new ArrayList
		var e = enumeration;

		while (e !== null) {
			e.enumValues.forEach[enumValues.add(it)]
			e = e.superType
		}
		return enumValues.sortBy[name];
	}

	private def generateEnums(List<RosettaEnumeration> enums, String version)  '''
		daml 1.2
		
		«fileComment(version)»
		module Org.Isda.Cdm.Enums
		  ( module Org.Isda.Cdm.Enums ) where
		
		«FOR e : enums»
			«val allEnumValues = allEnumsValues(e)»
			«var i = 0»
			«classComment(e.definition)»
			data «e.name» 
			  «FOR value: allEnumValues»
			      «IF i++<1»=«ELSE»|«ENDIF» «e.name»_«convertValues(value)»«IF allEnumValues.size==1»()«ENDIF»
			      «methodComment(value.definition)»
			  «ENDFOR»
			    deriving (Eq, Ord, Show)
			
		«ENDFOR»
	'''
	
	def boolean anyValueHasSynonym(RosettaEnumeration enumeration) {
		enumeration.allEnumsValues.map[enumSynonyms].flatten.size > 0
	}
	
    def static convertValues(RosettaEnumValue enumValue) {
		return formatEnumName(enumValue.name)
	}
	
	def static String formatEnumName(String name) {
		return name
	}
}