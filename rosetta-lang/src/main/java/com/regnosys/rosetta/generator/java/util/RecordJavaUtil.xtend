package com.regnosys.rosetta.generator.java.util

import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.rosetta.RosettaRecordFeature
import com.rosetta.model.lib.records.Date
import java.time.ZonedDateTime
import com.regnosys.rosetta.types.builtin.RDateType
import com.regnosys.rosetta.types.builtin.RDateTimeType
import com.regnosys.rosetta.types.builtin.RZonedDateTimeType
import java.util.Map
import java.time.LocalDateTime
import java.time.ZoneId
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import jakarta.inject.Inject
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.rosetta.util.types.JavaPrimitiveType
import java.util.List
import com.regnosys.rosetta.generator.java.statement.builder.JavaConditionalExpression
import com.regnosys.rosetta.generator.java.statement.builder.JavaLiteral
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope

class RecordJavaUtil {
	@Inject JavaTypeUtil typeUtil
	
	def dispatch StringConcatenationClient recordFeatureToLambda(RDateType recordType, RosettaRecordFeature feature, JavaStatementScope scope) {
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
	def dispatch StringConcatenationClient recordFeatureToLambda(RDateTimeType recordType, RosettaRecordFeature feature, JavaStatementScope scope) {
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
	def dispatch StringConcatenationClient recordFeatureToLambda(RZonedDateTimeType recordType, RosettaRecordFeature feature, JavaStatementScope scope) {
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
	
	def dispatch JavaStatementBuilder recordConstructor(RDateType recordType, Map<String, JavaStatementBuilder> features, JavaStatementScope scope) {
		#["year", "month", "day"].ifAllNotNull(features, [args|
            args.get(0)
                .then(
                    args.get(1),
                    [list,item|JavaExpression.from('''«list», «item»''', null)],
                    scope
                )
                .then(
                    args.get(2),
                    [list,item|JavaExpression.from('''«list», «item»''', null)],
                    scope
                )
                .mapExpression[
                    JavaExpression.from('''«Date».of(«it»)''', typeUtil.DATE)
                ]
        ], scope)
	}
	def dispatch JavaStatementBuilder recordConstructor(RDateTimeType recordType, Map<String, JavaStatementBuilder> features, JavaStatementScope scope) {
		#["date", "time"].ifAllNotNull(features, [args|
            args.get(0)
                .then(
                    args.get(1),
                    [list,item|JavaExpression.from('''«list».toLocalDate(), «item»''', null)],
                    scope
                )
                .mapExpression[
                    JavaExpression.from('''«LocalDateTime».of(«it»)''', typeUtil.LOCAL_DATE_TIME)
                ]
        ], scope)
	}
	def dispatch JavaStatementBuilder recordConstructor(RZonedDateTimeType recordType, Map<String, JavaStatementBuilder> features, JavaStatementScope scope) {
		#["date", "time", "timezone"].ifAllNotNull(features, [args|
		    args.get(0)
                .then(
                    args.get(1),
                    [list,item|JavaExpression.from('''«list».toLocalDate(), «item»''', null)],
                    scope
                )
                .then(
                    args.get(2),
                    [list,item|JavaExpression.from('''«list», «ZoneId».of(«item»)''', null)],
                    scope
                )
                .mapExpression[
                    JavaExpression.from('''«ZonedDateTime».of(«it»)''', typeUtil.ZONED_DATE_TIME)
                ]
		], scope)
	}
	
	private def JavaStatementBuilder ifAllNotNull(List<String> featureList, Map<String, JavaStatementBuilder> allFeatures, (List<JavaStatementBuilder>) => JavaStatementBuilder conversion, JavaStatementScope scope) {
        if (featureList.map[allFeatures.get(it)].forall[expressionType instanceof JavaPrimitiveType]) {
            return conversion.apply(featureList.map[allFeatures.get(it)])
        }
        val nullableArgs = newArrayList
        return conversion.apply(featureList
            .map[
                val feature = allFeatures.get(it)
                if (feature.expressionType instanceof JavaPrimitiveType) {
                    return feature
                } else {
                    val res = feature
                        .declareAsVariable(true, it, scope)
                    nullableArgs.add(scope.getIdentifierOrThrow(feature))
                    return res
                }
            ])
            .mapExpression[
                new JavaConditionalExpression(
                    JavaExpression.from('''«FOR arg : nullableArgs SEPARATOR ' && '»«arg» != null«ENDFOR»''', JavaPrimitiveType.BOOLEAN),
                    it,
                    JavaLiteral.NULL,
                    typeUtil
                )
            ]
	}
}
