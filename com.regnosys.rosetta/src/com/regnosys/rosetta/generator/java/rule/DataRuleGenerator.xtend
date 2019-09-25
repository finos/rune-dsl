package com.regnosys.rosetta.generator.java.rule

import com.google.common.base.CaseFormat
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.expression.RosettaExpressionJavaGenerator
import com.regnosys.rosetta.generator.java.expression.RosettaExpressionJavaGeneratorForFunctions
import com.regnosys.rosetta.generator.java.expression.RosettaExpressionJavaGeneratorForFunctions.ParamMap
import com.regnosys.rosetta.generator.java.util.ImportGenerator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.RosettaGrammarUtil
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaDataRule
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Condition
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ComparisonResult
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.validation.ValidationResult.ValidationType
import com.rosetta.model.lib.validation.Validator
import java.util.List
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.CONDITION__EXPRESSION

class DataRuleGenerator {
	@Inject RosettaExpressionJavaGeneratorForFunctions expressionHandler
	@Inject extension ImportManagerExtension
	
	def generate(RosettaJavaPackages packages, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version) {
		elements.filter(RosettaDataRule).forEach [
			fsa.generateFile('''«packages.dataRule.directoryName»/«dataRuleClassName(name)».java''', toJava(packages, version))
		]
	}
	
	def generate(JavaNames names, IFileSystemAccess2 fsa, Condition ele, String version) {
		val classBody = tracImports(ele.dataRuleClassBody(names, version))
		val content = '''
			package «names.packages.dataRule.packageName»;
			
			«FOR imp : classBody.imports»
				import «imp»;
			«ENDFOR»
			«»
			«FOR imp : classBody.staticImports»
				import static «imp»;
			«ENDFOR»
			
			«classBody.toString»
		'''
		fsa.generateFile('''«names.packages.dataRule.directoryName»/«dataRuleClassName(ele.name)».java''', content)
	}

