package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.types.RObjectFactory
import com.rosetta.model.lib.reports.ReportFunction
import com.rosetta.util.types.JavaInterface
import com.rosetta.util.types.JavaParameterizedType
import javax.inject.Inject
import org.eclipse.xtext.generator.IFileSystemAccess2
import com.regnosys.rosetta.rosetta.RosettaReport

class ReportGenerator {
	@Inject extension RObjectFactory
	@Inject FunctionGenerator functionGenerator
	@Inject extension JavaTypeTranslator
	@Inject extension ImportManagerExtension

	def generate(RootPackage root, IFileSystemAccess2 fsa, RosettaReport report, String version) {
		
		val rFunction = buildRFunction(report)
		val clazz = rFunction.toFunctionJavaClass
		val topScope = new JavaScope(clazz.packageName)
		val baseInterface = new JavaParameterizedType(JavaInterface.from(ReportFunction), rFunction.inputs.head.attributeToJavaType, rFunction.output.attributeToJavaType)
		val classBody = functionGenerator.rBuildClass(rFunction, false, #[baseInterface], true, topScope);
		val content = buildClass(clazz.packageName, classBody, topScope)
		fsa.generateFile(clazz.canonicalName.withForwardSlashes + ".java", content)
	}


}
