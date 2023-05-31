package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.utils.RosettaConfigExtension
import com.rosetta.model.lib.annotations.RosettaMeta
import com.rosetta.model.lib.meta.RosettaMetaData
import com.rosetta.model.lib.qualify.QualifyFunctionFactory
import com.rosetta.model.lib.qualify.QualifyResult
import com.rosetta.model.lib.validation.Validator
import com.rosetta.model.lib.validation.ValidatorWithArg
import java.util.Arrays
import java.util.Collections
import java.util.List
import java.util.Set
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import com.rosetta.model.lib.validation.ValidatorFactory
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RDataType

class ModelMetaGenerator {

	@Inject extension ImportManagerExtension
	@Inject extension RosettaExtensions
	@Inject RosettaConfigExtension confExt
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension JavaTypeTranslator
	
	def generate(RootPackage root, IFileSystemAccess2 fsa, Data data, String version) {
		val className = '''«data.name»Meta'''
		
		val scope = new JavaScope(root.meta)
		
		val classBody = data.metaClassBody(root, className, version)
		val javaFileContents = buildClass(root.meta, classBody, scope)
		fsa.generateFile('''«root.meta.withForwardSlashes»/«className».java''', javaFileContents)
	}
	
	private def StringConcatenationClient metaClassBody(Data c, RootPackage root, String className, String version) {
		val t = new RDataType(c)
		val dataClass = t.toJavaType
		val validator = t.toValidatorClass
		val typeFormatValidator = t.toTypeFormatValidatorClass
		val onlyExistsValidator = t.toOnlyExistsValidatorClass
		val context = c.eResource.resourceSet
		val qualifierFuncs = qualifyFuncs(c, context.resources.map[contents.head as RosettaModel].toSet)
		val dataRules = c.allSuperTypes.map[it.conditionRules(it.conditions)].flatten
		'''
			«emptyJavadocWithVersion(version)»
			@«RosettaMeta»(model=«dataClass».class)
			public class «className» implements «RosettaMetaData»<«dataClass»> {
			
				@Override
				public «List»<«Validator»<? super «dataClass»>> dataRules(«ValidatorFactory» factory) {
					return «Arrays».asList(
						«FOR r : dataRules SEPARATOR ','»
							factory.create(«r.containingClassNamespace.dataRule».«r.ruleName.toConditionJavaType».class)
						«ENDFOR»
					);
				}
				
				@Override
				public «List»<«java.util.function.Function»<? super «dataClass», «QualifyResult»>> getQualifyFunctions(«QualifyFunctionFactory» factory) {
					«IF !qualifierFuncs.nullOrEmpty»
					return Arrays.asList(
						«FOR qf : qualifierFuncs SEPARATOR ','»
							factory.create(«qf.toFunctionJavaClass».class)
						«ENDFOR»
					);
					«ELSE»
					return «Collections».emptyList();
					«ENDIF»
				}
				
				@Override
				public «Validator»<? super «dataClass»> validator() {
					return new «validator»();
				}
				
				@Override
				public «Validator»<? super «dataClass»> typeFormatValidator() {
					return new «typeFormatValidator»();
				}
				
				@Override
				public «ValidatorWithArg»<? super «dataClass», «Set»<String>> onlyExistsValidator() {
					return new «onlyExistsValidator»();
				}
			}
		'''
	}
	
	private def Set<Function> qualifyFuncs(Data type, Set<RosettaModel> models) {
		// TODO: make sure this method doesn't need to go through all models in the resource set
		if(!confExt.isRootEventOrProduct(type)) {
			return emptySet
		}
		val funcs = models.flatMap[elements].filter(Function).toSet
		return funcs.filter[funcExt.isQualifierFunctionFor(it,type)].toSet
	}
	
	private def List<ClassRule> conditionRules(Data d, List<Condition> elements) {
		val dataNamespace = new RootPackage(d.model)
		return elements.map[new ClassRule((it.eContainer as RosettaNamed).getName, it.conditionName(d), dataNamespace)].toList
	}

	@org.eclipse.xtend.lib.annotations.Data
	static class ClassRule {
		String className
		String ruleName
		RootPackage containingClassNamespace
	}
}
