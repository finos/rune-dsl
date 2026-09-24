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

package com.regnosys.rosetta.generator.java.expression;

import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.builder.JavaConditionalExpression;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.statement.builder.JavaIfThenElseBuilder;
import com.regnosys.rosetta.generator.java.statement.builder.JavaLiteral;
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder;
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;

import jakarta.inject.Inject;

/**
 * This service is responsible for coercing an expression from its actual Java type to an `expected` Java type.
 *
 * Both coercions of item types and of wrapper types are supported. Examples of supported coercions:
 * - `Long` to `BigDecimal`
 * - `BigInteger` to `Integer` (will throw a runtime exception if conversion looses information)
 * - `Boolean` to `ComparisonResult`
 * - `ComparisonResult` to `Boolean`
 * - `String` to `MapperS<String>`
 * - `MapperC<Long>` to `List<BigInteger>`
 * - `MapperC<Integer>` to `BigDecimal`
 * - `MapperC<Boolean>` to `ComparisonResult`
 * - `Void` to `LocalDate`
 * - `Void` to `MapperC<LocalDate>`
 * - `FieldWithMetaString` to `String`
 * - `String` to `FieldWithMetaString`
 *
 * Item to item coercions and item to wrapper coercions are performed null-safe.
 *
 * This service is auto-boxing aware. If the expected type is a wrapper class of a primitive type
 * and the input expression is of a primitive type as well, the result will be of a primitive type.
 *
 * Terminology:
 * - a "wrapper type" refers to any of the following classes:
 *   - `MapperS`
 *   - `MapperC`
 *   - `ComparisonResult`
 *   - `Mapper` (the general interface of the above classes)
 *   - `MapperListOfLists`
 *   - `List`
 * - an "item type" refers to the type of the items of a wrapper type, or to the type itself if it does not refer to a wrapper type.
 * For a precise definition, see the methods `JavaTypeUtil#isWrapper` and `JavaTypeUtil#getItemType`.
 */
public class TypeCoercionService {
	@Inject
	private JavaTypeUtil typeUtil;

	public JavaStatementBuilder addCoercions(JavaStatementBuilder expr, JavaType expected, JavaStatementScope scope) {
		return addCoercions(expr, expected, true, scope);
	}
	public JavaStatementBuilder addCoercions(JavaStatementBuilder expr, JavaType expected, boolean throwOnFail, JavaStatementScope scope) {
		JavaType actual = expr.getExpressionType();
		if (isNullOrVoid(typeUtil.getItemType(actual))) {
			return empty(expected);
		}

		return expr.mapExpression(it -> addCoercions(it, expected, throwOnFail, scope));
	}

	public JavaStatementBuilder addCoercions(JavaExpression expr, JavaType expected, JavaStatementScope scope) {
		return addCoercions(expr, expected, true, scope);
	}
	/**
	 * defaultItemValue: the expression to use if the coercion fails. If null, throw an error.
	 */
	public JavaStatementBuilder addCoercions(JavaExpression expr, JavaType expected, boolean throwOnFail, JavaStatementScope scope) {
		JavaType actual = expr.getExpressionType();
		// Simple coercions
		if (isNullOrVoid(typeUtil.getItemType(actual))) {
			return empty(expected);
		}
		if (actual.equals(expected)) {
			return expr;
		}
		if (actual.equals(JavaPrimitiveType.VOID)) {
			throw new IllegalArgumentException("Cannot coerce from primitive type `void`.");
		}

		// Complex coercions
		if (typeUtil.isWrapper(actual) && typeUtil.isWrapper(expected)) {
			return wrapperToWrapper(expr, expected, throwOnFail, scope);
		} else if (typeUtil.isWrapper(actual)) {
			return wrapperToItem(expr, expected, throwOnFail, scope);
		} else if (typeUtil.isWrapper(expected)) {
			return itemToWrapper(expr, expected, throwOnFail, scope);
		} else {
			return itemToItem(expr, expected, throwOnFail, scope);
		}
	}

	private boolean isNullOrVoid(JavaType itemType) {
		return itemType.equals(JavaReferenceType.NULL_TYPE) || typeUtil.isVoid(itemType);
	}

