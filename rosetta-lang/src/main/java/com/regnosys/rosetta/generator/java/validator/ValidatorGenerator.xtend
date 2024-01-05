package com.regnosys.rosetta.generator.java.validator

import com.google.inject.ImplementedBy
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.function.FunctionDependencyProvider
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
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
import javax.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.CONDITION__EXPRESSION
import com.rosetta.model.lib.validation.ConditionValidation
import com.rosetta.model.lib.validation.RosettaModelObjectValidator

class ValidatorGenerator {
	@Inject ExpressionGenerator expressionHandler
	@Inject extension RosettaExtensions
	@Inject extension ImportManagerExtension
	@Inject FunctionDependencyProvider funcDependencies
	@Inject extension JavaIdentifierRepresentationService
	@Inject extension JavaTypeTranslator
	@Inject extension JavaTypeUtil
	
	def generate(RootPackage root, IFileSystemAccess2 fsa, Data data, String version) {
		val topScope = new JavaScope(root.typeValidation)
		
		val classBody = data.classBody(topScope)
		val content = buildClass(root.typeValidation, classBody, topScope)
		fsa.generateFile('''«root.typeValidation.withForwardSlashes»/«data.name»Validator.java''', content)
	}

	private def StringConcatenationClient classBody(Data data, JavaScope scope)  {
		
		val modelPojo = new RDataType(data).toJavaReferenceType
		'''
		public class «data.name»Validator implements «RosettaModelObjectValidator»<«modelPojo»>{
			
			
		}
		
		'''
		
	}
}

