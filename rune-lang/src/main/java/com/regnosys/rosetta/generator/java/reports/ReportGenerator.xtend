package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RObjectFactory
import com.rosetta.model.lib.reports.ReportFunction
import com.rosetta.util.types.JavaParameterizedType
import jakarta.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaReport
import com.fasterxml.jackson.core.type.TypeReference
import com.regnosys.rosetta.utils.ModelIdProvider
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.rosetta.model.lib.annotations.RuneLabelProvider
import java.util.Map
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass
import com.regnosys.rosetta.generator.java.RObjectJavaClassGenerator
import com.regnosys.rosetta.types.RFunction
import com.regnosys.rosetta.rosetta.RosettaModel
import com.rosetta.model.lib.functions.RosettaFunction
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope

class ReportGenerator extends RObjectJavaClassGenerator<RFunction, RGeneratedJavaClass<? extends RosettaFunction>> {
	@Inject extension RObjectFactory
	@Inject FunctionGenerator functionGenerator
	@Inject extension JavaTypeTranslator
	@Inject extension ModelIdProvider

	override protected streamObjects(RosettaModel model) {
		model.elements.stream.filter[it instanceof RosettaReport].map[it as RosettaReport].map[buildRFunction]
	}
	override protected createTypeRepresentation(RFunction rFunction) {
		rFunction.toFunctionJavaClass
	}
	override protected generateClass(RFunction rFunction, RGeneratedJavaClass<? extends RosettaFunction> clazz, String version, JavaClassScope scope) {
		val report = rFunction.EObject as RosettaReport
		val baseInterface = JavaParameterizedType.from(new TypeReference<ReportFunction<?, ?>>() {}, rFunction.inputs.head.toMetaJavaType, rFunction.output.toMetaJavaType)
		
		val Map<Class<?>, StringConcatenationClient> annotations = newLinkedHashMap
		annotations.put(com.rosetta.model.lib.annotations.RosettaReport, '''namespace="«report.model.toDottedPath»", body="«report.regulatoryBody.body.name»", corpusList={«FOR corpus: report.regulatoryBody.corpusList SEPARATOR ", "»"«corpus.name»"«ENDFOR»}''')
		val labelProviderClass = rFunction.toLabelProviderJavaClass
		annotations.put(RuneLabelProvider, '''labelProvider=«labelProviderClass».class''')
		
		return functionGenerator.rBuildClass(rFunction, clazz, false, #[baseInterface], annotations, true, scope);
	}
}