	private JavaStatementBuilder itemToItem(JavaExpression expr, JavaType expected, boolean throwOnFail, JavaStatementScope scope) {
		JavaType actual = expr.getExpressionType();
		// Strategy:
		// - if no item conversion is needed, return the given expression.
		// - Otherwise, assign last expression to a local variable.
		// - Check if that variable is null:
		//   - if it is null, return null,
		//   - otherwise, convert variable to expected type.

		return getItemConversion(actual, expected, throwOnFail, scope)
				.map(itemConversion -> convertNullSafe(expr, itemConversion, expected, false, scope))
				.orElse(expr);
	}
	private JavaStatementBuilder itemToWrapper(JavaExpression expr, JavaType expected, boolean throwOnFail, JavaStatementScope scope) {
		JavaType actual = expr.getExpressionType();
		JavaType expectedItemType = typeUtil.getItemType(expected);

		// Strategy:
		// - assign last expression to a local variable.
		// - check if that variable is null:
		//   - if it is null, return an empty wrapper of the expected type,
		//   - otherwise, convert the variable to the expected item type (if necessary) and then wrap the result to the expected wrapper type.

		Function<JavaExpression, JavaExpression> wrapConversion = getWrapConversion(expected);

		// Exception: wrapping to a MapperS or MapperC is null safe, so no need to do a null check.
		if (typeUtil.extendsMapper(expected)) {
			return getItemConversion(actual, expectedItemType, throwOnFail, scope)
					.map(itemConversion -> convertNullSafe(
							expr,
							itemConversion.andThen(converted -> converted.mapExpression(wrapConversion)),
							expected,
							false,
							scope))
					.orElse(expr.mapExpression(wrapConversion));
		} else {
			boolean expectedIsMeta = expected instanceof RJavaWithMetaValue || expectedItemType instanceof RJavaWithMetaValue;
			boolean isMetaToItemConversion = actual instanceof RJavaWithMetaValue && !expectedIsMeta;

			Function<JavaExpression, ? extends JavaStatementBuilder> totalConversion = getItemConversion(actual, expectedItemType, throwOnFail, scope)
					.<Function<JavaExpression, ? extends JavaStatementBuilder>>map(itemConversion ->
							itemConversion.andThen(converted -> converted.mapExpression(wrapConversion)))
					.orElse(wrapConversion);

			return convertNullSafe(expr, totalConversion, expected, isMetaToItemConversion, scope);
		}
	}
	private JavaStatementBuilder wrapperToItem(JavaExpression expr, JavaType expected, boolean throwOnFail, JavaStatementScope scope) {
		JavaType actual = expr.getExpressionType();
		// Strategy:
		// - unwrap the given expression.
		// - perform item to item conversion.

		// Special case: mapper to primitive
		if (typeUtil.extendsMapper(actual) && expected instanceof JavaPrimitiveType) {
			return expr.mapExpression(it ->
					JavaExpression.from(out -> out.write(it, ".getOrDefault(", empty(expected), ")"), expected));
		}

		JavaExpression unwrappedExpr = getUnwrapConversion(actual).apply(expr);

		return itemToItem(unwrappedExpr, expected, throwOnFail, scope);
	}
	private JavaExpression wrapperToWrapper(JavaExpression expr, JavaType expected, boolean throwOnFail, JavaStatementScope scope) {
		JavaType actual = expr.getExpressionType();
		JavaType expectedItemType = typeUtil.getItemType(expected);

		// Strategy:
		// - if no conversion is needed, return the given expression.
		// - Otherwise, first convert the item type to the expected item type (if necessary),
		// - then convert the wrapper type to the expected wrapper type (if necessary).

		Optional<Function<JavaExpression, JavaExpression>> optionalWrappedItemConversion = getWrappedItemConversion(actual, expectedItemType, throwOnFail, scope);
		Optional<Function<JavaExpression, JavaExpression>> optionalWrapperConversion = getWrapperConversion(
				optionalWrappedItemConversion.isEmpty() ? actual : typeUtil.changeItemType(actual, expectedItemType),
				expected);

		if (optionalWrappedItemConversion.isEmpty() && optionalWrapperConversion.isEmpty()) {
			return expr;
		}

		Function<JavaExpression, JavaExpression> totalConversion;
		if (optionalWrappedItemConversion.isEmpty()) {
			totalConversion = optionalWrapperConversion.orElseThrow();
		} else if (optionalWrapperConversion.isEmpty()) {
			totalConversion = optionalWrappedItemConversion.orElseThrow();
		} else {
			totalConversion = optionalWrapperConversion.orElseThrow().compose(optionalWrappedItemConversion.orElseThrow());
		}

		return totalConversion.apply(expr);
	}

