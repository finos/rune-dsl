package com.regnosys.rosetta.generator.java.object.validators

import com.google.common.collect.ImmutableMap
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ExistenceChecker
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.validation.ValidationResult.ValidationType
import java.util.Map
import java.util.Set
import java.util.stream.Collectors

import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RDataType
import jakarta.inject.Inject
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression

import com.regnosys.rosetta.generator.java.object.validators.AbstractValidatorGenerator
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope

class OnlyExistsValidatorGenerator extends AbstractValidatorGenerator {

	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeTranslator
	
	override protected createValidatorClass(JavaPojoInterface pojo) {
		pojo.toOnlyExistsValidatorClass
	}
	
	override protected generate(RDataType type, RGeneratedJavaClass<?> validator, JavaPojoInterface javaType, String version, JavaClassScope scope) {
		'''
		public «validator.asClassDeclaration» {

			/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
			@Override
			public <T2 extends «javaType»> «ValidationResult»<«javaType»> validate(«RosettaPath» path, T2 o, «Set»<String> fields) {
				«Map»<String, Boolean> fieldExistenceMap = «ImmutableMap».<String, Boolean>builder()
						«FOR attr : type.allAttributes»
							«val prop = javaType.findProperty(attr.name)»
							«val propCode = prop.applyGetter(JavaExpression.from('''o''', javaType))»
							.put("«prop.name»", «ExistenceChecker».isSet((«prop.type») «propCode»))
						«ENDFOR»
						.build();
				
				// Find the fields that are set
				«Set»<String> setFields = fieldExistenceMap.entrySet().stream()
						.filter(Map.Entry::getValue)
						.map(Map.Entry::getKey)
						.collect(«Collectors».toSet());
				
				if (setFields.equals(fields)) {
					return «method(ValidationResult, "success")»("«javaType.rosettaName»", «ValidationType».ONLY_EXISTS, "«javaType.rosettaName»", path, "");
				}
				return «method(ValidationResult, "failure")»("«javaType.rosettaName»", «ValidationType».ONLY_EXISTS, "«javaType.rosettaName»", path, "",
						String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
			}
		}
		'''
	}
}
