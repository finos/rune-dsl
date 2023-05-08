package com.regnosys.rosetta.generator.java.enums

import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaRootElement
import java.util.ArrayList
import java.util.List
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.enums.EnumHelper.*
import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import javax.inject.Inject

class EnumGenerator {
	@Inject
	RosettaJavaPackages packages

	def generate(RootPackage root, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version) {
		elements.filter(RosettaEnumeration).forEach [
			fsa.generateFile(root.withForwardSlashes + '/' + name + '.java', toJava(root, version))
		]
	}
	
	private def allEnumsValues(RosettaEnumeration enumeration) {
		val enumValues = new ArrayList
		var e = enumeration;

		while (e !== null) {
			e.enumValues.forEach[enumValues.add(it)]
			e = e.superType
		}
		return enumValues;
	}

	private def toJava(RosettaEnumeration e, RootPackage root, String version) '''
		package «root»;
		
		«IF e.anyValueHasSynonym»
		import «packages.defaultLibAnnotations».RosettaSynonym;
		«ENDIF»
		
		«javadoc(e, version)»
		public enum «e.name» {
		    
			«FOR value: allEnumsValues(e) SEPARATOR ',\n'»

				«javadoc(value)»
				«value.contributeAnnotations»
				«IF value.display !== null»
				    «convertValuesWithDisplay(value)»
				«ELSE»
				    «convertValues(value)»
		        «ENDIF»		
			«ENDFOR»
			;
		
			private final String displayName;
			
			«e.name»() {
				this.displayName = null;
			}

			«IF e.anyValueHasDisplayName»
			«e.name»(String displayName) {
				this.displayName = displayName;
			}
			«ENDIF»

			@Override
			public String toString() {
				return displayName != null ?  displayName : name();
			}
			
		}
		'''

	def boolean anyValueHasSynonym(RosettaEnumeration enumeration) {
		enumeration.allEnumsValues.map[enumSynonyms].flatten.size > 0
	}
	
	
    def boolean anyValueHasDisplayName(RosettaEnumeration enumeration) {
        enumeration.allEnumsValues.exists[display !== null]
    }
	
	
	private def contributeAnnotations(RosettaEnumValue e) '''
	«FOR synonym : e.enumSynonyms»
		«FOR source : synonym.sources»
			@RosettaSynonym(value = "«synonym.synonymValue»", source = "«source.getName»")
		«ENDFOR»
	«ENDFOR»
	'''
	
	/**
	 * Use EnumHelper.formatEnumName(String) instead
	 */
	@Deprecated
	def static String formatEnumName(String name) {
		EnumHelper.formatEnumName(name);
	}

	
}