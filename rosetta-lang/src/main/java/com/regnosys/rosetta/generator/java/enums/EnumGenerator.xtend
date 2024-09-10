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

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.types.RJavaEnum
import java.util.List
import java.util.Set
import org.apache.commons.text.StringEscapeUtils

class EnumGenerator {
	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeTranslator

	def generate(RootPackage root, IFileSystemAccess2 fsa, REnumType enumeration, String version) {
		fsa.generateFile(root.withForwardSlashes + '/' + enumeration.name + '.java', enumeration.toJava(root, version))
	}

	private def String toJava(REnumType e, RootPackage root, String version) {
		val scope = new JavaScope(root)
		
		val javaEnum = e.toJavaReferenceType as RJavaEnum
		
		val StringConcatenationClient classBody = '''
		«javadoc(e.EObject, version)»
		@«RosettaEnum»("«e.name»")
		public enum «javaEnum» {
		
			«FOR value: javaEnum.enumValues SEPARATOR ',\n' AFTER ';'»
				«javadoc(value.EObject)»
				«value.EObject.contributeAnnotations»
				@«com.rosetta.model.lib.annotations.RosettaEnumValue»(value = "«value.rosettaName»"«IF value.displayName !== null», displayName = "«value.displayName»"«ENDIF») 
				«value.name»("«value.rosettaName»", «IF value.displayName !== null»"«StringEscapeUtils.escapeJava(value.displayName)»"«ELSE»null«ENDIF»)
			«ENDFOR»
		
			private static «Map»<«String», «javaEnum»> values;
			static {
		        «Map»<«String», «javaEnum»> map = new «ConcurrentHashMap»<>();
				for («javaEnum» instance : «javaEnum».values()) {
					map.put(instance.toDisplayString(), instance);
				}
				values = «Collections».unmodifiableMap(map);
		    }
		
			private final «String» rosettaName;
			private final «String» displayName;

			«javaEnum»(«String» rosettaName, «String» displayName) {
				this.rosettaName = rosettaName;
				this.displayName = displayName;
			}
		
			public static «javaEnum» fromDisplayName(String name) {
				«javaEnum» value = values.get(name);
				if (value == null) {
					throw new «IllegalArgumentException»("No enum constant with display name \"" + name + "\".");
				}
				return value;
			}
			«val visitedAncestors = javaEnum.parents.toSet»
			«FOR p : javaEnum.parents»
				
				«val fromScope = scope.methodScope("from" + p.simpleName)»
				«val fromParam = fromScope.createUniqueIdentifier(p.simpleName.toFirstLower)»
				public static «javaEnum» from«p.simpleName»(«p» «fromParam») {
					switch («fromParam») {
						«FOR v : p.enumValues»
						case «v.name»: return «v.name»;
						«ENDFOR»
					}
					return null;
				}
				
				«val toScope = scope.methodScope("to" + p.simpleName)»
				«val toParam = toScope.createUniqueIdentifier(javaEnum.simpleName.toFirstLower)»
				public static «p» to«p.simpleName»(«javaEnum» «toParam») {
					switch («toParam») {
						«FOR v : p.enumValues»
						case «v.name»: return «p».«v.name»;
						«ENDFOR»
					}
					return null;
				}
				«ancestorConversions(javaEnum, p, p.parents, visitedAncestors, scope)»
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
	
	private def StringConcatenationClient ancestorConversions(RJavaEnum javaEnum, RJavaEnum currentParent, List<RJavaEnum> ancestors, Set<RJavaEnum> visitedAncestors, JavaScope scope) {
		'''
		«FOR a : ancestors»
			«IF visitedAncestors.add(a)»
			
			«val fromScope = scope.methodScope("from" + a.simpleName)»
			«val fromParam = fromScope.createUniqueIdentifier(a.simpleName.toFirstLower)»
			public static «javaEnum» from«a.simpleName»(«a» «fromParam») {
				return from«currentParent.simpleName»(«currentParent».from«a.simpleName»(«fromParam»));
			}
			
			«val toScope = scope.methodScope("to" + a.simpleName)»
			«val toParam = toScope.createUniqueIdentifier(javaEnum.simpleName.toFirstLower)»
			public static «a» to«a.simpleName»(«javaEnum» «toParam») {
				return «currentParent».to«a.simpleName»(to«currentParent.simpleName»(«toParam»));
			}
			«ancestorConversions(javaEnum, currentParent, a.parents, visitedAncestors, scope)»
			«ENDIF»
		«ENDFOR»
		'''
	}
	
	
	private def StringConcatenationClient contributeAnnotations(RosettaEnumValue e) '''
	«FOR synonym : e.enumSynonyms»
		«FOR source : synonym.sources»
			@«RosettaSynonym»(value = "«synonym.synonymValue»", source = "«source.getName»")
		«ENDFOR»
	«ENDFOR»
	'''	
}