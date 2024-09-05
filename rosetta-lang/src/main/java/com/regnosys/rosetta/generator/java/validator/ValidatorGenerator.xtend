package com.regnosys.rosetta.generator.java.validator

import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.TypeSystem
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import com.regnosys.rosetta.types.builtin.RNumberType
import com.regnosys.rosetta.types.builtin.RStringType
import com.rosetta.model.lib.ModelSymbolId
import com.rosetta.model.lib.validation.AttributeValidation
import com.rosetta.model.lib.validation.RosettaModelObjectValidator
import com.rosetta.model.lib.validation.TypeValidation
import com.rosetta.model.lib.validation.ValidationUtil
import com.rosetta.util.DottedPath
import java.math.BigDecimal
import java.util.ArrayList
import java.util.Optional
import java.util.regex.Pattern
import java.util.stream.Collectors
import javax.inject.Inject
import org.apache.commons.text.StringEscapeUtils
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2
import java.util.List
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ConditionValidation
import com.rosetta.util.types.generated.GeneratedJavaClass
import com.rosetta.model.lib.validation.ElementValidationResult
import com.regnosys.rosetta.types.RAttribute

class ValidatorGenerator {
	@Inject extension ImportManagerExtension
	@Inject extension RosettaExtensions
	@Inject extension JavaTypeTranslator
	@Inject extension TypeSystem
	@Inject extension RBuiltinTypeService
	@Inject extension JavaTypeUtil

	def generate(RootPackage root, IFileSystemAccess2 fsa, RDataType type, String version) {
		val topScope = new JavaScope(root.typeValidation)

		val classBody = type.classBody(topScope, root)
		val content = buildClass(root.typeValidation, classBody, topScope)
		fsa.generateFile('''«root.typeValidation.withForwardSlashes»/«type.name»Validator.java''', content)
	}

	private def StringConcatenationClient classBody(RDataType type, JavaScope scope, RootPackage root) {

		val modelPojo = type.toJavaReferenceType
		val data = type.EObject
		'''
			public class «data.name»Validator implements «RosettaModelObjectValidator»<«modelPojo»>{
				«FOR con : data.conditions»
				@«Inject» protected «new GeneratedJavaClass(root.condition, con.conditionName(type).toConditionJavaType, Object)» «con.conditionName(type).toFirstLower» ;
										
				«ENDFOR»
				
				@Override
				public «TypeValidation» validate(«RosettaPath» path, «modelPojo» o) {
				
					«DottedPath» packageName = «DottedPath».of(o.getClass().getPackage().toString());
					«String» simpleName = o.getClass().getSimpleName();
					«ModelSymbolId» modelSymbolId = new «ModelSymbolId»(packageName, simpleName);
				
				 	«List»<«AttributeValidation»> attributeValidations = new «ArrayList»<>();
				 	«FOR attribute : type.allNonOverridenAttributes»
				 	 	attributeValidations.add(validate«attribute.name.toFirstUpper»(«attribute.attributeValue», path));
				 	«ENDFOR»
				 	
				 	«List»<«ConditionValidation»> conditionValidations = new «ArrayList»<>();
				 	«FOR dataCondition : data.conditions»
				 		conditionValidations.add(validate«dataCondition.conditionName(type).toFirstUpper»(o, path));
				 	«ENDFOR»

				 	return new «TypeValidation»(modelSymbolId, attributeValidations, conditionValidations);
				}
				
				«FOR attribute : type.allNonOverridenAttributes»
				public «AttributeValidation» validate«attribute.name.toFirstUpper»(«attribute.toJavaType» atr, «RosettaPath» path) {
					«List»<«ElementValidationResult»> validationResults = new «ArrayList»<>();
					«val cardinalityCheck = checkCardinality(attribute)»
					
					«ElementValidationResult» cardinalityValidation =«IF cardinalityCheck !== null»«cardinalityCheck»;«ELSE»«ElementValidationResult».success(path);«ENDIF»
					
					«IF !attribute.isMulti»«val typeFormatCheck = checkTypeFormat(attribute, "atr")»
					    «IF typeFormatCheck !== null»validationResults.add(«typeFormatCheck»);«ENDIF»
					«ELSE»
					if (atr != null) {
					    for («attribute.toItemJavaType» atrb : atr) {
							«val typeFormatCheck = checkTypeFormat(attribute, "atrb" )»
							«IF typeFormatCheck !== null»validationResults.add(«typeFormatCheck»);«ENDIF»
						}
					}
					«ENDIF»
					
					return new «AttributeValidation»("«attribute.name»", cardinalityValidation, validationResults);
				}
				«ENDFOR»
				
				«FOR dataCondition : data.conditions»
				public «ConditionValidation» validate«dataCondition.conditionName(type).toFirstUpper»(«modelPojo» data, «RosettaPath» path) {
					«ElementValidationResult» result = «dataCondition.conditionName(type).toFirstLower».validate(path, data);
					
					return new «ConditionValidation»(«dataCondition.conditionName(type).toFirstLower».toString(), result);
				}
				«ENDFOR»
			}
		'''
		
	}
	private def StringConcatenationClient checkCardinality(RAttribute attr) {
		if (attr.cardinality.minBound === 0 && attr.cardinality.unboundedRight) {
			null
		} else {
			if (attr.isMulti) {
				'''«method(ValidationUtil, "checkCardinality")»("«attr.name.toString»", atr == null ? 0 : atr.size(), «attr.cardinality.minBound», «attr.cardinality.max.orElse(0)» , path)'''
			} else {
				'''«method(ValidationUtil, "checkCardinality")»("«attr.name.toString»", atr != null ? 1 : 0, «attr.cardinality.minBound», «attr.cardinality.max.orElse(0)», path)'''
			}
		}
	}
		
	private def StringConcatenationClient checkTypeFormat(RAttribute attr, String atrVariable) {
		val t = attr.RType.stripFromTypeAliases
		if (t instanceof RStringType) {
			if (t != UNCONSTRAINED_STRING) {
				val min = t.interval.minBound
				val max = t.interval.max.optional
				val pattern = t.pattern.optionalPattern
								
				return '''«method(ValidationUtil, "checkString")»("«attr.name»", «atrVariable», «min», «max», «pattern», path)'''
			}
		} else if (t instanceof RNumberType) {
			if (t != UNCONSTRAINED_NUMBER) {
				val digits = t.digits.optional
				val fractionalDigits = t.fractionalDigits.optional
				val min = t.interval.min.optionalBigDecimal
				val max = t.interval.max.optionalBigDecimal
				
				return '''«method(ValidationUtil, "checkNumber")»("«attr.name»", «atrVariable», «digits», «IF !t.isInteger»«fractionalDigits», «ENDIF»«min», «max», path)'''
			}
		}
		return null
	}
	
	private def StringConcatenationClient getAttributeValue(RAttribute attr) {
		if (attr.metaAnnotations.empty) {
			'''o.get«attr.name?.toFirstUpper»()'''
		} else {
			val jt = attr.toMetaJavaType
			if (jt.isList) {
				val itemType = jt.itemType
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