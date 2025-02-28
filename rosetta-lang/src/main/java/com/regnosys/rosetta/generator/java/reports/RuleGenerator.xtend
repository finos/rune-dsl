package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import jakarta.inject.Inject
import org.eclipse.xtext.generator.IFileSystemAccess2
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.generator.java.JavaScope
import com.rosetta.model.lib.reports.ReportFunction
import com.rosetta.util.types.JavaParameterizedType
import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.regnosys.rosetta.rosetta.RosettaRule
import com.fasterxml.jackson.core.type.TypeReference

class RuleGenerator {
	@Inject extension JavaTypeTranslator
	@Inject extension RObjectFactory
	@Inject extension ImportManagerExtension
	@Inject FunctionGenerator functionGenerator

	
	def generate(RootPackage root, IFileSystemAccess2 fsa, RosettaRule rule, String version) {
		val rFunctionRule = buildRFunction(rule)
		val clazz = rFunctionRule.toFunctionJavaClass
		val baseInterface = JavaParameterizedType.from(new TypeReference<ReportFunction<?, ?>>() {}, rFunctionRule.inputs.head.toMetaJavaType, rFunctionRule.output.toMetaJavaType)
		val topScope = new JavaScope(clazz.packageName)
		val classBody = functionGenerator.rBuildClass(rFunctionRule, false, #[baseInterface], emptyMap, true, topScope)
		
		val content = buildClass(clazz.packageName, classBody, topScope)
		fsa.generateFile(clazz.canonicalName.withForwardSlashes + ".java", content)
	}
}