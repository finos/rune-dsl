package com.regnosys.rosetta.generator.java.object

import com.google.common.base.Strings
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.rosetta.model.lib.expression.ComparisonResult
import com.rosetta.model.lib.expression.ExpressionOperators
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ExistenceChecker
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.validation.ValidationResult.ValidationType
import com.rosetta.model.lib.validation.Validator
import com.rosetta.model.lib.validation.ValidatorWithArg
import java.util.Map
import java.util.Set
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.TypeSystem
import com.regnosys.rosetta.types.builtin.RStringType
import com.regnosys.rosetta.types.builtin.RNumberType
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import java.util.Optional
import java.util.regex.Pattern
import org.apache.commons.text.StringEscapeUtils
import java.math.BigDecimal
import javax.inject.Inject
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import java.util.List
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.types.RCardinality
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression

import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.types.RAliasType
import java.util.Collections
import java.util.ArrayList

class ValidatorsGenerator {

	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeTranslator
	@Inject extension TypeSystem
	@Inject extension RBuiltinTypeService
	@Inject extension JavaTypeUtil

	def generate(RootPackage root, IFileSystemAccess2 fsa, RDataType type, String version) {
		val javaType = type.toJavaReferenceType
		val attrs = type.allAttributes
		fsa.generateFile(javaType.toValidatorClass.canonicalName.withForwardSlashes + ".java",
			generateClass(root, javaType, attrs, version))
		fsa.generateFile(javaType.toTypeFormatValidatorClass.canonicalName.withForwardSlashes + ".java",
			generateTypeFormatValidator(root, javaType, attrs, version))
		fsa.generateFile(javaType.toOnlyExistsValidatorClass.canonicalName.withForwardSlashes + ".java",
			generateOnlyExistsValidator(root, javaType, attrs, version))
	}

	private def generateClass(RootPackage root, JavaPojoInterface javaType, Iterable<RAttribute> attributes, String version) {
		val scope = new JavaScope(root.typeValidation)
		buildClass(root.typeValidation, javaType.classBody(version, attributes), scope)
	}
	
	private def generateTypeFormatValidator(RootPackage root, JavaPojoInterface javaType, Iterable<RAttribute> attributes, String version) {
		val scope = new JavaScope(root.typeValidation)
		buildClass(root.typeValidation, javaType.typeFormatClassBody(version, attributes), scope)
	}

	private def generateOnlyExistsValidator(RootPackage root, JavaPojoInterface javaType, Iterable<RAttribute> attributes, String version) {
		val scope = new JavaScope(root.existsValidation)
		buildClass(root.existsValidation, javaType.onlyExistsClassBody(version, attributes), scope)
	}

	def private StringConcatenationClient classBody(JavaPojoInterface javaType, String version, Iterable<RAttribute> attributes) '''
		public class «javaType.toValidatorClass» implements «Validator»<«javaType»> {
		
			private «List»<«ComparisonResult»> getComparisonResults(«javaType» o) {
				return «Lists».<«ComparisonResult»>newArrayList(
						«FOR attrCheck : attributes.map[checkCardinality(javaType, it)].filter[it !== null] SEPARATOR ", "»
							«attrCheck»
						«ENDFOR»
					);
			}
		
			@Override
			public «ValidationResult»<«javaType»> validate(«RosettaPath» path, «javaType» o) {
				String error = getComparisonResults(o)
					.stream()
					.filter(res -> !res.get())
					.map(res -> res.getError())
					.collect(«method(Collectors, "joining")»("; "));
		
				if (!«method(Strings, "isNullOrEmpty")»(error)) {
					return «method(ValidationResult, "failure")»("«javaType.rosettaName»", «ValidationResult.ValidationType».CARDINALITY, "«javaType.rosettaName»", path, "", error);
				}
				return «method(ValidationResult, "success")»("«javaType.rosettaName»", «ValidationResult.ValidationType».CARDINALITY, "«javaType.rosettaName»", path, "");
			}

			@Override
			public «List»<«ValidationResult»<?>> getValidationResults(«RosettaPath» path, «javaType» o) {
				return getComparisonResults(o)
					.stream()
					.map(res -> {
						if (!«method(Strings, "isNullOrEmpty")»(res.getError())) {
							return «method(ValidationResult, "failure")»("«javaType.rosettaName»", «ValidationResult.ValidationType».CARDINALITY, "«javaType.rosettaName»", path, "", res.getError());
						}
						return «method(ValidationResult, "success")»("«javaType.rosettaName»", «ValidationResult.ValidationType».CARDINALITY, "«javaType.rosettaName»", path, "");
					})
					.collect(«method(Collectors, "toList")»());
			}
		
		}
	'''
	
