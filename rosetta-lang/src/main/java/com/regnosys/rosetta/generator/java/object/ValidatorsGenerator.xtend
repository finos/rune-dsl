package com.regnosys.rosetta.generator.java.object

import com.google.common.base.Strings
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
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

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.types.TypeSystem
import com.regnosys.rosetta.types.builtin.RStringType
import com.regnosys.rosetta.types.builtin.RNumberType
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import java.util.Optional
import java.util.regex.Pattern
import org.apache.commons.text.StringEscapeUtils
import java.util.List
import java.math.BigDecimal
import com.rosetta.util.types.JavaWildcardTypeArgument
import com.rosetta.util.types.JavaReferenceType
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaParameterizedType

class ValidatorsGenerator {

	@Inject extension ImportManagerExtension
	@Inject extension RosettaExtensions
	@Inject extension JavaTypeTranslator
	@Inject extension RosettaTypeProvider
	@Inject extension TypeSystem
	@Inject extension RBuiltinTypeService

	def generate(RootPackage root, IFileSystemAccess2 fsa, Data data, String version) {
		val t = new RDataType(data)
		fsa.generateFile(t.toValidatorClass.canonicalName.withForwardSlashes + ".java",
			generateClass(root, data, version))
		fsa.generateFile(t.toTypeFormatValidatorClass.canonicalName.withForwardSlashes + ".java",
			generateTypeFormatValidator(root, data, version))
		fsa.generateFile(t.toOnlyExistsValidatorClass.canonicalName.withForwardSlashes + ".java",
			generateOnlyExistsValidator(root, data, version))
	}

	private def generateClass(RootPackage root, Data d, String version) {
		val scope = new JavaScope(root.typeValidation)
		buildClass(root.typeValidation, new RDataType(d).classBody(version, d.allNonOverridesAttributes), scope)
	}
	
	private def generateTypeFormatValidator(RootPackage root, Data d, String version) {
		val scope = new JavaScope(root.typeValidation)
		buildClass(root.typeValidation, new RDataType(d).typeFormatClassBody(version, d.allNonOverridesAttributes), scope)
	}

	private def generateOnlyExistsValidator(RootPackage root, Data d, String version) {
		val scope = new JavaScope(root.existsValidation)
		buildClass(root.existsValidation, new RDataType(d).onlyExistsClassBody(version, d.allNonOverridesAttributes), scope)
	}

	def private StringConcatenationClient classBody(RDataType t, String version, Iterable<Attribute> attributes) '''
		public class «t.toValidatorClass» implements «Validator»<«t.toJavaType»> {

			@Override
			public «ValidationResult»<«t.toJavaType»> validate(«RosettaPath» path, «t.toJavaType» o) {
				/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
				String error = 
					«Lists».<«ComparisonResult»>newArrayList(
						«FOR attrCheck : attributes.map[checkCardinality(toExpandedAttribute)].filter[it !== null] SEPARATOR ", "»
							«attrCheck»
						«ENDFOR»
					).stream().filter(res -> !res.get()).map(res -> res.getError()).collect(«method(Collectors, "joining")»("; "));
				
				if (!«method(Strings, "isNullOrEmpty")»(error)) {
					return «method(ValidationResult, "failure")»("«t.name»", «ValidationResult.ValidationType».CARDINALITY, o.getClass().getSimpleName(), path, "", error);
				}
				return «method(ValidationResult, "success")»("«t.name»", «ValidationResult.ValidationType».CARDINALITY, o.getClass().getSimpleName(), path, "");
			}
		
		}
	'''
	
	def private StringConcatenationClient typeFormatClassBody(RDataType t, String version, Iterable<Attribute> attributes) '''
		public class «t.toTypeFormatValidatorClass» implements «Validator»<«t.toJavaType»> {
		
			@Override
			public «ValidationResult»<«t.toJavaType»> validate(«RosettaPath» path, «t.toJavaType» o) {
				String error = 
					«Lists».<«ComparisonResult»>newArrayList(
						«FOR attrCheck : attributes.map[checkTypeFormat].filter[it !== null] SEPARATOR ", "»
							«attrCheck»
						«ENDFOR»
					).stream().filter(res -> !res.get()).map(res -> res.getError()).collect(«method(Collectors, "joining")»("; "));
				
				if (!«method(Strings, "isNullOrEmpty")»(error)) {
					return «method(ValidationResult, "failure")»("«t.name»", «ValidationResult.ValidationType».TYPE_FORMAT, o.getClass().getSimpleName(), path, "", error);
				}
				return «method(ValidationResult, "success")»("«t.name»", «ValidationResult.ValidationType».TYPE_FORMAT, o.getClass().getSimpleName(), path, "");
			}
		
		}
	'''

