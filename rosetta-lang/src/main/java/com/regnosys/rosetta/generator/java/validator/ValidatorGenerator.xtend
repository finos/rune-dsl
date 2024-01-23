package com.regnosys.rosetta.generator.java.validator

import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.RosettaTypeProvider
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
import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import java.util.List
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ConditionValidation
import com.rosetta.util.types.generated.GeneratedJavaClass
import java.util.Set
import java.util.Map
import com.rosetta.model.lib.validation.ExistenceChecker
import com.rosetta.model.lib.validation.ValidatorWithArg
import com.google.common.collect.ImmutableMap
import com.rosetta.model.lib.validation.ValidationData
import java.util.concurrent.ConcurrentHashMap
import com.regnosys.rosetta.types.RObjectFactory

class ValidatorGenerator {
	@Inject extension ImportManagerExtension
	@Inject extension RosettaExtensions
	@Inject extension JavaTypeTranslator
	@Inject extension RosettaTypeProvider
	@Inject extension TypeSystem
	@Inject extension RBuiltinTypeService
	@Inject extension JavaTypeUtil
	@Inject extension RObjectFactory

	def generate(RootPackage root, IFileSystemAccess2 fsa, Data data, String version) {
		val topScope = new JavaScope(root.typeValidation)

		val classBody = data.classBody(topScope, root)
		val content = buildClass(root.typeValidation, classBody, topScope)
		fsa.generateFile('''«root.typeValidation.withForwardSlashes»/«data.name»Validator.java''', content)
	}

	private def StringConcatenationClient classBody(Data data, JavaScope scope, RootPackage root) {

		val modelPojo = new RDataType(data).toJavaReferenceType
		val rDataType = new RDataType(data)
		'''
			public class «data.name»Validator implements «RosettaModelObjectValidator»<«modelPojo»>{
				«FOR con : data.conditions»
				@«Inject» protected «new GeneratedJavaClass(root.condition, con.conditionName(data).toConditionJavaType, Object)» «con.conditionName(data).toFirstLower» ;
										
				«ENDFOR»
				
				@Override
				public «TypeValidation» validate(«RosettaPath» path, «rDataType.toJavaReferenceType» o) {
				
					«DottedPath» packageName = «DottedPath».of(o.getClass().getPackage().toString());
					«String» simpleName = o.getClass().getSimpleName();
					«ModelSymbolId» modelSymbolId = new «ModelSymbolId»(packageName, simpleName);
				
				 	«List»<«AttributeValidation»> attributeValidations = new «ArrayList»<>();
				 	«FOR attribute : data.allNonOverridesAttributes»
				 	 	attributeValidations.add(validate«attribute.name.toFirstUpper»(«attribute.attributeValue», path));
				 	«ENDFOR»
				 	
				 	«List»<«ConditionValidation»> conditionValidations = new «ArrayList»<>();
				 	«FOR dataCondition : data.conditions»
				 		conditionValidations.add(validate«dataCondition.conditionName(data).toFirstUpper»(o, path));
				 	«ENDFOR»

				 	return new «TypeValidation»(modelSymbolId, attributeValidations, conditionValidations);
				}
				
				«FOR attribute : data.allNonOverridesAttributes»
				public «AttributeValidation» validate«attribute.name.toFirstUpper»(«attribute.buildRAttribute.attributeToJavaType» atr, «RosettaPath» path) {
					«List»<«ValidationResult»> validationResults = new «ArrayList»<>();
					«val cardinalityCheck = checkCardinality(attribute)»
					
					«ValidationResult» cardinalityValidation =«IF cardinalityCheck !== null»«cardinalityCheck»;«ELSE»«ValidationResult».success(path);«ENDIF»
					
					«IF !attribute.card.isIsMany»«val typeFormatCheck = checkTypeFormat(attribute, "atr")»
					    «IF typeFormatCheck !== null»validationResults.add(«typeFormatCheck»);«ENDIF»
					«ELSE»
					if (atr != null) {
					    for («attribute.RTypeOfSymbol.toJavaReferenceType» atrb : atr) {
							«val typeFormatCheck = checkTypeFormat(attribute, "atrb" )»
							«IF typeFormatCheck !== null»validationResults.add(«typeFormatCheck»);«ENDIF»
						}
					}
					«ENDIF»
					
					return new «AttributeValidation»("«attribute.name»", cardinalityValidation, validationResults);
				}
				«ENDFOR»
				
				«FOR dataCondition : data.conditions»
				public «ConditionValidation» validate«dataCondition.conditionName(data).toFirstUpper»(«rDataType.toJavaReferenceType» data, «RosettaPath» path) {
					«ValidationResult» result = «dataCondition.conditionName(data).toFirstLower».validate(path, data);
					
					return new «ConditionValidation»(«dataCondition.conditionName(data).toFirstLower».toString(), result);
				}
				«ENDFOR»
			}
		'''
		
	}
	private def StringConcatenationClient checkCardinality(Attribute attr) {
		if (attr.card.inf === 0 && attr.card.unbounded) {
			null
		} else {
			if (attr.card.isIsMany) {
				'''«method(ValidationUtil, "checkCardinality")»("«attr.name.toString»", atr == null ? 0 : atr.size(), «attr.card.inf», «attr.card.sup» , path)'''
			} else {
				'''«method(ValidationUtil, "checkCardinality")»("«attr.name.toString»", atr != null ? 1 : 0, «attr.card.inf», «attr.card.sup», path)'''
			}
		}
	}
		
	private def StringConcatenationClient checkTypeFormat(Attribute attr, String atrVariable) {
		val t = attr.RTypeOfSymbol.stripFromTypeAliases
		if (t instanceof RStringType) {
			if (t != UNCONSTRAINED_STRING) {
				val min = t.interval.minBound
				val max = t.interval.max.optional
				val pattern = t.pattern.optionalPattern
								
				return '''«method(ValidationUtil, "checkString")»("«attr.name»", «atrVariable», «min», «max», «pattern», path)'''
			}
		} else if (t instanceof RNumberType) {
			val testintomethod = false
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
	
	private def StringConcatenationClient getAttributeValue(Attribute attr) {
		if (attr.metaAnnotations.empty) {
			'''o.get«attr.name?.toFirstUpper»()'''
		} else {
			val jt = attr.toExpandedAttribute.toMultiMetaOrRegularJavaType
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