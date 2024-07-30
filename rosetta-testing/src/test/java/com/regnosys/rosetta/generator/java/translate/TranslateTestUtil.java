package com.regnosys.rosetta.generator.java.translate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.inject.Injector;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.model.lib.ModelTranslationId;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.generated.GeneratedJavaClassService;

public class TranslateTestUtil {
	private final Injector injector;
	private final GeneratedJavaClassService generatedJavaClassService;
	private final ModelHelper modelHelper;
	
	@Inject
	public TranslateTestUtil(Injector injector, GeneratedJavaClassService generatedJavaClassService, ModelHelper modelHelper) {
		this.injector = injector;
		this.generatedJavaClassService = generatedJavaClassService;
		this.modelHelper = modelHelper;
	}
	
	public RosettaFunction createTranslation(Map<String, Class<?>> classes, String sourceName, List<String> inputNames, String outputName) {
		ModelTranslationId id = new ModelTranslationId(
				new ModelSymbolId(modelHelper.rootPackage(), sourceName),
				inputNames.stream().map(n -> new ModelSymbolId(modelHelper.rootPackage(), n)).collect(Collectors.toList()),
				new ModelSymbolId(modelHelper.rootPackage(), outputName)
			);
		JavaClass<RosettaFunction> classRepr = generatedJavaClassService.toJavaTranslationFunction(id);
		return (RosettaFunction)injector.getInstance(classes.get(classRepr.getCanonicalName().toString()));
	}
}