	def private StringConcatenationClient onlyExistsClassBody(RDataType t, String version, Iterable<Attribute> attributes) '''
		public class «t.toOnlyExistsValidatorClass» implements «ValidatorWithArg»<«t.toJavaType», «Set»<String>> {

			/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
			@Override
			public <T2 extends «t.toJavaType»> «ValidationResult»<«t.toJavaType»> validate(«RosettaPath» path, T2 o, «Set»<String> fields) {
				«Map»<String, Boolean> fieldExistenceMap = «ImmutableMap».<String, Boolean>builder()
						«FOR attr : attributes»
							.put("«attr.name»", «ExistenceChecker».isSet((«attr.toExpandedAttribute.toMultiMetaOrRegularJavaType») o.get«attr.name?.toFirstUpper»()))
						«ENDFOR»
						.build();
				
				// Find the fields that are set
				«Set»<String> setFields = fieldExistenceMap.entrySet().stream()
						.filter(Map.Entry::getValue)
						.map(Map.Entry::getKey)
						.collect(«Collectors».toSet());
				
				if (setFields.equals(fields)) {
					return «method(ValidationResult, "success")»("«t.name»", «ValidationType».ONLY_EXISTS, o.getClass().getSimpleName(), path, "");
				}
				return «method(ValidationResult, "failure")»("«t.name»", «ValidationType».ONLY_EXISTS, o.getClass().getSimpleName(), path, "",
						String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
			}
		}
	'''

	private def StringConcatenationClient checkCardinality(ExpandedAttribute attr) {
		if (attr.inf === 0 && attr.isUnbound) {
			null
		} else {
	        /* Casting is required to ensure types are output to ensure recompilation in Rosetta */
			'''
			«IF attr.isMultiple»
				«method(ExpressionOperators, "checkCardinality")»("«attr.name»", («attr.toMultiMetaOrRegularJavaType») o.get«attr.name?.toFirstUpper»() == null ? 0 : ((«attr.toMultiMetaOrRegularJavaType») o.get«attr.name?.toFirstUpper»()).size(), «attr.inf», «attr.sup»)
			«ELSE»
				«method(ExpressionOperators, "checkCardinality")»("«attr.name»", («attr.toMultiMetaOrRegularJavaType») o.get«attr.name?.toFirstUpper»() != null ? 1 : 0, «attr.inf», «attr.sup»)
			«ENDIF»
			'''
		}
	}
		
	private def StringConcatenationClient checkTypeFormat(Attribute attr) {
		val t = attr.RTypeOfSymbol.stripFromTypeAliases
		if (t instanceof RStringType) {
			if (t != UNCONSTRAINED_STRING) {
				val min = t.interval.minBound
				val max = t.interval.max.optional
				val pattern = t.pattern.optionalPattern
								
				return '''«method(ExpressionOperators, "checkString")»("«attr.name»", «attr.attributeValue», «min», «max», «pattern»)'''
			}
		} else if (t instanceof RNumberType) {
			if (t != UNCONSTRAINED_NUMBER) {
				val digits = t.digits.optional
				val fractionalDigits = t.fractionalDigits.optional
				val min = t.interval.min.optionalBigDecimal
				val max = t.interval.max.optionalBigDecimal
				
				return '''«method(ExpressionOperators, "checkNumber")»("«attr.name»", «attr.attributeValue», «digits», «fractionalDigits», «min», «max»)'''
			}
		}
		return null
	}
	
	private def StringConcatenationClient getAttributeValue(Attribute attr) {
		if (attr.metaAnnotations.empty) {
			'''o.get«attr.name?.toFirstUpper»()'''
		} else {
			val jt = attr.toExpandedAttribute.toMultiMetaOrRegularJavaType
			if (jt instanceof JavaParameterizedType && (jt as JavaParameterizedType).getBaseType.equals(JavaClass.from(List))) {
				val typeArg = (jt as JavaParameterizedType).getArguments.head
				val itemType = if (typeArg instanceof JavaWildcardTypeArgument) {
					typeArg.bound.get
				} else {
					typeArg as JavaReferenceType
				}
				'''o.get«attr.name?.toFirstUpper»().stream().map(«itemType»::getValue).collect(«Collectors».toList())'''
			} else {
				'''o.get«attr.name?.toFirstUpper»().getValue()'''
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
