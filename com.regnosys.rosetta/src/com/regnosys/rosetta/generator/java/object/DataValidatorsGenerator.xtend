package com.regnosys.rosetta.generator.java.object

import com.google.common.base.Strings
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ComparisonResult
import com.rosetta.model.lib.validation.ExistenceChecker
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.validation.ValidationResult.ValidationType
import com.rosetta.model.lib.validation.Validator
import com.rosetta.model.lib.validation.ValidatorHelper
import com.rosetta.model.lib.validation.ValidatorWithArg
import java.util.List
import java.util.Map
import java.util.Set
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.object.ModelObjectGenerator.*

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*

class DataValidatorsGenerator {

	@Inject extension ImportManagerExtension

	def generate(JavaNames names, IFileSystemAccess2 fsa, Data data, String version) {
		fsa.generateFile(names.packages.model.typeValidation.directoryName + '/' + data.name + 'Validator.java',
			generatClass(names, data, version))
		fsa.generateFile(names.packages.model.existsValidation.directoryName + '/' + onlyExistsValidatorName(data) + '.java',
			generateOnlyExistsValidator(names, data, version))
	}

	private def generatClass(JavaNames names, Data d, String version) {
		val classBody = tracImports(d.classBody(names, version, d.getExpandedAttributes(false)))
		'''
			package «names.packages.model.typeValidation.name»;
			
			«FOR imp : classBody.imports»
				import «imp»;
			«ENDFOR»
			«FOR imp : classBody.staticImports»
				import static «imp»;
			«ENDFOR»
			
			«classBody.toString»
		'''
	}

	private def generateOnlyExistsValidator(JavaNames names, Data d, String version) {
		val classBody = tracImports(d.onlyExistsClassBody(names, version))
		'''
			package «names.packages.model.existsValidation.name»;
			
			«FOR imp : classBody.imports»
				import «imp»;
			«ENDFOR»
			«FOR imp : classBody.staticImports»
				import static «imp»;
			«ENDFOR»
			
			«classBody.toString»
		'''
	}

	def private StringConcatenationClient classBody(Data c, JavaNames names, String version, List<ExpandedAttribute> attributes) '''
		public class «c.name»Validator implements «Validator»<«names.toJavaType(c)»> {
		
			@Override
			public «ValidationResult»<«c.name»> validate(«RosettaPath» path, «c.name» o) {
				String error = 
					«Lists».<«ComparisonResult»>newArrayList(
						«FOR attr : attributes SEPARATOR ","»
							«checkCardinality(attr)»
						«ENDFOR»
					).stream().filter(res -> !res.get()).map(res -> res.getError()).collect(«Collectors.importMethod("joining")»("; "));
				
				if (!«Strings.importMethod("isNullOrEmpty")»(error)) {
					return «ValidationResult.importMethod("failure")»("«c.name»", «ValidationResult.ValidationType».MODEL_INSTANCE, o.getClass().getSimpleName(), path, "", error);
				}
				return «ValidationResult.importMethod("success")»("«c.name»", «ValidationResult.ValidationType».MODEL_INSTANCE, o.getClass().getSimpleName(), path, "");
			}
			
			@Override
			public ValidationResult<«c.name»> validate(RosettaPath path, «RosettaModelObjectBuilder» b) {
				«c.name».«c.name»Builder o = («c.name».«c.name»Builder) b;
				String error = 
					Lists.<ComparisonResult>newArrayList(
						«FOR attr : attributes SEPARATOR ","»
							«checkCardinality(attr)»
						«ENDFOR»
					).stream().filter(res -> !res.get()).map(res -> res.getError()).collect(«Collectors.importMethod("joining")»("; "));
				
				if (!isNullOrEmpty(error)) {
					return failure("«c.name»", ValidationType.MODEL_INSTANCE, o.getClass().getSimpleName(), path, "", error);
				}
				return success("«c.name»", ValidationType.MODEL_INSTANCE, o.getClass().getSimpleName(), path, "");
			}
		
		}
	'''

	def private StringConcatenationClient onlyExistsClassBody(Data c, JavaNames names, String version) '''
		public class «onlyExistsValidatorName(c)» implements «ValidatorWithArg»<«names.toJavaType(c)», String> {
		
			@Override
			public «ValidationResult»<«c.name»> validate(«RosettaPath» path, «c.name» o, String field) {
				«Map»<String,Boolean> fieldExistenceMap = «ImmutableMap».<String, Boolean>builder()
						«FOR attr : c.attributes»
							.put("«attr.name»", «ExistenceChecker».isSet(o.get«attr.name?.toFirstUpper»()))
						«ENDFOR»
						.build();
				// Exclude given method name
				«Map»<String, Boolean> otherFieldsExistenceMap = fieldExistenceMap.entrySet().stream()
						.filter(x -> !x.getKey().equals(field))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				// Find any other fields that are set
				«Set»<String> setFields = otherFieldsExistenceMap.entrySet().stream()
						.filter(Map.Entry::getValue)
						.collect(«Collectors».toMap(Map.Entry::getKey, Map.Entry::getValue))
						.keySet();
		
				if (!setFields.isEmpty()) {
					return failure("«c.name»", «ValidationType».ONLY_EXISTS, o.getClass().getSimpleName(), path, "",
							String.format("[%s] is not the only field set. Other set fields: %s", field, setFields));
				}
				return success("«c.name»", ValidationType.ONLY_EXISTS, o.getClass().getSimpleName(), path, "");
			}
			
			@Override
			public ValidationResult<«c.name»> validate(RosettaPath path, «RosettaModelObjectBuilder» b, String field) {
				«c.name».«c.name»Builder o = («c.name».«c.name»Builder)b;
				Map<String,Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
						«FOR attr : c.attributes»
							.put("«attr.name»", «ExistenceChecker».isSet(o.get«attr.name?.toFirstUpper»()))
						«ENDFOR»
						.build();
				// Exclude given method name
				Map<String, Boolean> otherFieldsExistenceMap = fieldExistenceMap.entrySet().stream()
						.filter(x -> !x.getKey().equals(field))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				// Find any other fields that are set
				Set<String> setFields = otherFieldsExistenceMap.entrySet().stream()
						.filter(Map.Entry::getValue)
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
						.keySet();
		
				if (!setFields.isEmpty()) {
					return «ValidationResult.importMethod("failure")»("«c.name»", ValidationType.ONLY_EXISTS, o.getClass().getSimpleName(), path, "",
							String.format("[%s] is not the only field set. Other set fields: %s", field, setFields));
				}
				return «ValidationResult.importMethod("success")»("«c.name»", ValidationType.ONLY_EXISTS, o.getClass().getSimpleName(), path, "");
			}
		}
	'''

	private def StringConcatenationClient checkCardinality(ExpandedAttribute attr) '''
		«IF attr.isMultiple»
			«ValidatorHelper.importMethod("checkCardinality")»("«attr.name»", o.get«attr.name?.toFirstUpper»()==null?0:o.get«attr.name?.toFirstUpper»().size(), «attr.inf», «attr.sup»)
		«ELSE»
			«ValidatorHelper.importMethod("checkCardinality")»("«attr.name»", o.get«attr.name?.toFirstUpper»()!=null ? 1 : 0, «attr.inf», «attr.sup»)
		«ENDIF»
	'''
}