	private Optional<Function<JavaExpression, ? extends JavaStatementBuilder>> getItemConversion(JavaType actual, JavaType expected, boolean throwOnFail, JavaStatementScope scope) {
		if (actual.equals(expected)) {
			return Optional.empty();
		}

		if (actual.toReferenceType().equals(expected.toReferenceType())) {
			// Autoboxing and unboxing
			return Optional.of(it -> it);
		} else if (typeUtil.extendsNumber(actual.toReferenceType()) && typeUtil.extendsNumber(expected.toReferenceType())) {
			// Number type to number type
			return Optional.of(it -> getNumberConversionExpression(it, expected, throwOnFail, scope));
		} else if (actual instanceof RJavaWithMetaValue) {
			// Meta to non-meta
			return Optional.of(it -> metaToItemConversionExpression(it, expected, throwOnFail, scope));
		} else if (expected instanceof RJavaWithMetaValue expectedMeta) {
			// Non-meta to meta
			return Optional.of(it -> itemToMetaConversionExpression(it, expectedMeta, throwOnFail, scope));
		} else if (expected instanceof JavaPojoInterface expectedPojo && expected.isSubtypeOf(actual)) {
			// Supertype to subtype
			return Optional.of(it -> downCastConversionExpression(it, expectedPojo, throwOnFail, scope));
		}
		return Optional.empty();
	}
	private Function<JavaExpression, JavaExpression> getWrapConversion(JavaType wrapperType) {
		if (typeUtil.isList(wrapperType)) {
			return this::getItemToListConversionExpression;
		} else if (typeUtil.isMapperS(wrapperType) || typeUtil.isMapper(wrapperType)) {
			return this::getItemToMapperSConversionExpression;
		} else if (typeUtil.isMapperC(wrapperType)) {
			return this::getItemToMapperCConversionExpression;
		} else if (typeUtil.isComparisonResult(wrapperType)) {
			return this::getItemToComparisonResultConversionExpression;
		} else {
			throw unexpectedWrapperException(wrapperType);
		}
	}
	private Function<JavaExpression, JavaExpression> getUnwrapConversion(JavaType wrapperType) {
		if (typeUtil.isList(wrapperType)) {
			return this::getListToItemConversionExpression;
		} else if (typeUtil.extendsMapper(wrapperType)) {
			return this::getMapperToItemConversionExpression;
		} else {
			throw unexpectedWrapperException(wrapperType);
		}
	}
	private Optional<Function<JavaExpression, JavaExpression>> getWrappedItemConversion(JavaType actual, JavaType expectedItemType, boolean throwOnFail, JavaStatementScope scope) {
		JavaType actualItemType = typeUtil.getItemType(actual);
		JavaReferenceType expectedItemReferenceType = expectedItemType.toReferenceType();

		return getItemConversion(actualItemType, expectedItemType, throwOnFail, scope)
				.<Function<JavaExpression, JavaExpression>>map(itemConversion -> {
					if (typeUtil.isList(actual)) {
						return it -> getListItemConversionExpression(it, itemConversion, expectedItemReferenceType, scope);
					} else if (typeUtil.isMapperS(actual)) {
						return it -> getMapperSItemConversionExpression(it, itemConversion, expectedItemReferenceType, scope);
					} else if (typeUtil.isMapperC(actual)) {
						return it -> getMapperCItemConversionExpression(it, itemConversion, expectedItemReferenceType, scope);
					} else if (typeUtil.isMapperListOfLists(actual)) {
						return it -> getMapperListOfListsItemConversionExpression(it, itemConversion, expectedItemReferenceType, scope);
					} else {
						throw unexpectedWrapperException(actual);
					}
				});
	}
	private Optional<Function<JavaExpression, JavaExpression>> getWrapperConversion(JavaType actual, JavaType expected) {
		JavaType expectedItemType = typeUtil.getItemType(expected);
		if (typeUtil.isComparisonResult(actual)) {
			if (typeUtil.isMapperS(expected)) {
				// Case ComparisonResult to MapperS
				return Optional.of(it -> JavaExpression.from(out -> out.write(it, ".asMapper()"), typeUtil.wrap(typeUtil.MAPPER_S, typeUtil.BOOLEAN)));
			} else if (typeUtil.isMapperC(expected)) {
				// Case ComparisonResult to MapperC
				// Not handled
			} else if (typeUtil.isMapperListOfLists(expected)) {
				// Case ComparisonResult to MapperListOfLists
				// Not handled
			} else if (typeUtil.isList(expected)) {
				// Case ComparisonResult to List
				return Optional.of(it -> JavaExpression.from(out -> out.write(it, ".getMulti()"), typeUtil.wrap(typeUtil.LIST, typeUtil.BOOLEAN)));
			}
		} else if (typeUtil.extendsMapper(actual)) {
			if (typeUtil.isComparisonResult(expected)) {
				// Case Mapper to ComparisonResult
				return Optional.of(it -> JavaExpression.from(out -> out.write(ComparisonResult.class, ".ofNullSafe(", it, ")"), typeUtil.COMPARISON_RESULT));
			} else if (typeUtil.extendsMapper(expected)) {
				if (typeUtil.isMapperS(actual) && typeUtil.hasWildcardArgument(actual) && typeUtil.isMapperS(expected) && !typeUtil.hasWildcardArgument(expected)) {
					// Case immutable MapperS<? extends T> to mutable MapperS<T>
					return Optional.of(it -> JavaExpression.from(out -> out.write(it, ".map(\"Make mutable\", ", Function.class, ".identity())"), typeUtil.wrap(typeUtil.MAPPER_S, expectedItemType)));
				} else if (typeUtil.isMapperC(actual) && typeUtil.hasWildcardArgument(actual) && typeUtil.isMapperC(expected) && !typeUtil.hasWildcardArgument(expected)) {
					// Case immutable MapperC<? extends T> to mutable MapperC<T>
					return Optional.of(it -> JavaExpression.from(out -> out.write(it, ".map(\"Make mutable\", ", Function.class, ".identity())"), typeUtil.wrap(typeUtil.MAPPER_C, expectedItemType)));
				} else if (typeUtil.isMapperS(actual) && typeUtil.isMapperC(expected)) {
					// Case MapperS to MapperC
					return Optional.of(it -> JavaExpression.from(out -> out.write(MapperC.class, ".of(", it, ")"), typeUtil.wrap(typeUtil.MAPPER_C, expectedItemType)));
				} else if (typeUtil.isMapperC(actual) && typeUtil.isMapperS(expected)) {
					// Case MapperC to MapperS
					return Optional.of(it -> JavaExpression.from(out -> out.write(MapperS.class, ".of(", it, ".get())"), typeUtil.wrap(typeUtil.MAPPER_S, expectedItemType)));
				}
			} else if (typeUtil.isMapperListOfLists(expected)) {
				// Case Mapper to MapperListOfLists
				// Not handled
			} else if (typeUtil.isList(expected)) {
				// Case Mapper to List
				if (typeUtil.hasWildcardArgument(actual) && !typeUtil.hasWildcardArgument(expected)) {
					return Optional.of(it -> JavaExpression.from(out -> out.write("new ", ArrayList.class, "<>(", it, ".getMulti())"), typeUtil.wrap(typeUtil.LIST, expectedItemType)));
				} else if (typeUtil.hasWildcardArgument(actual)) {
					return Optional.of(it -> JavaExpression.from(out -> out.write(it, ".getMulti()"), typeUtil.wrapExtends(typeUtil.LIST, expectedItemType)));
				} else {
					return Optional.of(it -> JavaExpression.from(out -> out.write(it, ".getMulti()"), typeUtil.wrap(typeUtil.LIST, expectedItemType)));
				}
			}
		} else if (typeUtil.isMapperListOfLists(actual)) {
			// Not handled
		} else if (typeUtil.isList(actual)) {
			if (typeUtil.isComparisonResult(expected)) {
				// Case List to ComparisonResult
				return Optional.of(it -> JavaExpression.from(out -> out.write(ComparisonResult.class, ".ofNullSafe(", MapperC.class, ".of(", it, "))"), typeUtil.COMPARISON_RESULT));
			} else if (typeUtil.isMapperS(expected)) {
				// Case List to MapperS
				return Optional.of(it -> JavaExpression.from(out -> out.write(MapperS.class, ".of(", it, ".get(0))"), typeUtil.wrap(typeUtil.MAPPER_S, expectedItemType)));
			} else if (typeUtil.isMapperC(expected) || typeUtil.isMapper(expected)) {
				// Case List to MapperC/Mapper
				return Optional.of(it -> JavaExpression.from(out -> out.write(MapperC.class, ".<", expectedItemType, ">of(", it, ")"), typeUtil.wrap(typeUtil.MAPPER_C, expectedItemType)));
			} else if (typeUtil.isMapperListOfLists(expected)) {
				// Case List to MapperListOfLists
				// Not handled
			} else if (typeUtil.isList(expected) && typeUtil.hasWildcardArgument(actual) && !typeUtil.hasWildcardArgument(expected)) {
				// Case immutable List<? extends T> to mutable List<T>
				return Optional.of(it -> JavaExpression.from(out -> out.write("new ", ArrayList.class, "(", it, ")"), typeUtil.wrap(typeUtil.LIST, expectedItemType)));
			}
		}
		return Optional.empty();
	}
	private JavaStatementBuilder convertNullSafe(JavaExpression expr, Function<JavaExpression, ? extends JavaStatementBuilder> conversion, JavaType expected, boolean nullCheckMetaValue, JavaStatementScope scope) {
		JavaType actual = expr.getExpressionType();
		if (actual instanceof JavaPrimitiveType) {
			return expr.mapExpression(conversion);
		}

		return expr
				.declareAsVariable(true, uncapitalize(actual.getSimpleName()), scope)
				.mapExpression(varExpr -> {
					JavaExpression conditionExpr = JavaExpression.from(out -> {
						out.write(varExpr, " == null");
						if (nullCheckMetaValue) {
							out.write(" || ", varExpr, ".getValue() == null");
						}
					}, JavaPrimitiveType.BOOLEAN);
					JavaStatementBuilder converted = conversion.apply(varExpr);
					if (converted instanceof JavaExpression convertedExpr) {
						return new JavaConditionalExpression(conditionExpr, empty(expected), convertedExpr, typeUtil);
					}
					return new JavaIfThenElseBuilder(conditionExpr, empty(expected), converted, typeUtil);
				});
	}

