package com.regnosys.rosetta.generator.java.qualify

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.meta.RosettaMetaDataBuilder
import com.rosetta.model.lib.qualify.QualifyFunctionFactory
import com.rosetta.model.lib.qualify.QualifyResult
import com.rosetta.model.lib.qualify.QualifyResultsExtractor
import java.util.List

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.core.Is.is
import com.rosetta.model.lib.validation.ModelObjectValidator

class QualifyTestHelper {

	final QualifyFunctionFactory funcFactory

	new() {
		// don't use the Language Injector. This is the Test env for the model.
		funcFactory = Guice.createInjector(new AbstractModule() {

			override protected configure() {
				bind(ModelObjectValidator).toInstance(new ModelObjectValidator() {
					
					override <T extends RosettaModelObject> validateAndFailOnErorr(Class<T> topClass, T modelObject) {
					}
					override <T extends RosettaModelObject> validateAndFailOnErorr(Class<T> topClass,
						List<? extends T> modelObjects) {
					}
				})
			}
		}).getInstance(QualifyFunctionFactory.Default)
	}

	def getQualifyResult(List<QualifyResult> results, String isEventName) {
		val resultsMatchingIsEventName = results.filter[it.name == isEventName].toList
		assertThat('Expected single isEvent function with name ' + isEventName, resultsMatchingIsEventName.size, is(1))
		return QualifyResult.cast(resultsMatchingIsEventName.get(0))
	}

	def createUtilAndGetAllResults(RosettaModelObject model) {
		val util = new QualifyResultsExtractor(
			RosettaMetaDataBuilder.getMetaData(model).getQualifyFunctions(funcFactory), RosettaModelObject.cast(model))
		return util.getAllResults()
	}
}
