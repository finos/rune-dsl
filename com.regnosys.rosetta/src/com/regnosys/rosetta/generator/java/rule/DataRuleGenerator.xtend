package com.regnosys.rosetta.generator.java.rule

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator.ParamMap
import com.regnosys.rosetta.generator.java.function.FunctionDependencyProvider
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.RosettaGrammarUtil
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.model.lib.annotations.RosettaDataRule
import com.rosetta.model.lib.expression.ComparisonResult
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ModelObjectValidator
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.validation.Validator
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.CONDITION__EXPRESSION

class DataRuleGenerator {
	@Inject ExpressionGenerator expressionHandler
	@Inject extension RosettaExtensions
	@Inject extension ImportManagerExtension
	@Inject FunctionDependencyProvider funcDependencies
	
	def generate(JavaNames names, IFileSystemAccess2 fsa, Data data, Condition ele, String version) {
		val classBody = tracImports(ele.dataRuleClassBody(data, names, version))
		val content = '''
			package «names.packages.model.dataRule.name»;
			
			«FOR imp : classBody.imports»
				import «imp»;
			«ENDFOR»
			«»
			«FOR imp : classBody.staticImports»
				import static «imp»;
			«ENDFOR»
			
			«classBody.toString»
		'''
		fsa.generateFile('''«names.packages.model.dataRule.directoryName»/«ele.conditionName(data).toConditionJavaType».java''', content)
	}

	private def StringConcatenationClient dataRuleClassBody(Condition rule, Data data, JavaNames javaName, String version)  {
		val rosettaClass = rule.eContainer as RosettaType
		
		val definition = RosettaGrammarUtil.quote(RosettaGrammarUtil.extractNodeText(rule, CONDITION__EXPRESSION))
		val ruleName = rule.conditionName(data)
		val funcDeps = funcDependencies.functionDependencies(rule.expression)
		'''
			«emptyJavadocWithVersion(version)»
			@«RosettaDataRule»("«ruleName»")
			public class «toConditionJavaType(ruleName)» implements «Validator»<«javaName.toJavaType(rosettaClass)»> {
				
				private static final String NAME = "«ruleName»";
				private static final String DEFINITION = «definition»;
				
				«FOR dep : funcDeps»
					@«Inject» protected «javaName.toJavaType(dep)» «dep.name.toFirstLower»;
				«ENDFOR»
				
				@Override
				public «ValidationResult»<«rosettaClass.name»> validate(«RosettaPath» path, «rosettaClass.name» «rosettaClass.name.toFirstLower») {
					«ComparisonResult» result = executeDataRule(«rosettaClass.name.toFirstLower»);
					if (result.get()) {
						return «ValidationResult».success(NAME, ValidationResult.ValidationType.DATA_RULE,  "«rosettaClass.name»", path, DEFINITION);
					}
					
					return «ValidationResult».failure(NAME, ValidationResult.ValidationType.DATA_RULE, "«rosettaClass.name»", path, DEFINITION, result.getError());
				}
				
				private ComparisonResult executeDataRule(«rosettaClass.name» «rosettaClass.name.toFirstLower») {
					
					try {
						«ComparisonResult» result = «expressionHandler.toComparisonResult(rule.expression, new ParamMap(rosettaClass))»;
						return result.get() == null ? ComparisonResult.success() : result;
					}
					catch («ModelObjectValidator».ModelObjectValidationException ex) {
						return ComparisonResult.failure(ex.getErrors());
					}
				}
			}
		'''
	}
}

