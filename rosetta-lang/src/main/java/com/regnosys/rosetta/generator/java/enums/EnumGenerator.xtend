package com.regnosys.rosetta.generator.java.enums

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
import java.util.Map
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.rosetta.model.lib.annotations.RosettaSynonym
import java.util.concurrent.ConcurrentHashMap
import java.util.Collections

class EnumGenerator {
	@Inject extension ImportManagerExtension

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

	private def String toJava(RosettaEnumeration e, RootPackage root, String version) {
		val scope = new JavaScope(root)
		
		val StringConcatenationClient classBody = '''
		«javadoc(e, version)»
		public enum «e.name» {
		    
			«FOR value: allEnumsValues(e) SEPARATOR ',\n' AFTER ';'»
				«javadoc(value)»
				«value.contributeAnnotations»
				«convertValuesWithDisplay(value)»
			«ENDFOR»
			
			private static «Map»<«String», «e.name»> values;
			static {
		        «Map»<«String», «e.name»> map = new «ConcurrentHashMap»<>();
				for («e.name» instance : «e.name».values()) {
					map.put(instance.toDisplayString(), instance);
				}
				values = «Collections».unmodifiableMap(map);
		    }
		
			private final «String» rosettaName;
			private final «String» displayName;
			
			«e.name»(«String» rosettaName) {
				this(rosettaName, null);
			}

			«e.name»(«String» rosettaName, «String» displayName) {
				this.rosettaName = rosettaName;
				this.displayName = displayName;
			}
			
			public static «e.name» fromDisplayName(String name) {
				«e.name» value = values.get(name);
				if (value == null) {
					throw new «IllegalArgumentException»("No enum constant with display name \"" + name + "\".");
				}
				return value;
			}
			
			public «String» toDisplayString() {
				return displayName != null ?  displayName : rosettaName;
			}
		}
		'''
		
		buildClass(root, classBody, scope)
	}
	
	
	private def StringConcatenationClient contributeAnnotations(RosettaEnumValue e) '''
	«FOR synonym : e.enumSynonyms»
		«FOR source : synonym.sources»
			@«RosettaSynonym»(value = "«synonym.synonymValue»", source = "«source.getName»")
		«ENDFOR»
	«ENDFOR»
	'''	
}