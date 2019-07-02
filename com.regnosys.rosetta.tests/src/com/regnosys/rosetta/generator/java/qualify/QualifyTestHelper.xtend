package com.regnosys.rosetta.generator.java.qualify

import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.meta.RosettaMetaDataBuilder
import com.rosetta.model.lib.qualify.QualifyResult
import com.rosetta.model.lib.qualify.QualifyResultsExtractor
import java.util.List

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.core.Is.is

class QualifyTestHelper {

	def getQualifyResult(List<QualifyResult> results, String isEventName) {
		val resultsMatchingIsEventName = results.filter[it.name==isEventName].toList
		assertThat('Expected single isEvent function with name ' + isEventName, resultsMatchingIsEventName.size, is(1))
		return QualifyResult.cast(resultsMatchingIsEventName.get(0))
	}
	
	def createUtilAndGetAllResults(RosettaModelObject model) {
		val util = new QualifyResultsExtractor(RosettaMetaDataBuilder.getMetaData(model).qualifyFunctions, RosettaModelObject.cast(model))
		return util.getAllResults()
	}
}