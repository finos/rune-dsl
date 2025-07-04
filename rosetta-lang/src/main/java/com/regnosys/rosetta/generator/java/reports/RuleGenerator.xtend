package com.regnosys.rosetta.generator.java.reports

import jakarta.inject.Inject
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RObjectFactory
import com.rosetta.model.lib.reports.ReportFunction
import com.rosetta.util.types.JavaParameterizedType
import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.regnosys.rosetta.rosetta.RosettaRule
import com.fasterxml.jackson.core.type.TypeReference
import com.regnosys.rosetta.generator.java.RObjectJavaClassGenerator
import com.regnosys.rosetta.types.RFunction
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass
import com.rosetta.model.lib.functions.RosettaFunction
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope

class RuleGenerator extends RObjectJavaClassGenerator<RFunction, RGeneratedJavaClass<? extends RosettaFunction>> {
	@Inject extension JavaTypeTranslator
	@Inject extension RObjectFactory
	@Inject FunctionGenerator functionGenerator

	override protected streamObjects(RosettaModel model) {
		model.elements.stream.filter[it instanceof RosettaRule].map[it as RosettaRule].map[buildRFunction]
	}
	override protected createTypeRepresentation(RFunction rFunction) {
		rFunction.toFunctionJavaClass
	}
	override protected generate(RFunction rFunction, RGeneratedJavaClass<? extends RosettaFunction> clazz, String version, JavaClassScope scope) {
		val baseInterface = JavaParameterizedType.from(new TypeReference<ReportFunction<?, ?>>() {}, rFunction.inputs.head.toMetaJavaType, rFunction.output.toMetaJavaType)
		return functionGenerator.rBuildClass(rFunction, clazz, false, #[baseInterface], emptyMap, true, scope)
	}
}