	private JavaExpression empty(JavaType expected) {
		JavaType itemType = typeUtil.getItemType(expected);
		if (typeUtil.isList(expected)) {
			return JavaExpression.from(out -> out.write(Collections.class, ".<", itemType, ">emptyList()"), typeUtil.wrap(typeUtil.LIST, itemType));
		} else if (typeUtil.isMapperS(expected) || typeUtil.isMapper(expected)) {
			return JavaExpression.from(out -> out.write(MapperS.class, ".<", itemType, ">ofNull()"), typeUtil.wrap(typeUtil.MAPPER_S, itemType));
		} else if (typeUtil.isMapperC(expected)) {
			return JavaExpression.from(out -> out.write(MapperC.class, ".<", itemType, ">ofNull()"), typeUtil.wrap(typeUtil.MAPPER_C, itemType));
		} else if (typeUtil.isComparisonResult(expected)) {
			return JavaExpression.from(out -> out.write(ComparisonResult.class, ".ofEmpty()"), typeUtil.COMPARISON_RESULT);
		} else if (expected.equals(JavaPrimitiveType.BOOLEAN)) {
			return JavaExpression.from(out -> out.write("false"), JavaPrimitiveType.BOOLEAN);
		} else if (expected instanceof RJavaWithMetaValue) {
			return JavaExpression.from(out -> out.write(expected, ".builder().build()"), itemType);
		} else if (expected instanceof JavaPrimitiveType) {
			throw new IllegalArgumentException("No empty representation for primitive type `" + expected + "`.");
		} else {
			return JavaLiteral.NULL;
		}
	}

