package com.regnosys.rosetta.generator.java.rule

import com.google.common.base.CaseFormat
import com.google.common.collect.ImmutableSet
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.rosetta.RosettaChoiceRule
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaRootElement
import java.util.List
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import static com.rosetta.model.lib.validation.ValidationResult.ChoiceRuleValidationMethod.REQUIRED

class ChoiceRuleGenerator {
	
	@Inject extension RosettaExtensions
	
	def generate(RosettaJavaPackages packages, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version) {
		elements.filter(RosettaChoiceRule).forEach [
			fsa.generateFile('''«packages.choiceRule.directoryName»/«choiceRuleClassName(name)».java''', toChoiceRuleJava(packages, version))
		]
		elements.filter(RosettaClass).filter[oneOf].forEach [
			fsa.generateFile('''«packages.choiceRule.directoryName»/«oneOfRuleClassName(it.name)».java''', toOneOfRuleJava(packages, version))
		]
	}

	private def toChoiceRuleJava(RosettaChoiceRule rule, RosettaJavaPackages packages, String version) {
		val choiceAttributeNames = ImmutableSet.builder.add(rule.thisOne).addAll(rule.thatOnes).build.map[name].toList
		toJava(packages, choiceRuleClassName(rule.name), rule.name, rule.qualifier, rule.scope.name, choiceAttributeNames, version)
	}

	def static String choiceRuleClassName(String choiceRuleName) {
		val allUnderscore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, choiceRuleName)
		val camel = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, allUnderscore)
		if (camel.endsWith('Choice'))
			return camel +'Rule'
		if (camel.endsWith('ChoiceRule'))
			return camel
		return camel + 'ChoiceRule'
	}

	private def toOneOfRuleJava(RosettaClass clazz, RosettaJavaPackages packages, String version) {
		val classAttributeNames = clazz.allAttributes.map[name].toList 
		toJava(packages, oneOfRuleClassName(clazz.name), oneOfRuleName(clazz.name), REQUIRED.name, clazz.name, classAttributeNames, version)
	}

	def static String oneOfRuleClassName(String className) {
		return className + 'OneOfRule'
	}
	
	def static String oneOfRuleName(String className) {
		return className + '_oneOf'
	}

	private def toJava(RosettaJavaPackages packages, String className, String ruleName, String qualifier, String clazz, List<String> attributes, String version) '''
		package «packages.choiceRule.packageName»;
		
		import «packages.model.packageName».«clazz»;
		import «packages.validation.packageName».ValidationResult.ChoiceRuleValidationMethod;
		import «packages.validation.packageName».ValidationResult;
		import «packages.validation.packageName».Validator;
		import java.util.Arrays;
		import java.util.List;
		import java.util.LinkedList;
		
		import «packages.lib.packageName».RosettaModelObjectBuilder;
		import «packages.annotations.packageName».RosettaChoiceRule;
		import «packages.lib.packageName».path.RosettaPath;
					
		import static «packages.validation.packageName».ExistenceChecker.isSet;
		
		«emptyJavadocWithVersion(version)»
		@RosettaChoiceRule("«ruleName»")
		public class «className» implements Validator<«clazz»> {
			
			private static final String NAME = "«ruleName»";
			
		    @Override
		    public ValidationResult<«clazz»> validate(RosettaPath path, «clazz» object) {
				List<String> choiceFieldNames = Arrays.asList(«attributes.join('"', '", "', '"', [it])»);
				List<String> populatedFieldNames = new LinkedList<>();
				«FOR a : attributes»
					if (isSet(object.get« a.toFirstUpper »())) populatedFieldNames.add("«a»");
				«ENDFOR»
				
				ChoiceRuleValidationMethod validationMethod = ChoiceRuleValidationMethod.«qualifier.toUpperCase»;
				
				if (validationMethod.check(populatedFieldNames.size())) {
					return ValidationResult.success(NAME, ValidationResult.ValidationType.CHOICE_RULE, "«clazz»", path, "");
				}
				return new ValidationResult.ChoiceRuleFailure<«clazz»>(NAME, "«clazz»", choiceFieldNames, path, populatedFieldNames, validationMethod);
		    }
		    
		    @Override
		    public ValidationResult<«clazz»> validate(RosettaPath path, RosettaModelObjectBuilder builder) {
		    	«clazz».«clazz»Builder object = («clazz».«clazz»Builder) builder;
				List<String> choiceFieldNames = Arrays.asList(«attributes.join('"', '", "', '"', [it])»);
				List<String> populatedFieldNames = new LinkedList<>();
				«FOR a : attributes»
					if (isSet(object.get« a.toFirstUpper »())) populatedFieldNames.add("«a»");
				«ENDFOR»
				
				ChoiceRuleValidationMethod validationMethod = ChoiceRuleValidationMethod.«qualifier.toUpperCase»;
				
				if (validationMethod.check(populatedFieldNames.size())) {
					return ValidationResult.success(NAME, ValidationResult.ValidationType.CHOICE_RULE, "«clazz»", path, "");
				}
				return new ValidationResult.ChoiceRuleFailure<«clazz»>(NAME, "«clazz»", choiceFieldNames, path, populatedFieldNames, validationMethod);
		    }
		}
	'''
	
}

