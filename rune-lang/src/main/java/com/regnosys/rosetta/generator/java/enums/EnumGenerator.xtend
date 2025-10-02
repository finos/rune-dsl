package com.regnosys.rosetta.generator.java.enums

import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.rosetta.model.lib.annotations.RosettaEnum
import com.rosetta.model.lib.annotations.RosettaSynonym
import java.util.Collections
import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import jakarta.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient

import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import org.apache.commons.text.StringEscapeUtils
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.generator.java.types.RJavaEnum
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.generator.java.RObjectJavaClassGenerator

class EnumGenerator extends RObjectJavaClassGenerator<REnumType, RJavaEnum> {
	@Inject extension JavaTypeTranslator
	@Inject extension ModelGeneratorUtil
	@Inject extension RObjectFactory
	
	override protected streamObjects(RosettaModel model) {
		model.elements.stream.filter[it instanceof RosettaEnumeration].map[it as RosettaEnumeration].map[buildREnumType]
	}
	override protected createTypeRepresentation(REnumType object) {
		object.toJavaReferenceType
	}
	override protected generate(REnumType e, RJavaEnum javaEnum, String version, JavaClassScope scope) {
		'''
		«javadoc(e.EObject.definition, e.EObject.references, version)»
		@«RosettaEnum»("«e.name»")
		public enum «e.name» {
		
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
		
			@Override
			public «String» toString() {
				return toDisplayString();
			}
		
			public «String» toDisplayString() {
				return displayName != null ?  displayName : rosettaName;
			}
		}
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