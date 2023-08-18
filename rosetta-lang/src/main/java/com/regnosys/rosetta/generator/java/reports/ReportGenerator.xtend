package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.rosetta.RosettaBlueprintReport
import com.regnosys.rosetta.types.RObjectFactory
import javax.inject.Inject
import org.eclipse.xtext.generator.IFileSystemAccess2
import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension

class ReportGenerator {
	@Inject extension RObjectFactory
	@Inject FunctionGenerator functionGenerator
	@Inject extension JavaTypeTranslator
	@Inject extension ImportManagerExtension

	def generate(RootPackage root, IFileSystemAccess2 fsa, RosettaBlueprintReport report, String version) {
		
		val rFunction = buildRFunction(report)
		val clazz = rFunction.toFunctionJavaClass
		val topScope = new JavaScope(clazz.packageName)
		val classBody = functionGenerator.rBuildClass(rFunction, #[], true, topScope);
		val content = buildClass(clazz.packageName, classBody, topScope)
		fsa.generateFile(clazz.canonicalName.withForwardSlashes + ".java", content)
	}


}