	def static String dataRuleClassName(String dataRuleName) {
		val allUnderscore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, dataRuleName)
		val camel = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, allUnderscore)
		if (camel.endsWith('Data'))
			return camel +'Rule'
		if (camel.endsWith('DataRule'))
			return camel
		if (camel.endsWith('Rule'))
			return camel.substring(0, camel.lastIndexOf('Rule')) + 'DataRule'
		return camel + 'DataRule'
	}

	private def toJava(RosettaDataRule rule, RosettaJavaPackages packages, String version) {
		val rosettaClass = rosettaClassForRule(rule)
		val expressionHandler = new RosettaExpressionJavaGenerator()
		toJava(packages, rule, rosettaClass, expressionHandler, version)
	}
	
	private def static rosettaClassForRule(RosettaDataRule rule) {
		val rosettaClasses = newHashSet
		val extensions = new RosettaExtensions
		rule.when.eAllContents.filter(RosettaCallableCall).forEach[
			extensions.collectRootCalls(it, [if(it instanceof RosettaClass) rosettaClasses.add(it)])
		]
		if (rosettaClasses.size > 1) {
			throw new IllegalStateException(rule.name + ' compile failed. Found more then one class reference ' + rosettaClasses.map[name] + ' for this rule ' + rule.name)
		}
		if (rosettaClasses.size < 1) {
			throw new IllegalStateException(rule.name + ' compile failed. Found any class reference ' + rosettaClasses.map[name] + ' for this rule ' + rule.name)
		}
		
		return rosettaClasses.get(0)
	}
	private def StringConcatenationClient dataRuleClassBody(Condition rule, JavaNames javaName, String version)  {
		val rosettaClass = rule.eContainer as RosettaType
		val expression = rule.expression // TODO allow only one conditional expression here
		
		val ruleWhen = if(expression instanceof RosettaConditionalExpression ) expression.^if
		val ruleThen = if(expression instanceof RosettaConditionalExpression ) expression.ifthen
		
		val definition = RosettaGrammarUtil.quote(RosettaGrammarUtil.extractNodeText(rule, CONDITION__EXPRESSION));
		'''
			«emptyJavadocWithVersion(version)»
			@«com.rosetta.model.lib.annotations.RosettaDataRule»("«rule.name»")
			public class «dataRuleClassName(rule.name)» implements «Validator»<«javaName.toJavaType(rosettaClass)»> {
				
				private static final String NAME = "«rule.name»";
				private static final String DEFINITION = «definition»;
				
				@Override
				public «ValidationResult»<«rosettaClass.name»> validate(«RosettaPath» path, «rosettaClass.name» «rosettaClass.name.toFirstLower») {
					«ComparisonResult» result = executeDataRule(«rosettaClass.name.toFirstLower»);
					if (result.get()) {
						return «ValidationResult».success(NAME, ValidationResult.ValidationType.DATA_RULE,  "«rosettaClass.name»", path, DEFINITION);
					}
					
					return «ValidationResult».failure(NAME, ValidationResult.ValidationType.DATA_RULE, "«rosettaClass.name»", path, DEFINITION, result.getError());
				}
				
				@Override
				public «ValidationResult»<«rosettaClass.name»> validate(RosettaPath path, «RosettaModelObjectBuilder» «rosettaClass.name.toFirstLower») {
					«ComparisonResult» result = executeDataRule((«rosettaClass.name»)«rosettaClass.name.toFirstLower».build());
					if (result.get()) {
						return ValidationResult.success(NAME, «ValidationType».DATA_RULE, "«rosettaClass.name»", path, DEFINITION);
					}
					
					return ValidationResult.failure(NAME, «ValidationType».DATA_RULE,  "«rosettaClass.name»", path, DEFINITION, result.getError());
				}
				
				private ComparisonResult executeDataRule(«rosettaClass.name» «rosettaClass.name.toFirstLower») {
					if (ruleIsApplicable(«rosettaClass.name.toFirstLower»).get()) {
						return evaluateThenExpression(«rosettaClass.name.toFirstLower»);
					}
					return ComparisonResult.success();
				}
				
				private ComparisonResult ruleIsApplicable(«rosettaClass.name» «rosettaClass.name.toFirstLower») {
					return «expressionHandler.javaCode(ruleWhen, new ParamMap(rosettaClass))»;
				}
				
				private ComparisonResult evaluateThenExpression(«rosettaClass.name» «rosettaClass.name.toFirstLower») {
					return «expressionHandler.javaCode(ruleThen, new ParamMap(rosettaClass))»;
				}
			}
		'''
	}
	private def toJava(RosettaJavaPackages packages, RosettaDataRule rule, RosettaClass rosettaClass, RosettaExpressionJavaGenerator expressionHandler, String version)  {
	val definition = RosettaGrammarUtil.quote(RosettaGrammarUtil.grammarWhenThen(rule.when, rule.then) );
	val imports = new ImportGenerator(packages)
	imports.addRule(rule)
	return '''
		package «packages.dataRule.packageName»;
		
		«FOR importClass : imports.imports.filter[imports.isImportable(it)]»
		import «importClass»;
		«ENDFOR»
		
		«FOR importClass : imports.staticImports»
		import static «importClass».*;
		«ENDFOR»

		«emptyJavadocWithVersion(version)»
		@RosettaDataRule("«rule.name»")
		public class «dataRuleClassName(rule.name)» implements Validator<«rosettaClass.name»> {
			
			private static final String NAME = "«rule.name»";
			private static final String DEFINITION = «definition»;
			
			@Override
			public ValidationResult<«rosettaClass.name»> validate(RosettaPath path, «rosettaClass.name» «rosettaClass.name.toFirstLower») {
				ComparisonResult result = executeDataRule(«rosettaClass.name.toFirstLower»);
				if (result.get()) {
					return ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE,  "«rosettaClass.name»", path, DEFINITION);
				}
				
				return ValidationResult.failure(NAME, ValidationResult.ValidationType.DATA_RULE, "«rosettaClass.name»", path, DEFINITION, result.getError());
			}
			
			@Override
			public ValidationResult<«rosettaClass.name»> validate(RosettaPath path, RosettaModelObjectBuilder «rosettaClass.name.toFirstLower») {
				ComparisonResult result = executeDataRule((«rosettaClass.name»)«rosettaClass.name.toFirstLower».build());
				if (result.get()) {
					return ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "«rosettaClass.name»", path, DEFINITION);
				}
				
				return ValidationResult.failure(NAME, ValidationResult.ValidationType.DATA_RULE,  "«rosettaClass.name»", path, DEFINITION, result.getError());
			}
			
			private ComparisonResult executeDataRule(«rosettaClass.name» «rosettaClass.name.toFirstLower») {
				if (ruleIsApplicable(«rosettaClass.name.toFirstLower»).get()) {
					return evaluateThenExpression(«rosettaClass.name.toFirstLower»);
				}
				return ComparisonResult.success();
			}
			
			private ComparisonResult ruleIsApplicable(«rosettaClass.name» «rosettaClass.name.toFirstLower») {
				return «expressionHandler.javaCode(rule.when, new RosettaExpressionJavaGenerator.ParamMap(rosettaClass))»;
			}
			
			private ComparisonResult evaluateThenExpression(«rosettaClass.name» «rosettaClass.name.toFirstLower») {
				return «expressionHandler.javaCode(rule.then, new RosettaExpressionJavaGenerator.ParamMap(rosettaClass))»;
			}
		}
	'''
	}
}

