package com.regnosys.rosetta.generator.java.util

import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.rosetta.RosettaRecordFeature
import com.rosetta.model.lib.records.Date
import java.time.ZonedDateTime

class RecordFeatureMap {
	def StringConcatenationClient recordFeatureToLambda(RosettaRecordFeature feature) {
		switch(feature.name) {
			/* Features of `zonedDateTime` */
			case "date": 
				'''_zdt -> «Date».of(_zdt.toLocalDate())'''
			case "time": 
				'''«ZonedDateTime»::toLocalTime'''
			case "timezone": 
				'''_zdt -> _zdt.getZone().getId()'''
			/* Features of `date` */
			case "day": 
				'''«Date»::getDay'''
			case "month": 
				'''«Date»::getMonth'''
			case "year": 
				'''«Date»::getYear'''
			default:
				throw new UnsupportedOperationException("Unsupported record feature named " + feature.name)
		}
	}
}
