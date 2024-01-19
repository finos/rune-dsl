package com.regnosys.rosetta.generator.java.object

import com.google.common.base.Strings
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
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
import com.rosetta.model.lib.validation.ValidationType
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
import java.math.BigDecimal
import javax.inject.Inject
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil

class ValidatorsGenerator {

	@Inject extension ImportManagerExtension
	@Inject extension RosettaExtensions
	@Inject extension JavaTypeTranslator
	@Inject extension RosettaTypeProvider
	@Inject extension TypeSystem
	@Inject extension RBuiltinTypeService
	@Inject extension JavaTypeUtil

	def generate(RootPackage root, IFileSystemAccess2 fsa, Data data, String version) {
		val t = new RDataType(data)
		fsa.generateFile(t.toOnlyExistsValidatorClass.canonicalName.withForwardSlashes + ".java",
			generateOnlyExistsValidator(root, data, version))
	}


	private def generateOnlyExistsValidator(RootPackage root, Data d, String version) {
		val scope = new JavaScope(root.existsValidation)
		buildClass(root.existsValidation, new RDataType(d).onlyExistsClassBody(version, d.allNonOverridesAttributes), scope)
	}

	def private StringConcatenationClient onlyExistsClassBody(RDataType t, String version, Iterable<Attribute> attributes) '''
		public class «t.toOnlyExistsValidatorClass» implements «ValidatorWithArg»<«t.toJavaType», «Set»<String>> {

			/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
			@Override
			public <T2 extends «t.toJavaType»> «ValidationResult» validate(«RosettaPath» path, T2 o, «Set»<String> fields) {
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
					return «method(ValidationResult, "success")»(path);
				}
				return «method(ValidationResult, "failure")»(path,
						String.format("[%s] should only be set.  Set fields: %s", fields, setFields), null);
			}
		}
	'''
}
