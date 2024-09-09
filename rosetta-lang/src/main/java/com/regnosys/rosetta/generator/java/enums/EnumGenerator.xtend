package com.regnosys.rosetta.generator.java.enums

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.rosetta.model.lib.annotations.RosettaEnum
import com.rosetta.model.lib.annotations.RosettaSynonym
import java.util.Collections
import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.enums.EnumHelper.*
import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator

class EnumGenerator {
	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeTranslator

	def generate(RootPackage root, IFileSystemAccess2 fsa, REnumType enumeration, String version) {
		fsa.generateFile(root.withForwardSlashes + '/' + enumeration.name + '.java', enumeration.toJava(root, version))
	}

	private def String toJava(REnumType e, RootPackage root, String version) {
		val scope = new JavaScope(root)
		
		val clazz = e.toJavaReferenceType
		
		val StringConcatenationClient classBody = '''
		«javadoc(e.EObject, version)»
		@«RosettaEnum»("«e.name»")
		public enum «clazz» {
		
			«FOR value: e.allEnumValues SEPARATOR ',\n' AFTER ';'»
				«javadoc(value)»
				«value.contributeAnnotations»
				@«com.rosetta.model.lib.annotations.RosettaEnumValue»(value = "«value.name»"«IF value.display !== null», displayName = "«value.display»"«ENDIF») «convertValuesWithDisplay(value)»
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
			
			«FOR p : e.allParents»
			«val parentClass = p.toJavaReferenceType»
			«val fromScope = scope.methodScope("from" + parentClass.simpleName)»
			«val fromParam = fromScope.createUniqueIdentifier(parentClass.simpleName.toFirstLower)»
			public static «clazz» from«parentClass»(«parentClass» «fromParam») {
				
			}
			«ENDFOR»
		
			@Override
			public «String» toString() {
				return toDisplayString();
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