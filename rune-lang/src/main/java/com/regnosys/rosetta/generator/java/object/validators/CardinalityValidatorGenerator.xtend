package com.regnosys.rosetta.generator.java.object.validators

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.rosetta.model.lib.expression.ComparisonResult
import com.rosetta.model.lib.expression.ExpressionOperators
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ValidationResult
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient

import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RDataType
import jakarta.inject.Inject
import java.util.List
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.types.RCardinality
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression

import com.regnosys.rosetta.generator.java.scoping.JavaClassScope
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass

class CardinalityValidatorGenerator extends AbstractValidatorGenerator {

	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeTranslator

	override protected createValidatorClass(JavaPojoInterface pojo) {
		pojo.toValidatorClass
	}
	override protected generateClass(RDataType type, RGeneratedJavaClass<?> validatorClass, JavaPojoInterface pojo, String version, JavaClassScope scope) {
		'''
		public «validatorClass.asClassDeclaration» {
		
			private «List»<«ComparisonResult»> getComparisonResults(«pojo» o) {
				return «Lists».<«ComparisonResult»>newArrayList(
						«FOR attrCheck : type.allAttributes.map[checkCardinality(pojo, it, scope)].filter[it !== null] SEPARATOR ", "»
							«attrCheck»
						«ENDFOR»
					);
			}

			@Override
			public «List»<«ValidationResult»<?>> getValidationResults(«RosettaPath» path, «pojo» o) {
				return getComparisonResults(o)
					.stream()
					.map(res -> {
						if (!«method(Strings, "isNullOrEmpty")»(res.getError())) {
							return «method(ValidationResult, "failure")»("«pojo.rosettaName»", «ValidationResult.ValidationType».CARDINALITY, "«pojo.rosettaName»", path, "", res.getError());
						}
						return «method(ValidationResult, "success")»("«pojo.rosettaName»", «ValidationResult.ValidationType».CARDINALITY, "«pojo.rosettaName»", path, "");
					})
					.collect(«method(Collectors, "toList")»());
			}
		
		}
	'''
	}

	private def StringConcatenationClient checkCardinality(JavaPojoInterface javaType, RAttribute attr, JavaClassScope scope) {
		if (attr.cardinality == RCardinality.UNBOUNDED) {
			null
		} else {
			val prop = javaType.findProperty(attr.name)
			val propCode = prop.applyGetter(JavaExpression.from('''o''', javaType));
	        /* Casting is required to ensure types are output to ensure recompilation in Rosetta */
			'''
			«IF attr.isMulti»
				«method(ExpressionOperators, "checkCardinality")»("«attr.name»", («prop.type») «propCode» == null ? 0 : «propCode».size(), «attr.cardinality.min», «attr.cardinality.max.orElse(0)»)
			«ELSE»
				«method(ExpressionOperators, "checkCardinality")»("«attr.name»", («prop.type») «propCode» != null ? 1 : 0, «attr.cardinality.min», «attr.cardinality.max.orElse(0)»)
			«ENDIF»
			'''
		}
	}
}
