package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.Sets
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.expression.RosettaExpressionJavaGenerator
import com.regnosys.rosetta.generator.java.qualify.QualifyFunctionGenerator
import com.regnosys.rosetta.generator.java.rule.ChoiceRuleGenerator
import com.regnosys.rosetta.generator.java.rule.DataRuleGenerator
import com.regnosys.rosetta.generator.java.util.ImportGenerator
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaChoiceRule
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaDataRule
import com.regnosys.rosetta.rosetta.RosettaEvent
import com.regnosys.rosetta.rosetta.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaProduct
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaRootElement
import java.util.List
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.cardinalityIsListValue

class ModelMetaGenerator {

	@Inject extension RosettaExpressionJavaGenerator
	@Inject extension RosettaExtensions
	
	def generate(RosettaJavaPackages packages, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version) {
		elements.filter(RosettaClass).forEach [ RosettaClass rosettaClass |
			val className = '''«rosettaClass.name»Meta'''
			fsa.generateFile('''«packages.meta.directoryName»/«className».java''',
				metaClass(packages, className, rosettaClass, elements, version))
		]
	}

	private def metaClass(RosettaJavaPackages packages, String className, RosettaClass c,
		List<RosettaRootElement> elements, String version) {
		val imports = new ImportGenerator(packages)
		imports.addMeta(c)

		'''
			package «packages.meta.packageName»;
			
			«FOR importClass : imports.imports.filter[imports.isImportable(it)]»
				import «importClass»;
			«ENDFOR»
			
			«FOR importClass : imports.staticImports»
				import static «importClass».*;
			«ENDFOR»
			
			«emptyJavadocWithVersion(version)»
			@RosettaMeta(model=«c.name».class)
			public class «className» implements RosettaMetaData<«c.name»> {
			
				@Override
				public List<Validator<? super «c.name»>> dataRules() {
					return Arrays.asList(
						«FOR r : dataRules(elements, c) SEPARATOR ','»
							new «packages.dataRule.packageName».«DataRuleGenerator.dataRuleClassName(r.ruleName)»()
						«ENDFOR»
					);
				}
			
				@Override
				public List<Validator<? super «c.name»>> choiceRuleValidators() {
					return Arrays.asList(
						«IF c.oneOf»
							new «packages.choiceRule.packageName».«ChoiceRuleGenerator.oneOfRuleClassName(c.name)»()
						«ENDIF»
						«FOR r : choiceRules(elements, c) SEPARATOR ','»
							new «packages.choiceRule.packageName».«ChoiceRuleGenerator.choiceRuleClassName(r.ruleName)»()
						«ENDFOR»
					);
				}

				@Override
				public List<Function<? super «c.name», QualifyResult>> getQualifyFunctions() {
					return Arrays.asList(
						«FOR qf : qualifyFunctions(packages, elements, c) SEPARATOR ','»
							new «qf.javaPackage».«qf.functionName»()
						«ENDFOR»
					);
				}
				
				@Override
				public Validator<? super «c.name»> validator() {
					return new «packages.classValidation.packageName».«c.name»Validator();
				}
				
				@Override
				public ValidatorWithArg<? super «c.name», String> onlyExistsValidator() {
					return new «packages.existsValidation.packageName».«ModelObjectGenerator.onlyExistsValidatorName(c)»();
				}
			}
		'''
	}

	def isField(RosettaExpression expression) {
		if(expression === null) return false
		val feat = expression as RosettaFeatureCall
		!(feat.feature.type instanceof RosettaClass)
	}

	def isList(RosettaExpression call) {
		switch (call) {
			RosettaCallableCall:
				return false
			RosettaFeatureCall: {
				return (call.feature as RosettaRegularAttribute).cardinalityIsListValue
			}
		}
	}

	def CharSequence getCreate(RosettaExpression call) {
		switch (call) {
			RosettaCallableCall:
				return ""
			RosettaFeatureCall: {
				val list = isList(call)
				return '''«getCreate(call.receiver)».getOrCreate«call.feature.name.toFirstUpper»(«IF list»i«ENDIF»)'''
			}
		}
	}

	def CharSequence getOutStartClass(RosettaExpression expr) {
		switch (expr) {
			RosettaFeatureCall:
				getOutStartClass(expr.receiver)
			RosettaCallableCall: {
				val callable = expr.callable as RosettaClass;
				return callable.name
			}
		}
	}

	def CharSequence getOutEndClass(RosettaExpression expr) {
		switch (expr) {
			RosettaFeatureCall:
				expr.feature.type.name
			RosettaCallableCall: {
				val callable = expr.callable as RosettaClass;
				return callable.name
			}
		}
	}

	protected def static List<ClassRule> choiceRules(List<RosettaRootElement> elements, RosettaClass thisClass) {
		val choiceRules = elements.filter(RosettaChoiceRule)
		val classRules = choiceRules.map[new ClassRule(scope.name, name)]
		return classRules.filter[it.className === thisClass.name].toList
	}
	
	protected def static List<ClassRule> dataRules(List<RosettaRootElement> elements, RosettaClass thisClass) {
		val dataRuleMappingSet = Sets.newLinkedHashSet
		elements.filter(RosettaDataRule).forEach [ dataRule |
			val dataRuleWhen = dataRule.when
			// TODO we need some kind of expansion for alias calls here
			val rosettaClasses = dataRuleWhen.eAllContents.filter(RosettaCallableCall).map[callable].filter(
				RosettaClass)
			val classRules = rosettaClasses.map[new ClassRule(name, dataRule.name)]
			dataRuleMappingSet.addAll(classRules.toSet)
		]
		return dataRuleMappingSet.filter[it.className === thisClass.name].toList
	}

	@Data
	static class ClassRule {
		String className;
		String ruleName;
	}

	private def List<QualifyFunction> qualifyFunctions(RosettaJavaPackages packages, List<RosettaRootElement> elements,
		RosettaClass thisClass) {
		val allQualifyFns = Sets.newLinkedHashSet
		// TODO create public constant with list of qualifiable classes / packages
		allQualifyFns.addAll(
			getQualifyFunctionsForRosettaClass(RosettaEvent, packages.qualifyEvent.packageName, elements))
		allQualifyFns.addAll(
			getQualifyFunctionsForRosettaClass(RosettaProduct, packages.qualifyProduct.packageName, elements))
		return allQualifyFns.filter[thisClass.allSuperTypes.map[name].toList.contains(it.className)].toList
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
		val extensions = new RosettaExtensions
		element.eAllContents.filter(RosettaCallableCall).forEach [
			extensions.collectRootCalls(it, [if(it instanceof RosettaClass) rosettaClasses.add(it)])
		]
		return rosettaClasses.stream.findAny
	}

	@Data
	static class QualifyFunction {
		String className;
		String javaPackage;
		String functionName;
	}
}
