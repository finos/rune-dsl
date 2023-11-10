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
import com.regnosys.rosetta.generator.java.statement.JavaStatementBuilder
import com.regnosys.rosetta.generator.java.statement.JavaExpression
import com.rosetta.util.types.JavaType

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
	
	def dispatch JavaStatementBuilder recordConstructor(RDateType recordType, Map<String, JavaStatementBuilder> features, JavaScope scope) {
		features.get("year")
			.then(
				features.get("month"),
				[list,item|JavaExpression.from('''«list», «item»''', null)],
				scope
			)
			.then(
				features.get("day"),
				[list,item|JavaExpression.from('''«list», «item»''', null)],
				scope
			)
			.mapExpression[
				JavaExpression.from('''«Date».of(«it»)''', JavaType.from(Date))
			]
	}
	def dispatch JavaStatementBuilder recordConstructor(RDateTimeType recordType, Map<String, JavaStatementBuilder> features, JavaScope scope) {
		features.get("date")
			.then(
				features.get("time"),
				[list,item|JavaExpression.from('''«list».toLocalDate(), «item»''', null)],
				scope
			)
			.mapExpression[
				JavaExpression.from('''«LocalDateTime».of(«it»)''', JavaType.from(LocalDateTime))
			]
	}
	def dispatch JavaStatementBuilder recordConstructor(RZonedDateTimeType recordType, Map<String, JavaStatementBuilder> features, JavaScope scope) {
		features.get("date")
			.then(
				features.get("time"),
				[list,item|JavaExpression.from('''«list».toLocalDate(), «item»''', null)],
				scope
			)
			.then(
				features.get("timezone"),
				[list,item|JavaExpression.from('''«list», «ZoneId».of(«item»)''', null)],
				scope
			)
			.mapExpression[
				JavaExpression.from('''«LocalDateTime».of(«it»)''', JavaType.from(LocalDateTime))
			]
	}
}
