package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
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

class ModelMetaGenerator {

	@Inject extension ImportManagerExtension
	@Inject extension RosettaExtensions
	@Inject RosettaConfigExtension confExt
	@Inject RosettaFunctionExtensions funcExt
	
	def generate(JavaNames names, IFileSystemAccess2 fsa, Data data, String version, Set<RosettaModel> models) {
		val className = '''«data.name»Meta'''
		
		val classBody = tracImports(data.metaClassBody(names, className, version, models))
		val javaFileContents = '''
			package «names.packages.model.meta.name»;
			
			«FOR imp : classBody.imports»
				import «imp»;
			«ENDFOR»
			«FOR imp : classBody.staticImports»
				import static «imp»;
			«ENDFOR»
			
			«classBody.toString»
		'''
		fsa.generateFile('''«names.packages.model.meta.directoryName»/«className».java''', javaFileContents)
	}
	
	private def StringConcatenationClient metaClassBody(Data c, JavaNames javaNames, String className, String version, Set<RosettaModel> models) {
		val dataClass = javaNames.toJavaType(c)
		val qualifierFuncs = qualifyFuncs(c, javaNames, models)
		'''
			«emptyJavadocWithVersion(version)»
			@«RosettaMeta»(model=«dataClass».class)
			public class «className» implements «RosettaMetaData»<«dataClass»> {
			
				@Override
				public «List»<«Validator»<? super «dataClass»>> dataRules(«ValidatorFactory» factory) {
					return «Arrays».asList(
						«FOR r : conditionRules(c, c.conditions)[!isChoiceRuleCondition] SEPARATOR ','»
							factory.create(«javaNames.packages.model.dataRule.name».«r.ruleName.toConditionJavaType».class)
						«ENDFOR»
					);
				}
			
				@Override
				public «List»<«Validator»<? super «dataClass»>> choiceRuleValidators() {
					return Arrays.asList(
						«FOR r : conditionRules(c, c.conditions)[isChoiceRuleCondition] SEPARATOR ','»
							new «javaNames.packages.model.choiceRule.name».«r.ruleName.toConditionJavaType»()
						«ENDFOR»
					);
				}
				
				@Override
				public «List»<«java.util.function.Function»<? super «dataClass», «QualifyResult»>> getQualifyFunctions(«QualifyFunctionFactory» factory) {
					«IF !qualifierFuncs.nullOrEmpty»
					return Arrays.asList(
						«FOR qf : qualifierFuncs SEPARATOR ','»
							factory.create(«javaNames.toJavaType(qf)».class)
						«ENDFOR»
					);
					«ELSE»
					return «Collections».emptyList();
					«ENDIF»
				}
				
				@Override
				public «Validator»<? super «dataClass»> validator() {
					return new «javaNames.packages.model.typeValidation.name».«dataClass»Validator();
				}
				
				@Override
				public «ValidatorWithArg»<? super «dataClass», «Set»<String>> onlyExistsValidator() {
					return new «javaNames.packages.model.existsValidation.name».«ValidatorsGenerator.onlyExistsValidatorName(c)»();
				}
			}
		'''
	}
	
	private def Set<Function> qualifyFuncs(Data type, JavaNames names, Set<RosettaModel> models) {
		if(!confExt.isRootEventOrProduct(type)) {
			return emptySet
		}
		val funcs = models.flatMap[elements].filter(Function).toSet
		return funcs.filter[funcExt.isQualifierFunctionFor(it,type)].toSet
	}
	
	private def List<ClassRule> conditionRules(Data d, List<Condition> elements, (Condition)=>boolean filter) {
		return elements.filter(filter).map[new ClassRule((it.eContainer as RosettaNamed).getName, it.conditionName(d))].toList
	}

	@org.eclipse.xtend.lib.annotations.Data
	static class ClassRule {
		String className;
		String ruleName;
	}
}
