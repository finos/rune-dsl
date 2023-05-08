package com.regnosys.rosetta.generator.java.rule

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.function.FunctionDependencyProvider
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.RosettaGrammarUtil
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.model.lib.annotations.RosettaDataRule
import com.rosetta.model.lib.expression.ComparisonResult
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.validation.Validator
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.CONDITION__EXPRESSION
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RDataType

class DataRuleGenerator {
	@Inject ExpressionGenerator expressionHandler
	@Inject extension RosettaExtensions
	@Inject extension ImportManagerExtension
	@Inject FunctionDependencyProvider funcDependencies
	@Inject extension JavaIdentifierRepresentationService
	@Inject extension JavaTypeTranslator
	
	def generate(RootPackage root, IFileSystemAccess2 fsa, Data data, Condition ele, String version) {
		val topScope = new JavaScope(root.dataRule)
		
		val classBody = ele.dataRuleClassBody(data, topScope, version)
		val content = buildClass(root.dataRule, classBody, topScope)
		fsa.generateFile('''«root.dataRule.withForwardSlashes»/«ele.conditionName(data).toConditionJavaType».java''', content)
	}

	private def StringConcatenationClient dataRuleClassBody(Condition rule, Data data, JavaScope scope, String version)  {
		val rosettaClass = rule.eContainer as Data
		val definition = RosettaGrammarUtil.quote(RosettaGrammarUtil.extractNodeText(rule, CONDITION__EXPRESSION))
		val ruleName = rule.conditionName(data)
		val funcDeps = funcDependencies.functionDependencies(rule.expression)
		val implicitVarRepr = rule.implicitVarInContext
		
		val classScope = scope.classScope(toConditionJavaType(ruleName))
		funcDeps.forEach[classScope.createIdentifier(it.toFunctionInstance, it.name.toFirstLower)]
		
		val validateScope = classScope.methodScope("validate")
		val pathId = validateScope.createUniqueIdentifier("path")
		val resultId = validateScope.createUniqueIdentifier("result")
		val failureMessageId = validateScope.createUniqueIdentifier("failureMessage")
		
		val executeScope = classScope.methodScope("execute")
		val executeResultId = executeScope.createUniqueIdentifier("result")
		val exceptionId = executeScope.createUniqueIdentifier("ex")
		'''
			«emptyJavadocWithVersion(version)»
			@«RosettaDataRule»("«ruleName»")
			public class «toConditionJavaType(ruleName)» implements «Validator»<«new RDataType(rosettaClass).toJavaType»> {
				
				private static final String NAME = "«ruleName»";
				private static final String DEFINITION = «definition»;
				
				«FOR dep : funcDeps»
					@«Inject» protected «dep.toFunctionJavaClass» «classScope.getIdentifierOrThrow(dep.toFunctionInstance)»;
				«ENDFOR»
				
				@Override
				public «ValidationResult»<«rosettaClass.name»> validate(«RosettaPath» «pathId», «rosettaClass.name» «validateScope.createIdentifier(implicitVarRepr, rosettaClass.name.toFirstLower)») {
					«ComparisonResult» «resultId» = executeDataRule(«validateScope.getIdentifierOrThrow(implicitVarRepr)»);
					if (result.get()) {
						return «ValidationResult».success(NAME, ValidationResult.ValidationType.DATA_RULE, "«rosettaClass.name»", «pathId», DEFINITION);
					}
					
					String «failureMessageId» = «resultId».getError();
					if («failureMessageId» == null) {
						«failureMessageId» = "Condition " + NAME + " failed.";
					}
					return «ValidationResult».failure(NAME, ValidationResult.ValidationType.DATA_RULE, "«rosettaClass.name»", «pathId», DEFINITION, «failureMessageId»);
				}
				
				private «ComparisonResult» executeDataRule(«rosettaClass.name» «executeScope.createIdentifier(implicitVarRepr, rosettaClass.name.toFirstLower)») {
					
					try {
						«ComparisonResult» «executeResultId» = «expressionHandler.toComparisonResult(rule.expression, executeScope)»;
						return «executeResultId».get() == null ? ComparisonResult.success() : «executeResultId»;
					}
					catch («Exception» «exceptionId») {
						return «ComparisonResult».failure(«exceptionId».getMessage());
					}
				}
			}
		'''
	}
}

