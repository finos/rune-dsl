package com.regnosys.rosetta.generator.java.object

import com.google.common.base.Strings
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.RosettaType
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

class ValidatorsGenerator {

	@Inject extension ImportManagerExtension
	@Inject extension RosettaExtensions

	def generate(JavaNames names, IFileSystemAccess2 fsa, Data data, String version) {
		fsa.generateFile(names.packages.model.typeValidation.withForwardSlashes + '/' + data.name + 'Validator.java',
			generatClass(names, data, version))
		fsa.generateFile(names.packages.model.existsValidation.withForwardSlashes + '/' + onlyExistsValidatorName(data) + '.java',
			generateOnlyExistsValidator(names, data, version))
	}

	private def generatClass(JavaNames names, Data d, String version) {
		val scope = new JavaScope
		buildClass(names.packages.model.typeValidation, d.classBody(names, version, d.allAttributes), scope)
	}

	private def generateOnlyExistsValidator(JavaNames names, Data d, String version) {
		val scope = new JavaScope
		buildClass(names.packages.model.existsValidation, d.onlyExistsClassBody(names, version, d.allAttributes), scope)
	}

	def private StringConcatenationClient classBody(Data c, JavaNames names, String version, Iterable<Attribute> attributes) '''
		public class «c.name»Validator implements «Validator»<«names.toJavaType(c)»> {
		
			@Override
			public «ValidationResult»<«c.name»> validate(«RosettaPath» path, «c.name» o) {
				String error = 
					«Lists».<«ComparisonResult»>newArrayList(
						«FOR attr : attributes SEPARATOR ","»
							«checkCardinality(attr.toExpandedAttribute)»
						«ENDFOR»
					).stream().filter(res -> !res.get()).map(res -> res.getError()).collect(«method(Collectors, "joining")»("; "));
				
				if (!«method(Strings, "isNullOrEmpty")»(error)) {
					return «method(ValidationResult, "failure")»("«c.name»", «ValidationResult.ValidationType».MODEL_INSTANCE, o.getClass().getSimpleName(), path, "", error);
				}
				return «method(ValidationResult, "success")»("«c.name»", «ValidationResult.ValidationType».MODEL_INSTANCE, o.getClass().getSimpleName(), path, "");
			}
		
		}
	'''

	static def onlyExistsValidatorName(RosettaType c) {
		return c.name + 'OnlyExistsValidator'
	}

	def private StringConcatenationClient onlyExistsClassBody(Data c, JavaNames names, String version, Iterable<Attribute> attributes) '''
		public class «onlyExistsValidatorName(c)» implements «ValidatorWithArg»<«names.toJavaType(c)», «Set»<String>> {
		
			@Override
			public <T2 extends «c.name»> «ValidationResult»<«c.name»> validate(«RosettaPath» path, T2 o, «Set»<String> fields) {
				«Map»<String, Boolean> fieldExistenceMap = «ImmutableMap».<String, Boolean>builder()
						«FOR attr : attributes»
							.put("«attr.name»", «ExistenceChecker».isSet(o.get«attr.name?.toFirstUpper»()))
						«ENDFOR»
						.build();
				
				// Find the fields that are set
				«Set»<String> setFields = fieldExistenceMap.entrySet().stream()
						.filter(Map.Entry::getValue)
						.map(Map.Entry::getKey)
						.collect(«Collectors».toSet());
				
				if (setFields.equals(fields)) {
					return «method(ValidationResult, "success")»("«c.name»", «ValidationType».ONLY_EXISTS, o.getClass().getSimpleName(), path, "");
				}
				return «method(ValidationResult, "failure")»("«c.name»", «ValidationType».ONLY_EXISTS, o.getClass().getSimpleName(), path, "",
						String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
			}
		}
	'''

	private def StringConcatenationClient checkCardinality(ExpandedAttribute attr) '''
		«IF attr.isMultiple»
			«method(ExpressionOperators, "checkCardinality")»("«attr.name»", o.get«attr.name?.toFirstUpper»()==null?0:o.get«attr.name?.toFirstUpper»().size(), «attr.inf», «attr.sup»)
		«ELSE»
			«method(ExpressionOperators, "checkCardinality")»("«attr.name»", o.get«attr.name?.toFirstUpper»()!=null ? 1 : 0, «attr.inf», «attr.sup»)
		«ENDIF»
	'''
}