	def private StringConcatenationClient typeFormatClassBody(JavaPojoInterface javaType, String version, Iterable<RAttribute> attributes) {
		val conditions = attributes.map[it.RMetaAnnotatedType.RType.collectConditionsFromTypeAliases].flatten
			
		'''
		public class «javaType.toTypeFormatValidatorClass» implements «Validator»<«javaType»> {
			«IF conditions.size() > 0»
				«IF conditions.map[it.name].filter[it.equalsIgnoreCase("IsValidCodingScheme")].size > 0»
					//GEM-TH: cdm-ref-data validation mock impl
					protected cdm.base.staticdata.codelist.functions.ValidateFpMLCodingSchemeDomain func = new cdm.base.staticdata.codelist.ValidateFpMLCodingSchemeImpl();
				«ENDIF»
			«ENDIF»
		
			private «List»<«ComparisonResult»> getComparisonResults(«javaType» o) {
				return «Lists».<«ComparisonResult»>newArrayList(
						«FOR attrCheck : attributes.map[checkTypeFormat(javaType, it)].filter[it !== null] SEPARATOR ", "»
							«attrCheck»
						«ENDFOR»
						«FOR condCheck : attributes.map[checkTypeAliasFormat(javaType, it)].filter[it !== null] SEPARATOR ", "»
							«condCheck»
						«ENDFOR»
					);
			}
		
			@Override
			public «ValidationResult»<«javaType»> validate(«RosettaPath» path, «javaType» o) {
				String error = getComparisonResults(o)
					.stream()
					.filter(res -> !res.get())
					.map(res -> res.getError())
					.collect(«method(Collectors, "joining")»("; "));

				if (!«method(Strings, "isNullOrEmpty")»(error)) {
					return «method(ValidationResult, "failure")»("«javaType.rosettaName»", «ValidationResult.ValidationType».TYPE_FORMAT, "«javaType.rosettaName»", path, "", error);
				}
				return «method(ValidationResult, "success")»("«javaType.rosettaName»", «ValidationResult.ValidationType».TYPE_FORMAT, "«javaType.rosettaName»", path, "");
			}
		
			@Override
			public «List»<«ValidationResult»<?>> getValidationResults(«RosettaPath» path, «javaType» o) {
				return getComparisonResults(o)
					.stream()
					.map(res -> {
						if (!«method(Strings, "isNullOrEmpty")»(res.getError())) {
							return «method(ValidationResult, "failure")»("«javaType.rosettaName»", «ValidationResult.ValidationType».TYPE_FORMAT, "«javaType.rosettaName»", path, "", res.getError());
						}
						return «method(ValidationResult, "success")»("«javaType.rosettaName»", «ValidationResult.ValidationType».TYPE_FORMAT, "«javaType.rosettaName»", path, "");
					})
					.collect(«method(Collectors, "toList")»());
			}
		
		}
		'''
	}

