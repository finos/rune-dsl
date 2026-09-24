/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.generator.java.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.builder.JavaConditionalExpression;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.statement.builder.JavaLiteral;
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.rosetta.RosettaRecordFeature;
import com.regnosys.rosetta.types.builtin.RDateTimeType;
import com.regnosys.rosetta.types.builtin.RDateType;
import com.regnosys.rosetta.types.builtin.RRecordType;
import com.regnosys.rosetta.types.builtin.RZonedDateTimeType;
import com.rosetta.model.lib.records.Date;
import com.rosetta.util.types.JavaPrimitiveType;

import jakarta.inject.Inject;

public class RecordJavaUtil {
	@Inject
	private JavaTypeUtil typeUtil;

	public CodeRenderer recordFeatureToLambda(RRecordType recordType, RosettaRecordFeature feature, JavaStatementScope scope) {
		if (recordType instanceof RDateType) {
			return switch (feature.getName()) {
				case "day" -> out -> out.write(Date.class, "::getDay");
				case "month" -> out -> out.write(Date.class, "::getMonth");
				case "year" -> out -> out.write(Date.class, "::getYear");
				default -> throw unsupportedFeature(feature);
			};
		} else if (recordType instanceof RDateTimeType) {
			return switch (feature.getName()) {
				case "date" -> {
					GeneratedIdentifier dt = scope.lambdaScope().createUniqueIdentifier("dt");
					yield out -> out.write(dt, " -> ", Date.class, ".of(", dt, ".toLocalDate())");
				}
				case "time" -> out -> out.write(LocalDateTime.class, "::toLocalTime");
				default -> throw unsupportedFeature(feature);
			};
		} else if (recordType instanceof RZonedDateTimeType) {
			return switch (feature.getName()) {
				case "date" -> {
					GeneratedIdentifier zdt = scope.lambdaScope().createUniqueIdentifier("zdt");
					yield out -> out.write(zdt, " -> ", Date.class, ".of(", zdt, ".toLocalDate())");
				}
				case "time" -> out -> out.write(ZonedDateTime.class, "::toLocalTime");
				case "timezone" -> {
					GeneratedIdentifier zdt = scope.lambdaScope().createUniqueIdentifier("zdt");
					yield out -> out.write(zdt, " -> ", zdt, ".getZone().getId()");
				}
				default -> throw unsupportedFeature(feature);
			};
		}
		throw new IllegalArgumentException("Unsupported record type " + recordType);
	}

	private UnsupportedOperationException unsupportedFeature(RosettaRecordFeature feature) {
		return new UnsupportedOperationException("Unsupported record feature named " + feature.getName());
	}

	public JavaStatementBuilder recordConstructor(RRecordType recordType, Map<String, JavaStatementBuilder> features, JavaStatementScope scope) {
		if (recordType instanceof RDateType) {
			return ifAllNotNull(List.of("year", "month", "day"), features, args -> args.get(0)
					.then(args.get(1), (list, item) -> JavaExpression.from(out -> out.write(list, ", ", item), null), scope)
					.then(args.get(2), (list, item) -> JavaExpression.from(out -> out.write(list, ", ", item), null), scope)
					.mapExpression(it -> JavaExpression.from(out -> out.write(Date.class, ".of(", it, ")"), typeUtil.DATE)),
				scope);
		} else if (recordType instanceof RDateTimeType) {
			return ifAllNotNull(List.of("date", "time"), features, args -> args.get(0)
					.then(args.get(1), (list, item) -> JavaExpression.from(out -> out.write(list, ".toLocalDate(), ", item), null), scope)
					.mapExpression(it -> JavaExpression.from(out -> out.write(LocalDateTime.class, ".of(", it, ")"), typeUtil.LOCAL_DATE_TIME)),
				scope);
		} else if (recordType instanceof RZonedDateTimeType) {
			return ifAllNotNull(List.of("date", "time", "timezone"), features, args -> args.get(0)
					.then(args.get(1), (list, item) -> JavaExpression.from(out -> out.write(list, ".toLocalDate(), ", item), null), scope)
					.then(args.get(2), (list, item) -> JavaExpression.from(out -> out.write(list, ", ", ZoneId.class, ".of(", item, ")"), null), scope)
					.mapExpression(it -> JavaExpression.from(out -> out.write(ZonedDateTime.class, ".of(", it, ")"), typeUtil.ZONED_DATE_TIME)),
				scope);
		}
		throw new IllegalArgumentException("Unsupported record type " + recordType);
	}

	private JavaStatementBuilder ifAllNotNull(List<String> featureNames, Map<String, JavaStatementBuilder> allFeatures, Function<List<JavaStatementBuilder>, JavaStatementBuilder> conversion, JavaStatementScope scope) {
		List<JavaStatementBuilder> features = featureNames.stream().map(allFeatures::get).toList();
		if (features.stream().allMatch(feature -> feature.getExpressionType() instanceof JavaPrimitiveType)) {
			return conversion.apply(features);
		}
		List<GeneratedIdentifier> nullableArgs = new ArrayList<>();
		List<JavaStatementBuilder> args = new ArrayList<>();
		for (int i = 0; i < featureNames.size(); i++) {
			JavaStatementBuilder feature = features.get(i);
			if (feature.getExpressionType() instanceof JavaPrimitiveType) {
				args.add(feature);
			} else {
				args.add(feature.declareAsVariable(true, featureNames.get(i), scope));
				nullableArgs.add(scope.getIdentifierOrThrow(feature));
			}
		}
		return conversion.apply(args)
				.mapExpression(it -> new JavaConditionalExpression(
						JavaExpression.from(out -> out.join(nullableArgs, " && ", arg -> out.write(arg, " != null")), JavaPrimitiveType.BOOLEAN),
						it,
						JavaLiteral.NULL,
						typeUtil
				));
	}
}
