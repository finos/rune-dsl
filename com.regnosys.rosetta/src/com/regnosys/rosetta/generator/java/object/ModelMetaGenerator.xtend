package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.Sets
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.qualify.QualifyFunctionGenerator
import com.regnosys.rosetta.generator.java.rule.ChoiceRuleGenerator
import com.regnosys.rosetta.generator.java.rule.DataRuleGenerator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaCallable
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaEvent
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaProduct
import com.regnosys.rosetta.rosetta.RosettaRootElement
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
import java.util.List
import java.util.Set
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

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
				public «List»<«Validator»<? super «dataClass»>> dataRules() {
					return «Arrays».asList(
						«FOR r : conditionRules(c, c.conditions)[!isChoiceRuleCondition] SEPARATOR ','»
							new «javaNames.packages.model.dataRule.name».«DataRuleGenerator.dataRuleClassName(r.ruleName)»()
						«ENDFOR»
					);
				}
			
				@Override
				public «List»<«Validator»<? super «dataClass»>> choiceRuleValidators() {
					return Arrays.asList(
						«FOR r : conditionRules(c, c.conditions)[isChoiceRuleCondition] SEPARATOR ','»
							new «javaNames.packages.model.choiceRule.name».«ChoiceRuleGenerator.choiceRuleClassName(r.ruleName)»()
						«ENDFOR»
					);
				}

				@Override
				public «List»<«java.util.function.Function»<? super «dataClass», «QualifyResult»>> getQualifyFunctions() {
					return Arrays.asList(
						«FOR qf : qualifyFunctions(javaNames.packages, c.model.elements, c) SEPARATOR ','»
							new «qf.javaPackage».«qf.functionName»()
						«ENDFOR»
					);
				}
				«IF !qualifierFuncs.nullOrEmpty»
				
				@Override
				public «List»<«java.util.function.Function»<? super «dataClass», «QualifyResult»>> getQualifyFunctions(«QualifyFunctionFactory» factory) {
					return Arrays.asList(
						«FOR qf : qualifierFuncs SEPARATOR ','»
							factory.create(«javaNames.toJavaType(qf)».class)
						«ENDFOR»
					);
				}
				«ENDIF»
				
				@Override
				public «Validator»<? super «dataClass»> validator() {
					return new «javaNames.packages.model.typeValidation.name».«dataClass»Validator();
				}
				
				@Override
				public «ValidatorWithArg»<? super «dataClass», String> onlyExistsValidator() {
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

	
	private def List<QualifyFunction> qualifyFunctions(RosettaJavaPackages packages, List<RosettaRootElement> elements,
		RosettaCallable thisClass) {
		val allQualifyFns = Sets.newLinkedHashSet
		val superClasses 
			= if(thisClass instanceof Data)
				thisClass.allSuperTypes.map[name].toList
				
		// TODO create public constant with list of qualifiable classes / packages
		allQualifyFns.addAll(
			getQualifyFunctionsForRosettaClass(RosettaEvent, packages.model.qualifyEvent.name, elements))
		allQualifyFns.addAll(
			getQualifyFunctionsForRosettaClass(RosettaProduct, packages.model.qualifyProduct.name, elements))
		return allQualifyFns.filter[superClasses.contains(it.className)].toList
	}

	private def <T extends RosettaRootElement & RosettaNamed> getQualifyFunctionsForRosettaClass(Class<T> clazz,
		String javaPackage, List<RosettaRootElement> elements) {
		val qualifyFns = Sets.newLinkedHashSet
		elements.filter(clazz).forEach [
			val rosettaClass = getRosettaClass(it)
			val functionName = QualifyFunctionGenerator.getIsFunctionClassName(it.name)
			rosettaClass.ifPresent [
				qualifyFns.add(new QualifyFunction(it.name, javaPackage, functionName))
			]
		]
		return qualifyFns
	}

	private def getRosettaClass(RosettaRootElement element) {
		val rosettaClasses = newHashSet
		element.eAllContents.filter(RosettaCallableCall).forEach [
			collectRootCalls(it, [if(it instanceof Data) rosettaClasses.add(it)])
		]
		return rosettaClasses.stream.findAny
	}

	@org.eclipse.xtend.lib.annotations.Data
	static class QualifyFunction {
		String className;
		String javaPackage;
		String functionName;
	}
}
