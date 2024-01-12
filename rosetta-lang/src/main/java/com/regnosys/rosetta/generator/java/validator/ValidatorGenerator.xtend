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

class ValidatorGenerator {
	@Inject extension ImportManagerExtension
	@Inject extension RosettaExtensions
	@Inject extension JavaTypeTranslator
	@Inject extension RosettaTypeProvider
	@Inject extension TypeSystem
	@Inject extension RBuiltinTypeService
	@Inject extension JavaTypeUtil

	def generate(RootPackage root, IFileSystemAccess2 fsa, Data data, String version) {
		val topScope = new JavaScope(root.typeValidation)

		val classBody = data.classBody(topScope)
		val content = buildClass(root.typeValidation, classBody, topScope)
		fsa.generateFile('''«root.typeValidation.withForwardSlashes»/«data.name»Validator.java''', content)
	}

	private def StringConcatenationClient classBody(Data data, JavaScope scope) {

		val modelPojo = new RDataType(data).toJavaReferenceType
		val rDataType = new RDataType(data)
		'''
			public class «data.name»Validator implements «RosettaModelObjectValidator»<«modelPojo»>{
				
				@Override
				public «TypeValidation» validate(«RosettaPath» path, «rDataType.toJavaType» o) {
				
					«DottedPath» packageName = «DottedPath».of(o.getClass().getPackage().toString());
					«String» simpleName = o.getClass().getSimpleName();
					«ModelSymbolId» modelSymbolId = new «ModelSymbolId»(packageName, simpleName);
				
				 	«List»<«AttributeValidation»> attributeValidations = new «ArrayList»<>();
				 	«FOR attribute : data.allAttributes»
				 	 	attributeValidations.add(validate«attribute.name.toFirstUpper»(o.get«attribute.name.toFirstUpper»(), path));
				 	«ENDFOR»
				}
				
				«FOR attribute : data.allAttributes»
				public «AttributeValidation» validate«attribute.name.toFirstUpper»(«attribute.RTypeOfSymbol.toJavaType» atr, «RosettaPath» path) {
					«List»<«ValidationResult»> validationResults = new «ArrayList»<>();
					«ValidationResult» cardinalityValidation = «checkCardinality(attribute)»;
					validationResults.add(«checkTypeFormat(attribute)»);
					
					return new «AttributeValidation»("«attribute.name»", cardinalityValidation, validationResults);
				}
				«ENDFOR»
			}
		'''
		
	}
	private def StringConcatenationClient checkCardinality(Attribute attr) {
		if (attr.card.inf === 0 && attr.card.unbounded) {
			null
		} else {
			/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
			if (attr.card.isIsMany) {
				'''«method(ValidationUtil, "checkCardinality")»("«attr.name»", o.get«attr.name?.toFirstUpper»() == null ? 0 : o.get«attr.name?.toFirstUpper»().size(), «attr.card.inf», «attr.card.sup» , null)'''
			} else {
				'''«method(ValidationUtil, "checkCardinality")»("«attr.name»", o.get«attr.name?.toFirstUpper»() != null ? 1 : 0, «attr.card.inf», «attr.card.sup», null)'''
			}
		}
	}
		
	private def StringConcatenationClient checkTypeFormat(Attribute attr) {
		val t = attr.RTypeOfSymbol.stripFromTypeAliases
		if (t instanceof RStringType) {
			if (t != UNCONSTRAINED_STRING) {
				val min = t.interval.minBound
				val max = t.interval.max.optional
				val pattern = t.pattern.optionalPattern
								
				return '''«method(ValidationUtil, "checkString")»("«attr.name»", «attr.attributeValue», «min», «max», «pattern», null)'''
			}
		} else if (t instanceof RNumberType) {
			if (t != UNCONSTRAINED_NUMBER) {
				val digits = t.digits.optional
				val fractionalDigits = t.fractionalDigits.optional
				val min = t.interval.min.optionalBigDecimal
				val max = t.interval.max.optionalBigDecimal
				
				return '''«method(ValidationUtil, "checkNumber")»("«attr.name»", «attr.attributeValue», «digits», «fractionalDigits», «min», «max», null)'''
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
≈Ω