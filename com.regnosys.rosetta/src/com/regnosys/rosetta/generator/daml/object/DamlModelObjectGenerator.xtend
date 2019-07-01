package com.regnosys.rosetta.generator.daml.object

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.RosettaOutputConfigurationProvider
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaMetaType
import java.util.List
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.daml.util.DamlModelGeneratorUtil.*

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*

class DamlModelObjectGenerator {

	@Inject extension RosettaExtensions
	@Inject extension DamlModelObjectBoilerPlate
	@Inject extension DamlMetaFieldGenerator
	
	static final String CLASSES_FILENAME = 'Org/Isda/Cdm/Classes.daml'
	static final String META_FIELDS_FILENAME = 'Org/Isda/Cdm/MetaFields.daml'
	
	def generate(IFileSystemAccess2 fsa, Iterable<RosettaClass> rosettaClasses, Iterable<RosettaMetaType> metaTypes, String version) {
		if (!fsa.isFile(CLASSES_FILENAME, RosettaOutputConfigurationProvider.SRC_GEN_DAML_OUTPUT)) {
			val classes = rosettaClasses.sortBy[name].generateClasses(version).replaceTabsWithSpaces
			fsa.generateFile(CLASSES_FILENAME, RosettaOutputConfigurationProvider.SRC_GEN_DAML_OUTPUT, classes)
			val metaFields = generateMetaFields(metaTypes, version).replaceTabsWithSpaces
			fsa.generateFile(META_FIELDS_FILENAME, RosettaOutputConfigurationProvider.SRC_GEN_DAML_OUTPUT, metaFields)
		}
	}

	/**
	 * DAML requires:
	 * - tabs not spaces
	 * - keyword "deriving" should in indented as additional 2 spaces
	 * - class name and folders in title case
	 * - must contain "import Prelude hiding (...)" line to avoid name clashes with DAML keywords
	 * - nullable fields must be declared with the keyword "Optional"
	 */
	private def generateClasses(List<RosettaClass> rosettaClasses, String version) '''
		daml 1.2
		
		«fileComment(version)»
		module Org.Isda.Cdm.Classes
		  ( module Org.Isda.Cdm.Classes ) where
		
		import Org.Isda.Cdm.Enums
		import Org.Isda.Cdm.ZonedDateTime
		import Org.Isda.Cdm.MetaClasses
		import Org.Isda.Cdm.MetaFields
		import Prelude hiding (Party, exercise, id, product, agreement)
		
		«FOR c : rosettaClasses»
			«classComment(c.definition)»
			data «c.name» = «c.name» with 
			  «FOR attribute : c.allSuperTypes.expandedAttributes»
			      «attribute.toAttributeName» : «attribute.toType»
			        «methodComment(attribute.definition)»
			  «ENDFOR»
			    deriving (Eq, Ord, Show)
			
		«ENDFOR»
	'''
}
