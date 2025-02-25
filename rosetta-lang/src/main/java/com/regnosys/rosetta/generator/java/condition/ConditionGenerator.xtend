package com.regnosys.rosetta.generator.java.condition

import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.RosettaGrammarUtil
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.types.RDataType
import com.rosetta.model.lib.annotations.RosettaDataRule
import com.rosetta.model.lib.expression.ComparisonResult
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.validation.Validator
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.CONDITION__EXPRESSION
import jakarta.inject.Inject
import com.google.inject.ImplementedBy
import com.rosetta.model.lib.validation.ValidationResult.ValidationType
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.expression.JavaDependencyProvider
import com.regnosys.rosetta.RosettaEcoreUtil

class ConditionGenerator {
	@Inject ExpressionGenerator expressionHandler
	@Inject extension RosettaEcoreUtil
	@Inject extension ImportManagerExtension
	@Inject JavaDependencyProvider dependencies
	@Inject extension JavaIdentifierRepresentationService
	@Inject extension JavaTypeTranslator
	@Inject extension JavaTypeUtil
	
	def generate(RootPackage root, IFileSystemAccess2 fsa, RDataType t, Condition ele, String version) {
		val topScope = new JavaScope(root.condition)
		
		val classBody = ele.conditionClassBody(t, topScope, version)
		val content = buildClass(root.condition, classBody, topScope)
		fsa.generateFile('''«root.condition.withForwardSlashes»/«ele.conditionName(t).toConditionJavaType».java''', content)
	}

	private def StringConcatenationClient conditionClassBody(Condition condition, RDataType t, JavaScope scope, String version)  {
		val rosettaClass = condition.eContainer as Data
		val definition = RosettaGrammarUtil.quote(RosettaGrammarUtil.extractNodeText(condition, CONDITION__EXPRESSION))
		val conditionName = condition.conditionName(t)
		val className = toConditionJavaType(conditionName);
		val deps = dependencies.javaDependencies(condition.expression)
		val implicitVarRepr = condition.implicitVarInContext
		
		val classScope = scope.classScope(toConditionJavaType(conditionName))
		
		val validateScope = classScope.methodScope("validate")
		val pathId = validateScope.createUniqueIdentifier("path")
		
		val defaultClassScope = classScope.classScope("Default")
		val defaultClassName = defaultClassScope.createUniqueIdentifier("Default")
		
		deps.forEach[defaultClassScope.createIdentifier(it.toDependencyInstance, it.simpleName.toFirstLower)]
		
		val defaultClassValidateScope = defaultClassScope.methodScope("validate")
		val defaultClassPathId = defaultClassValidateScope.createUniqueIdentifier("path")
		val defaultClassResultId = defaultClassValidateScope.createUniqueIdentifier("result")
		val defaultClassFailureMessageId = defaultClassValidateScope.createUniqueIdentifier("failureMessage")
		
		val defaultClassExecuteScope = defaultClassScope.methodScope("execute")
		val defaultClassExceptionId = defaultClassExecuteScope.createUniqueIdentifier("ex")
		
		val noOpClassScope = classScope.classScope("NoOp")
		val noOpClassName = noOpClassScope.createUniqueIdentifier("NoOp")
		
		val noOpClassValidateScope = noOpClassScope.methodScope("validate")
		val noOpClassPathId = noOpClassValidateScope.createUniqueIdentifier("path")
		
		'''
			«emptyJavadocWithVersion(version)»
			@«RosettaDataRule»("«conditionName»")
			@«ImplementedBy»(«className».Default.class)
			public interface «className» extends «Validator»<«t.toJavaType»> {
				
				String NAME = "«conditionName»";
				String DEFINITION = «definition»;
				
				«ValidationResult»<«rosettaClass.name»> validate(«RosettaPath» «pathId», «rosettaClass.name» «validateScope.createIdentifier(implicitVarRepr, rosettaClass.name.toFirstLower)»);
				
				class «defaultClassName» implements «className» {
				
					«FOR dep : deps»
						@«Inject» protected «dep» «defaultClassScope.getIdentifierOrThrow(dep.toDependencyInstance)»;
						
					«ENDFOR»
					@Override
					public «ValidationResult»<«rosettaClass.name»> validate(«RosettaPath» «defaultClassPathId», «rosettaClass.name» «defaultClassValidateScope.createIdentifier(implicitVarRepr, rosettaClass.name.toFirstLower)») {
						«ComparisonResult» «defaultClassResultId» = executeDataRule(«defaultClassValidateScope.getIdentifierOrThrow(implicitVarRepr)»);
						if (result.get()) {
							return «ValidationResult».success(NAME, ValidationResult.ValidationType.DATA_RULE, "«rosettaClass.name»", «defaultClassPathId», DEFINITION);
						}
						
						String «defaultClassFailureMessageId» = «defaultClassResultId».getError();
						if («defaultClassFailureMessageId» == null || «defaultClassFailureMessageId».contains("Null") || «defaultClassFailureMessageId» == "") {
							«defaultClassFailureMessageId» = "Condition has failed.";
						}
						return «ValidationResult».failure(NAME, «ValidationType».DATA_RULE, "«rosettaClass.name»", «defaultClassPathId», DEFINITION, «defaultClassFailureMessageId»);
					}
					
					private «ComparisonResult» executeDataRule(«rosettaClass.name» «defaultClassExecuteScope.createIdentifier(implicitVarRepr, rosettaClass.name.toFirstLower)») {
						try «expressionHandler.javaCode(condition.expression, COMPARISON_RESULT, defaultClassExecuteScope)
								.completeAsReturn.toBlock»
						catch («Exception» «defaultClassExceptionId») {
							return «ComparisonResult».failure(«defaultClassExceptionId».getMessage());
						}
					}
				}
				
				@SuppressWarnings("unused")
				class «noOpClassName» implements «className» {
				
					@Override
					public «ValidationResult»<«rosettaClass.name»> validate(«RosettaPath» «noOpClassPathId», «rosettaClass.name» «noOpClassValidateScope.createIdentifier(implicitVarRepr, rosettaClass.name.toFirstLower)») {
						return «ValidationResult».success(NAME, ValidationResult.ValidationType.DATA_RULE, "«rosettaClass.name»", «noOpClassPathId», DEFINITION);
					}
				}
			}
		'''
	}
}

