package com.regnosys.rosetta.generator.java.rule

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Necessity
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.rosetta.model.lib.annotations.RosettaChoiceRule
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ExistenceChecker
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.validation.ValidationResult.ChoiceRuleFailure
import com.rosetta.model.lib.validation.ValidationResult.ChoiceRuleValidationMethod
import com.rosetta.model.lib.validation.ValidationResult.ValidationType
import com.rosetta.model.lib.validation.Validator
import java.util.Arrays
import java.util.LinkedList
import java.util.List
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

class ChoiceRuleGenerator {
	
	@Inject extension RosettaExtensions
	@Inject extension ImportManagerExtension

	def generate(JavaNames names, IFileSystemAccess2 fsa, Data data, Condition cond, String version) {
		val classBody = tracImports(cond.toChoiceRuleJava(data, names, version))
		val fileContent = '''
			package «names.packages.model.choiceRule.name»;
			
			«FOR imp : classBody.imports»
				import «imp»;
			«ENDFOR»

			«FOR imp : classBody.staticImports»
				import static «imp»;
			«ENDFOR»
			
			«classBody.toString»
		'''
		fsa.generateFile('''«names.packages.model.choiceRule.directoryName»/«cond.conditionName(data).toConditionJavaType».java''', fileContent)
	}
	
	private def StringConcatenationClient toChoiceRuleJava(Condition rule, Data data, JavaNames names, String version) {
		val clazz = data.name
		val ruleName = rule.conditionName(data)
		val className = rule.conditionName(data).toConditionJavaType
		val usedAttributes = if(rule.constraint.isOneOf) data.allAttributes else rule.constraint.attributes // TODO multi choice rules? 
		val validationType = if(rule.constraint.isOneOf || rule.constraint.necessity === Necessity.REQUIRED) 'REQUIRED' else 'OPTIONAL'
		'''
		«emptyJavadocWithVersion(version)»
		@«RosettaChoiceRule»("«ruleName»")
		public class «className» implements «Validator»<«names.toJavaType(data)»> {
			
			private static final String NAME = "«ruleName»";
			
			@Override
			public «ValidationResult»<«clazz»> validate(«RosettaPath» path, «clazz» object) {
				«List»<String> choiceFieldNames = «importMethod(Arrays,"asList")»(«usedAttributes.join(', ')['"'+name+'"']»);
				List<String> populatedFieldNames = new «LinkedList»<>();
				«FOR a : usedAttributes»
					if («importMethod(ExistenceChecker,"isSet")»(object.get« a.name.toFirstUpper »())) populatedFieldNames.add("«a.name»");
				«ENDFOR»
				
				«ChoiceRuleValidationMethod» validationMethod = ChoiceRuleValidationMethod.«validationType»;
				
				if (validationMethod.check(populatedFieldNames.size())) {
					return «importMethod(ValidationResult,"success")»(NAME, «ValidationType».CHOICE_RULE, "«clazz»", path, "");
				}
				return new «ValidationResult».«ChoiceRuleFailure»<«clazz»>(NAME, "«clazz»", choiceFieldNames, path, populatedFieldNames, validationMethod);
			}
		
			@Override
			public «ValidationResult»<«clazz»> validate(«RosettaPath» path, «RosettaModelObjectBuilder» builder) {
				«clazz».«clazz»Builder object = («clazz».«clazz»Builder) builder;
				«List»<String> choiceFieldNames = «importMethod(Arrays,"asList")»(«usedAttributes.join(', ')['"'+name+'"']»);
				List<String> populatedFieldNames = new «LinkedList»<>();
				«FOR a : usedAttributes»
					if (isSet(object.get« a.name.toFirstUpper »())) populatedFieldNames.add("«a.name»");
				«ENDFOR»
				
				«ChoiceRuleValidationMethod» validationMethod = ChoiceRuleValidationMethod.«validationType»;
				
				if (validationMethod.check(populatedFieldNames.size())) {
					return «importMethod(ValidationResult,"success")»(NAME, «ValidationType».CHOICE_RULE, "«clazz»", path, "");
				}
				return new «ValidationResult».«ChoiceRuleFailure»<«clazz»>(NAME, "«clazz»", choiceFieldNames, path, populatedFieldNames, validationMethod);
			}
		}
	'''
	}

	def static String oneOfRuleClassName(String className) {
		return className + 'OneOfRule'
	}
	
	def static String oneOfRuleName(String className) {
		return className + '_oneOf'
	}
}

