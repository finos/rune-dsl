package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.types.RObjectFactory
import com.rosetta.model.lib.reports.ReportFunction
import com.rosetta.util.types.JavaParameterizedType
import jakarta.inject.Inject
import org.eclipse.xtext.generator.IFileSystemAccess2
import com.regnosys.rosetta.rosetta.RosettaReport
import com.fasterxml.jackson.core.type.TypeReference
import com.regnosys.rosetta.utils.ModelIdProvider
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.rosetta.model.lib.annotations.RuneLabelProvider
import java.util.Map

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
		val baseInterface = JavaParameterizedType.from(new TypeReference<ReportFunction<?, ?>>() {}, rFunction.inputs.head.toMetaJavaType, rFunction.output.toMetaJavaType)
		
		val Map<Class<?>, StringConcatenationClient> annotations = newLinkedHashMap
		annotations.put(com.rosetta.model.lib.annotations.RosettaReport, '''namespace="«report.model.toDottedPath»", body="«report.regulatoryBody.body.name»", corpusList={«FOR corpus: report.regulatoryBody.corpusList SEPARATOR ", "»"«corpus.name»"«ENDFOR»}''')
		val labelProviderClass = rFunction.toLabelProviderJavaClass
		annotations.put(RuneLabelProvider, '''labelProvider=«labelProviderClass».class''')
		
		val classBody = functionGenerator.rBuildClass(rFunction, false, #[baseInterface], annotations, true, topScope);
		val content = buildClass(clazz.packageName, classBody, topScope)
		fsa.generateFile(clazz.canonicalName.withForwardSlashes + ".java", content)
	}


}
