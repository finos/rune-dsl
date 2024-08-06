package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.types.RObjectFactory
import com.rosetta.model.lib.reports.ReportFunction
import com.rosetta.util.types.JavaParameterizedType
import javax.inject.Inject
import org.eclipse.xtext.generator.IFileSystemAccess2
import com.regnosys.rosetta.rosetta.RosettaReport
import com.fasterxml.jackson.core.type.TypeReference
import com.regnosys.rosetta.utils.ModelIdProvider

class ReportGenerator {
	@Inject extension RObjectFactory
	@Inject FunctionGenerator functionGenerator
	@Inject extension JavaTypeTranslator
	@Inject extension ImportManagerExtension
	@Inject extension ModelIdProvider

	def generate(RootPackage root, IFileSystemAccess2 fsa, RosettaReport report, String version) {
		
		val rFunction = buildRFunction(report)
		val clazz = rFunction.toFunctionJavaClass
		val topScope = new JavaScope(clazz.packageName)
		val baseInterface = JavaParameterizedType.from(new TypeReference<ReportFunction<?, ?>>() {}, rFunction.inputs.head.attributeToJavaType, rFunction.output.attributeToJavaType)
		val reportAnnotationArguments = '''namespace="«report.namespace.toDottedPath»", body="«report.regulatoryBody.body.name»", corpusList={«FOR corpus: report.regulatoryBody.corpusList SEPARATOR ", "»"«corpus.name»"«ENDFOR»}'''
		val classBody = functionGenerator.rBuildClass(rFunction, false, #[baseInterface], #{com.rosetta.model.lib.annotations.RosettaReport -> reportAnnotationArguments}, true, topScope);
		val content = buildClass(clazz.packageName, classBody, topScope)
		fsa.generateFile(clazz.canonicalName.withForwardSlashes + ".java", content)
	}


}
