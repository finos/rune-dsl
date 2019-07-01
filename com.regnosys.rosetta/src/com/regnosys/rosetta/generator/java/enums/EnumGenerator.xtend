package com.regnosys.rosetta.generator.java.enums

import com.google.common.base.CaseFormat
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaRootElement
import java.util.ArrayList
import java.util.Arrays
import java.util.List
import java.util.stream.Collectors
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

class EnumGenerator {
		
	def generate(RosettaJavaPackages packages, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version) {
		elements.filter(RosettaEnumeration).forEach [
			fsa.generateFile(packages.model.directoryName + '/' + name + '.java', toJava(packages, version))
		]
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
		return enumValues;
	}

	private def toJava(RosettaEnumeration e, RosettaJavaPackages packages, String version) '''
		package «packages.model.packageName»;
		
		«IF e.anyValueHasSynonym»
		import «packages.annotations.packageName».RosettaSynonym;
		«ENDIF»
		
		«javadocWithVersion(e.definition, version)»
		public enum «e.name» {
		    
			«FOR value: allEnumsValues(e) SEPARATOR ',\n'»

				«javadoc(value.definition)»
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
    
    def static convertValuesWithDisplay(RosettaEnumValue enumValue) {
        formatEnumName(enumValue.name) + '''("«enumValue.display»")''' 
        
    }
    
    def static convertValues(RosettaEnumValue enumValue) {
		return formatEnumName(enumValue.name)
	}
	
	def static String formatEnumName(String name) {
		if(noFormattingRequired(name))
			return name
			
		val parts = Arrays.asList(name.replaceSeparatorsWithUnderscores.splitAtNumbers).stream
									.map[splitAtUnderscore].flatMap[stream]
									.map[splitAtCamelCase].flatMap[stream]
									.map[camelCaseToUpperUnderscoreCase]
									.map[it.toUpperCase]
									.collect(Collectors.toList)
									
		return String.join("_", parts).prefixWithUnderscoreIfStartsWithNumber.removeDuplicateUnderscores
	}

	private def static boolean noFormattingRequired(String name) {
		return name.matches("^[A-Z0-9_]*$")
	}

	private def static String replaceSeparatorsWithUnderscores(String name) {
		return name.replace(".", "_").replace("-", "_").replace(" ", "_")
	}
	
	private def static List<String> splitAtCamelCase(String namePart) {
		return Arrays.asList(namePart.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"))
	}

	private def static List<String> splitAtUnderscore(String namePart) {
		return Arrays.asList(namePart.split("_"))
	}

	private def static String[] splitAtNumbers(String namePart) {
		return namePart.split("(?=[X])(?<=[^X])|(?=[^X])(?<=[X])".replace("X", "\\d"))
	}

	private def static String camelCaseToUpperUnderscoreCase(String namePart) {
		// if it starts with an upper case and ends with a lower case then assume it's camel case
		if(!namePart.empty && Character.isUpperCase(namePart.charAt(0)) && Character.isLowerCase(namePart.charAt(namePart.length()-1))) {
			return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, namePart)
		}
		return namePart
	}
	
	private def static String removeDuplicateUnderscores(String name) {
		return name.replace("__", "_")
	}
	
	private def static String prefixWithUnderscoreIfStartsWithNumber(String name) {
		if(Character.isDigit(name.charAt(0)))
			return "_" + name
		else
			return name
	}
}