	def private StringConcatenationClient onlyExistsClassBody(JavaPojoInterface javaType, String version, Iterable<RAttribute> attributes) {
		
		'''
		public class «javaType.toOnlyExistsValidatorClass» implements «ValidatorWithArg»<«javaType», «Set»<String>> {

			/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
			@Override
			public <T2 extends «javaType»> «ValidationResult»<«javaType»> validate(«RosettaPath» path, T2 o, «Set»<String> fields) {
				«Map»<String, Boolean> fieldExistenceMap = «ImmutableMap».<String, Boolean>builder()
						«FOR attr : attributes»
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

	private def StringConcatenationClient checkCardinality(JavaPojoInterface javaType, RAttribute attr) {
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
	
	//GEM-TH: Collect conditions and arguments from typeAliases to generate external domain validators.
	private def StringConcatenationClient checkTypeAliasFormat(JavaPojoInterface javaType, RAttribute attr) {
		val conditions = attr.RMetaAnnotatedType.RType.collectConditionsFromTypeAliases
		val args = attr.RMetaAnnotatedType.RType.collectArgumentsFromTypeAliases
		
		if (conditions.size() == 0) {
			null
		} else {
			val prop = javaType.findProperty(attr.name)
			val propCode = prop.applyGetter(JavaExpression.from('''o''', javaType));
			'''
			«FOR cond : conditions»
				«IF cond.getName().equalsIgnoreCase("IsValidCodingScheme")»
					«IF attr.isMulti»
						«IF !attr.RMetaAnnotatedType.hasMeta»
							(«ComparisonResult») «method(Optional, "ofNullable")»(«propCode»).orElse(«method(Collections, "emptyList")»())
								.stream()
								.filter(it -> !func.evaluate(it, "«args.get("domain").getSingle()»"))
								.collect(«Collectors».collectingAndThen(
									«Collectors».joining(", "), 
									it -> it.isEmpty() ? «method(ComparisonResult, "success")»() : «method(ComparisonResult, "failure")»(it + " code not found in domain '«args.get("domain").getSingle()»'")
								))
						«ELSE»
							(«ComparisonResult») «method(Optional, "ofNullable")»(«propCode»).orElse(«method(Collections, "emptyList")»())
								.stream().map(«prop.type.itemType»::getValue)
								.filter(it -> !func.evaluate(it, "«args.get("domain").getSingle()»"))
								.collect(«Collectors».collectingAndThen(
									«Collectors».joining(", "), 
									it -> it.isEmpty() ? «method(ComparisonResult, "success")»() : «method(ComparisonResult, "failure")»(it + " code not found in domain '«args.get("domain").getSingle()»'")
								))
						«ENDIF»
					«ELSE»
						func.evaluate(«javaType.getAttributeValue(attr)», "«args.get("domain").getSingle()»")?
							«method(ComparisonResult, "success")»() : «method(ComparisonResult, "failure")»(«javaType.getAttributeValue(attr)» + " code not found in domain '«args.get("domain").getSingle()»'")
					«ENDIF»
				«ENDIF»
			«ENDFOR»
			'''
		}
	}
		
	private def StringConcatenationClient checkTypeFormat(JavaPojoInterface javaType, RAttribute attr) {
		val t = attr.RMetaAnnotatedType.RType.stripFromTypeAliases
		
		if (t instanceof RStringType) {
			if (t != UNCONSTRAINED_STRING) {
				val min = t.interval.minBound
				val max = t.interval.max.optional
				val pattern = t.pattern.optionalPattern
								
				return '''«method(ExpressionOperators, "checkString")»("«attr.name»", «javaType.getAttributeValue(attr)», «min», «max», «pattern»)'''
			}
		} else if (t instanceof RNumberType) {
			if (t != UNCONSTRAINED_NUMBER) {
				val digits = t.digits.optional
				val fractionalDigits = t.fractionalDigits.optional
				val min = t.interval.min.optionalBigDecimal
				val max = t.interval.max.optionalBigDecimal
				
				return '''«method(ExpressionOperators, "checkNumber")»("«attr.name»", «javaType.getAttributeValue(attr)», «digits», «fractionalDigits», «min», «max»)'''
			}
		}
		return null
	}
	
	private def StringConcatenationClient getAttributeValue(JavaPojoInterface javaType, RAttribute attr) {
		val prop = javaType.findProperty(attr.name)
		val propCode = prop.applyGetter(JavaExpression.from('''o''', javaType));
		if (!attr.RMetaAnnotatedType.hasMeta) {
			'''«propCode»'''
		} else {
			val jt = prop.type
			if (jt.isList) {
				val itemType = jt.itemType
				'''«propCode».stream().map(«itemType»::getValue).collect(«Collectors».toList())'''
			} else {
				'''«propCode».getValue()'''
			}
		}
	}
	private def StringConcatenationClient optional(Optional<? extends Object> v) {
		if (v.isPresent) {
			'''«method(Optional, "of")»(«v.get»)'''
		} else {
			'''«method(Optional, "empty")»()'''
		}
	}
	private def StringConcatenationClient optionalPattern(Optional<Pattern> v) {
		if (v.isPresent) {
			'''«method(Optional, "of")»(«Pattern».compile("«StringEscapeUtils.escapeJava(v.get.toString)»"))'''
		} else {
			'''«method(Optional, "empty")»()'''
		}
	}
	private def StringConcatenationClient optionalBigDecimal(Optional<BigDecimal> v) {
		if (v.isPresent) {
			'''«method(Optional, "of")»(new «BigDecimal»("«StringEscapeUtils.escapeJava(v.get.toString)»"))'''
		} else {
			'''«method(Optional, "empty")»()'''
		}
	}
}