	/*
	 * 1. Unwrap the meta by calling getValue() on the expression
	 * 2. Map expression to a call to itemToItem(it, expected)
	 */
	private JavaStatementBuilder metaToItemConversionExpression(JavaExpression expression, JavaType expected, boolean throwOnFail, JavaStatementScope scope) {
		if (expression.getExpressionType() instanceof RJavaWithMetaValue actual) {
			return JavaExpression.from(out -> out.write(expression, ".getValue()"), actual.getValueType())
					.mapExpression(it -> itemToItem(it, expected, throwOnFail, scope));
		} else {
			return JavaLiteral.NULL;
		}
	}

	/*
	 * 1. Get the item conversion expression lambda for the given expression
	 * 2. If the lambda exists then run it and wrap the response in RJavaWithMetaValue builder
	 * 3. If no lambda exists wrap the given expression in RJavaWithMetaValue builder
	 */
	private JavaStatementBuilder itemToMetaConversionExpression(JavaExpression expression, RJavaWithMetaValue expected, boolean throwOnFail, JavaStatementScope scope) {
		JavaType expectedValueType = expected.getValueType();
		return getItemConversion(expression.getExpressionType(), expectedValueType, throwOnFail, scope)
				.<JavaStatementBuilder>map(itemConversion ->
						itemConversion.apply(expression)
								.mapExpression(it -> JavaExpression.from(out -> out.write(expected, ".builder().setValue(", it, ").build()"), expected)))
				.orElseGet(() -> JavaExpression.from(out -> out.write(expected, ".builder().setValue(", expression, ").build()"), expected));
	}

	private JavaStatementBuilder downCastConversionExpression(JavaExpression expression, JavaPojoInterface expected, boolean throwOnFail, JavaStatementScope scope) {
		if (throwOnFail) {
			return JavaExpression.from(out -> out.write(expected, ".class.cast(", expression, ")"), expected);
		} else {
			return expression
					.declareAsVariable(true, uncapitalize(expression.getExpressionType().getSimpleName()), scope)
					.mapExpression(it -> new JavaConditionalExpression(
							JavaExpression.from(out -> out.write(it, " instanceof ", expected), JavaPrimitiveType.BOOLEAN),
							JavaExpression.from(out -> out.write(expected, ".class.cast(", it, ")"), expected),
							JavaLiteral.NULL,
							typeUtil));
		}
	}

