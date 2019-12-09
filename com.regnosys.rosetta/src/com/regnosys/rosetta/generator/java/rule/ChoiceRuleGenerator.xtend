package com.regnosys.rosetta.generator.java.rule

import com.google.common.base.CaseFormat
import com.google.common.collect.ImmutableSet
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.rosetta.RosettaChoiceRule
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.validation.ValidationResult.ChoiceRuleFailure
import com.rosetta.model.lib.validation.ValidationResult.ChoiceRuleValidationMethod
import com.rosetta.model.lib.validation.ValidationResult.ValidationType
import com.rosetta.model.lib.validation.Validator
import java.util.Arrays
import java.util.List
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import static com.rosetta.model.lib.validation.ValidationResult.ChoiceRuleValidationMethod.REQUIRED
import com.rosetta.model.lib.validation.ExistenceChecker
import java.util.LinkedList
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.regnosys.rosetta.rosetta.simple.Necessity

class ChoiceRuleGenerator {
	
	@Inject extension RosettaExtensions
	@Inject extension ImportManagerExtension

	def generate(RosettaJavaPackages packages, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version) {
		elements.filter(RosettaChoiceRule).forEach [
			fsa.generateFile('''«packages.model.choiceRule.directoryName»/«choiceRuleClassName(name)».java''', toChoiceRuleJava(packages, version))
		]
		elements.filter(RosettaClass).filter[oneOf].forEach [
			fsa.generateFile('''«packages.model.choiceRule.directoryName»/«oneOfRuleClassName(it.name)».java''', toOneOfRuleJava(packages, version))
		]
	}
	
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
		fsa.generateFile('''«names.packages.model.choiceRule.directoryName»/«choiceRuleClassName(cond.conditionName(data))».java''', fileContent)
	}
	
	private def StringConcatenationClient toChoiceRuleJava(Condition rule, Data data, JavaNames names, String version) {
		val clazz = data.name
		val ruleName = rule.conditionName(data)
		val className = choiceRuleClassName(rule.conditionName(data))
		val usedAttributes = if(rule.constraint.isOneOf) data.allAttributes else rule.constraint.attributes // TODO multi choice rules? 
		val validationType = if(rule.constraint.isOneOf || rule.constraint.necessity === Necessity.REQUIRED) 'REQUIRED' else 'OPTIONAL'
		'''
		«emptyJavadocWithVersion(version)»
		@«com.rosetta.model.lib.annotations.RosettaChoiceRule»("«ruleName»")
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
	
	
	private def toChoiceRuleJava(RosettaChoiceRule rule, RosettaJavaPackages packages, String version) {
		val choiceAttributeNames = ImmutableSet.builder.add(rule.thisOne).addAll(rule.thatOnes).build.map[name].toList
		toJava(packages, choiceRuleClassName(rule.name), rule.name, rule.qualifier, rule.scope.name, choiceAttributeNames, version)
	}

	def static String choiceRuleClassName(String choiceRuleName) {
		val allUnderscore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, choiceRuleName)
		val camel = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, allUnderscore)
		return camel
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
		package «packages.model.choiceRule.name»;
		
		import «packages.model.name».«clazz»;
		import «packages.defaultLibValidation.name».ValidationResult.ChoiceRuleValidationMethod;
		import «packages.defaultLibValidation.name».ValidationResult;
		import «packages.defaultLibValidation.name».Validator;
		import java.util.Arrays;
		import java.util.List;
		import java.util.LinkedList;
		
		import «packages.defaultLib.name».RosettaModelObjectBuilder;
		import «packages.defaultLibAnnotations.name».RosettaChoiceRule;
		import «packages.defaultLib.name».path.RosettaPath;
					
		import static «packages.defaultLibValidation.name».ExistenceChecker.isSet;
		
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

