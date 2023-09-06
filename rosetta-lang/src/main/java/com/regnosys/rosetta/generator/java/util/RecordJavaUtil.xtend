package com.regnosys.rosetta.generator.java.util

import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.rosetta.RosettaRecordFeature
import com.rosetta.model.lib.records.Date
import java.time.ZonedDateTime
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.types.builtin.RDateType
import com.regnosys.rosetta.types.builtin.RDateTimeType
import com.regnosys.rosetta.types.builtin.RZonedDateTimeType
import java.util.Map
import java.time.LocalDateTime
import java.time.ZoneId

class RecordJavaUtil {
	def dispatch StringConcatenationClient recordFeatureToLambda(RDateType recordType, RosettaRecordFeature feature, JavaScope scope) {
		switch(feature.name) {
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
	def dispatch StringConcatenationClient recordFeatureToLambda(RDateTimeType recordType, RosettaRecordFeature feature, JavaScope scope) {
		switch(feature.name) {
			case "date": {
				val lambdaScope = scope.lambdaScope
				val dt = lambdaScope.createUniqueIdentifier("dt")
				'''«dt» -> «Date».of(«dt».toLocalDate())'''
			}
			case "time": 
				'''«LocalDateTime»::toLocalTime'''
			default:
				throw new UnsupportedOperationException("Unsupported record feature named " + feature.name)
		}
	}
	def dispatch StringConcatenationClient recordFeatureToLambda(RZonedDateTimeType recordType, RosettaRecordFeature feature, JavaScope scope) {
		switch(feature.name) {
			case "date": {
				val lambdaScope = scope.lambdaScope
				val zdt = lambdaScope.createUniqueIdentifier("zdt")
				'''«zdt» -> «Date».of(«zdt».toLocalDate())'''
			}
			case "time": 
				'''«ZonedDateTime»::toLocalTime'''
			case "timezone": {
				val lambdaScope = scope.lambdaScope
				val zdt = lambdaScope.createUniqueIdentifier("zdt")
				'''«zdt» -> «zdt».getZone().getId()'''
			}
			default:
				throw new UnsupportedOperationException("Unsupported record feature named " + feature.name)
		}
	}
	
	def dispatch StringConcatenationClient recordConstructor(RDateType recordType, Map<String, StringConcatenationClient> features) {
		'''«Date».of(«features.get("year")», «features.get("month")», «features.get("day")»)'''
	}
	def dispatch StringConcatenationClient recordConstructor(RDateTimeType recordType, Map<String, StringConcatenationClient> features) {
		'''«LocalDateTime».of(«features.get("date")».toLocalDate(), «features.get("time")»)'''
	}
	def dispatch StringConcatenationClient recordConstructor(RZonedDateTimeType recordType, Map<String, StringConcatenationClient> features) {
		'''«ZonedDateTime».of(«features.get("date")».toLocalDate(), «features.get("time")», «ZoneId».of(«features.get("timezone")»))'''
	}
}