	private JavaStatementBuilder getNumberConversionExpression(JavaExpression expression, JavaType expected, boolean throwOnFail, JavaStatementScope scope) {
		JavaType actual = expression.getExpressionType();
		if (typeUtil.isInteger(actual.toReferenceType())) {
			if (typeUtil.isLong(expected.toReferenceType())) {
				if (actual.equals(JavaPrimitiveType.INT)) {
					if (expected.equals(JavaPrimitiveType.LONG)) {
						// Case int to long
						return JavaExpression.from(out -> out.write(expression), JavaPrimitiveType.LONG);
					} else {
						// Case int to Long
						return JavaExpression.from(out -> out.write("(long) ", expression), JavaPrimitiveType.LONG);
					}
				} else {
					// Case Integer to long/Long
					return JavaExpression.from(out -> out.write(expression, ".longValue()"), JavaPrimitiveType.LONG);
				}
			} else if (typeUtil.isBigInteger(expected)) {
				// Case int/Integer to BigInteger
				return JavaExpression.from(out -> out.write(BigInteger.class, ".valueOf(", expression, ")"), typeUtil.BIG_INTEGER);
			} else if (typeUtil.isBigDecimal(expected)) {
				// Case int/Integer to BigDecimal
				return JavaExpression.from(out -> out.write(BigDecimal.class, ".valueOf(", expression, ")"), typeUtil.BIG_DECIMAL);
			} else {
				throw unexpectedCaseException(actual, expected);
			}
		} else if (typeUtil.isLong(actual.toReferenceType())) {
			if (typeUtil.isInteger(expected.toReferenceType())) {
				// Case long/Long to int/Integer
				if (throwOnFail) {
					return JavaExpression.from(out -> out.write(Math.class, ".toIntExact(", expression, ")"), JavaPrimitiveType.INT);
				} else {
					return expression.declareAsVariable(true, "i", scope)
							.mapExpression(it -> new JavaConditionalExpression(
									JavaExpression.from(out -> out.write(it, " <= ", Integer.class, ".MAX_VALUE && ", it, " >= ", Integer.class, ".MIN_VALUE"), JavaPrimitiveType.BOOLEAN),
									JavaExpression.from(out -> out.write("(int) ", it), JavaPrimitiveType.INT),
									JavaLiteral.NULL,
									typeUtil));
				}
			} else if (typeUtil.isBigInteger(expected)) {
				// Case long/Long to BigInteger
				return JavaExpression.from(out -> out.write(BigInteger.class, ".valueOf(", expression, ")"), typeUtil.BIG_INTEGER);
			} else if (typeUtil.isBigDecimal(expected)) {
				// Case long/Long to BigDecimal
				return JavaExpression.from(out -> out.write(BigDecimal.class, ".valueOf(", expression, ")"), typeUtil.BIG_DECIMAL);
			} else {
				throw unexpectedCaseException(actual, expected);
			}
		} else if (typeUtil.isBigInteger(actual)) {
			if (typeUtil.isInteger(expected.toReferenceType())) {
				// Case BigInteger to int/Integer
				if (throwOnFail) {
					return JavaExpression.from(out -> out.write(expression, ".intValueExact()"), JavaPrimitiveType.INT);
				} else {
					return expression.declareAsVariable(true, "i", scope)
							.mapExpression(it -> new JavaConditionalExpression(
									JavaExpression.from(out -> out.write(BigInteger.class, ".valueOf(", it, ".intValue()).equals(", it, ")"), JavaPrimitiveType.BOOLEAN),
									JavaExpression.from(out -> out.write(it, ".intValue()"), JavaPrimitiveType.INT),
									JavaLiteral.NULL,
									typeUtil));
				}
			} else if (typeUtil.isLong(expected.toReferenceType())) {
				// Case BigInteger to long/Long
				if (throwOnFail) {
					return JavaExpression.from(out -> out.write(expression, ".longValueExact()"), JavaPrimitiveType.LONG);
				} else {
					return expression.declareAsVariable(true, "i", scope)
							.mapExpression(it -> new JavaConditionalExpression(
									JavaExpression.from(out -> out.write(BigInteger.class, ".valueOf(", it, ".longValue()).equals(", it, ")"), JavaPrimitiveType.BOOLEAN),
									JavaExpression.from(out -> out.write(it, ".longValue()"), JavaPrimitiveType.LONG),
									JavaLiteral.NULL,
									typeUtil));
				}
			} else if (typeUtil.isBigDecimal(expected)) {
				// Case BigInteger to BigDecimal
				return JavaExpression.from(out -> out.write("new ", BigDecimal.class, "(", expression, ")"), typeUtil.BIG_DECIMAL);
			} else {
				throw unexpectedCaseException(actual, expected);
			}
		} else if (typeUtil.isBigDecimal(actual)) {
			if (typeUtil.isInteger(expected.toReferenceType())) {
				// Case BigDecimal to int/Integer
				if (throwOnFail) {
					return JavaExpression.from(out -> out.write(expression, ".intValueExact()"), JavaPrimitiveType.INT);
				} else {
					return expression.declareAsVariable(true, "d", scope)
							.mapExpression(it -> new JavaConditionalExpression(
									JavaExpression.from(out -> out.write(BigDecimal.class, ".valueOf(", it, ".intValue()).compareTo(", it, ") == 0"), JavaPrimitiveType.BOOLEAN),
									JavaExpression.from(out -> out.write(it, ".intValue()"), JavaPrimitiveType.INT),
									JavaLiteral.NULL,
									typeUtil));
				}
			} else if (typeUtil.isLong(expected.toReferenceType())) {
				// Case BigDecimal to long/Long
				if (throwOnFail) {
					return JavaExpression.from(out -> out.write(expression, ".longValueExact()"), JavaPrimitiveType.LONG);
				} else {
					return expression.declareAsVariable(true, "d", scope)
							.mapExpression(it -> new JavaConditionalExpression(
									JavaExpression.from(out -> out.write(BigDecimal.class, ".valueOf(", it, ".longValue()).compareTo(", it, ") == 0"), JavaPrimitiveType.BOOLEAN),
									JavaExpression.from(out -> out.write(it, ".longValue()"), JavaPrimitiveType.LONG),
									JavaLiteral.NULL,
									typeUtil));
				}
			} else if (typeUtil.isBigInteger(expected)) {
				// Case BigDecimal to BigInteger
				if (throwOnFail) {
					return JavaExpression.from(out -> out.write(expression, ".toBigIntegerExact()"), typeUtil.BIG_INTEGER);
				} else {
					return expression.declareAsVariable(true, "d", scope)
							.mapExpression(it -> new JavaConditionalExpression(
									JavaExpression.from(out -> out.write("new ", BigDecimal.class, "(", it, ".toBigInteger()).compareTo(", it, ") == 0"), JavaPrimitiveType.BOOLEAN),
									JavaExpression.from(out -> out.write(it, ".toBigInteger()"), typeUtil.BIG_INTEGER),
									JavaLiteral.NULL,
									typeUtil));
				}
			} else {
				throw unexpectedCaseException(actual, expected);
			}
		} else {
			throw unexpectedCaseException(actual, expected);
		}
	}
	private JavaExpression getItemToListConversionExpression(JavaExpression expression) {
		return JavaExpression.from(out -> out.write(Collections.class, ".singletonList(", expression, ")"), typeUtil.wrap(typeUtil.LIST, expression.getExpressionType()));
	}
	private JavaExpression getItemToMapperSConversionExpression(JavaExpression expression) {
		return JavaExpression.from(out -> out.write(MapperS.class, ".of(", expression, ")"), typeUtil.wrap(typeUtil.MAPPER_S, expression.getExpressionType()));
	}
	private JavaExpression getItemToMapperCConversionExpression(JavaExpression expression) {
		return JavaExpression.from(out -> out.write(MapperC.class, ".of(", Collections.class, ".singletonList(", expression, "))"), typeUtil.wrap(typeUtil.MAPPER_C, expression.getExpressionType()));
	}
	private JavaExpression getItemToComparisonResultConversionExpression(JavaExpression expression) {
		return JavaExpression.from(out -> out.write(ComparisonResult.class, ".ofNullSafe(", MapperS.class, ".of(", expression, "))"), typeUtil.COMPARISON_RESULT);
	}
	private JavaExpression getListToItemConversionExpression(JavaExpression expression) {
		return JavaExpression.from(out -> out.write(MapperC.class, ".of(", expression, ").get()"), typeUtil.getItemType(expression.getExpressionType()));
	}
	private JavaExpression getMapperToItemConversionExpression(JavaExpression expression) {
		return JavaExpression.from(out -> out.write(expression, ".get()"), typeUtil.getItemType(expression.getExpressionType()));
	}
	private JavaExpression getListItemConversionExpression(JavaExpression expression, Function<JavaExpression, ? extends JavaStatementBuilder> itemConversion, JavaReferenceType expectedItemType, JavaStatementScope scope) {
		JavaType actualItemType = typeUtil.getItemType(expression.getExpressionType());
		JavaStatementScope lambdaScope = scope.lambdaScope();
		GeneratedIdentifier lambdaParam = lambdaScope.createUniqueIdentifier(uncapitalize(actualItemType.getSimpleName()));
		JavaStatementBuilder resultItem = itemConversion.apply(new JavaVariable(lambdaParam, actualItemType));
		JavaType resultType = typeUtil.wrap(typeUtil.LIST, expectedItemType);
		return JavaExpression.from(out -> {
			out.writeln(expression, ".stream()");
			out.indented(() -> {
				out.writeln(".<", expectedItemType, ">map(", lambdaParam, " -> ", resultItem.toLambdaBody(), ")");
				out.writeln(".collect(", Collectors.class, ".toList())");
			});
		}, resultType);
	}
	private JavaExpression getMapperSItemConversionExpression(JavaExpression expression, Function<JavaExpression, ? extends JavaStatementBuilder> itemConversion, JavaReferenceType expectedItemType, JavaStatementScope scope) {
		JavaType actualItemType = typeUtil.getItemType(expression.getExpressionType());
		JavaStatementScope lambdaScope = scope.lambdaScope();
		GeneratedIdentifier lambdaParam = lambdaScope.createUniqueIdentifier(uncapitalize(actualItemType.getSimpleName()));
		JavaVariable inputToItem = new JavaVariable(lambdaParam, actualItemType);
		JavaType resultType = typeUtil.wrap(typeUtil.MAPPER_S, expectedItemType);
		JavaStatementBuilder resultItemNullSafe = convertNullSafe(inputToItem, itemConversion, expectedItemType, false, scope);
		return JavaExpression.from(
				out -> out.write(expression, ".<", typeUtil.getItemType(resultType), ">map(\"Type coercion\", ", lambdaParam, " -> ", resultItemNullSafe.toLambdaBody(), ")"),
				resultType);
	}
	private JavaExpression getMapperCItemConversionExpression(JavaExpression expression, Function<JavaExpression, ? extends JavaStatementBuilder> itemConversion, JavaReferenceType expectedItemType, JavaStatementScope scope) {
		JavaType actualItemType = typeUtil.getItemType(expression.getExpressionType());
		JavaStatementScope lambdaScope = scope.lambdaScope();
		GeneratedIdentifier lambdaParam = lambdaScope.createUniqueIdentifier(uncapitalize(actualItemType.getSimpleName()));
		JavaStatementBuilder resultItem = itemConversion.apply(new JavaVariable(lambdaParam, actualItemType));
		JavaType resultType = typeUtil.wrap(typeUtil.MAPPER_C, expectedItemType);
		return JavaExpression.from(
				out -> out.write(expression, ".<", expectedItemType, ">map(\"Type coercion\", ", lambdaParam, " -> ", resultItem.toLambdaBody(), ")"),
				resultType);
	}
	private JavaExpression getMapperListOfListsItemConversionExpression(JavaExpression expression, Function<JavaExpression, ? extends JavaStatementBuilder> itemConversion, JavaReferenceType expectedItemType, JavaStatementScope scope) {
		JavaType actualItemType = typeUtil.getItemType(expression.getExpressionType());
		JavaStatementScope listToListLambdaScope = scope.lambdaScope();
		GeneratedIdentifier mapperCParam = listToListLambdaScope.createUniqueIdentifier("mapperC");
		JavaExpression resultMapperC = getMapperCItemConversionExpression(new JavaVariable(mapperCParam, typeUtil.wrap(typeUtil.MAPPER_C, actualItemType)), itemConversion, expectedItemType, listToListLambdaScope);
		JavaType resultType = typeUtil.wrap(typeUtil.MAPPER_LIST_OF_LISTS, expectedItemType);
		return JavaExpression.from(
				out -> out.write(expression, ".<", expectedItemType, ">mapListToList(", mapperCParam, " -> ", resultMapperC.toLambdaBody(), ")"),
				resultType);
	}

	private IllegalArgumentException unexpectedCaseException(JavaType actual, JavaType expected) {
		return new IllegalArgumentException("Cannot coerce from " + actual + " to " + expected + ".");
	}
	private IllegalArgumentException unexpectedWrapperException(JavaType expectedWrapper) {
		return new IllegalArgumentException("Cannot wrap to " + expectedWrapper + ".");
	}
}
