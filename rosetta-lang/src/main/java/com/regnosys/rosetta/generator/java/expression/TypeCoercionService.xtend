package com.regnosys.rosetta.generator.java.expression

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.statement.builder.JavaConditionalExpression
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.rosetta.model.lib.expression.ComparisonResult
import com.rosetta.model.lib.mapper.MapperC
import com.rosetta.model.lib.mapper.MapperS
import com.rosetta.util.types.JavaPrimitiveType
import com.rosetta.util.types.JavaReferenceType
import com.rosetta.util.types.JavaType
import java.math.BigDecimal
import java.math.BigInteger
import java.util.ArrayList
import java.util.Collections
import java.util.Optional
import java.util.function.Function
import java.util.stream.Collectors
import javax.inject.Inject
import com.regnosys.rosetta.generator.java.types.RJavaFieldWithMeta
import com.regnosys.rosetta.generator.java.types.RJavaReferenceWithMeta
import com.rosetta.util.types.RJavaWithMetaValue

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
class TypeCoercionService {
	@Inject extension JavaTypeUtil typeUtil
	
	def JavaStatementBuilder addCoercions(JavaStatementBuilder expr, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType
		if (actual.itemType == JavaReferenceType.NULL_TYPE || actual.itemType.isVoid) {
			return expected.empty
		}

		expr.mapExpression[addCoercions(expected, scope)]
	}

	def JavaStatementBuilder addCoercions(JavaExpression expr, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType
		// Simple coercions
		if (actual.itemType == JavaReferenceType.NULL_TYPE || actual.itemType.isVoid) {
			return expected.empty
		}
		if (actual == expected) {
			return expr
		}
		if (actual == JavaPrimitiveType.VOID) {
			throw new IllegalArgumentException("Cannot coerce from primitive type `void`.")
		}

		// Complex coercions
		if (actual.isWrapper && expected.isWrapper) {
			wrapperToWrapper(expr, expected, scope)
		} else if (actual.isWrapper) {
			wrapperToItem(expr, expected, scope)
		} else if (expected.isWrapper) {
			itemToWrapper(expr, expected, scope)
		} else {
			itemToItem(expr, expected, scope)
		}
	}
	
	private def JavaStatementBuilder itemToItem(JavaExpression expr, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType
		// Strategy:
		// - if no item conversion is needed, return the given expression.
		// - Otherwise, assign last expression to a local variable.
		// - Check if that variable is null:
		//   - if it is null, return null,
		//   - otherwise, convert variable to expected type.
		
		getItemConversion(actual, expected, scope)
			.map[itemConversion|
				
				convertNullSafe(
					expr,
					itemConversion,
					expected,
					scope
				)
			].orElse(expr)
	}
	private def JavaStatementBuilder itemToWrapper(JavaExpression expr, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType
		val expectedItemType = expected.itemType
		
		// Strategy:
		// - assign last expression to a local variable.
		// - check if that variable is null:
		//   - if it is null, return an empty wrapper of the expected type,
		//   - otherwise, convert the variable to the expected item type (if necessary) and then wrap the result to the expected wrapper type.
		
		val wrapConversion = getWrapConversion(expected)
		
		// Exception: wrapping to a MapperS or MapperC is null safe, so no need to do a null check.
		if (expected.extendsMapper) {
			getItemConversion(actual, expectedItemType, scope)
				.map[itemConversion|
					convertNullSafe(
						expr,
						itemConversion.andThen[mapExpression(wrapConversion)],
						expected,
						scope
					)
				].orElse(expr.mapExpression(wrapConversion))
		} else {
			val totalConversion = getItemConversion(actual, expectedItemType, scope)
				.map[itemConversion|
					itemConversion.andThen[mapExpression(wrapConversion)] as Function<JavaExpression, ? extends JavaStatementBuilder>
				].orElse(wrapConversion)
			
			convertNullSafe(
				expr,
				totalConversion,
				expected,
				scope
			)
		}
	}
	private def JavaStatementBuilder wrapperToItem(JavaExpression expr, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType		
		// Strategy:
		// - unwrap the given expression.
		// - perform item to item conversion.
		
		// Special case: mapper to primitive
		if (actual.extendsMapper && expected instanceof JavaPrimitiveType) {
			return expr.mapExpression[
				JavaExpression.from('''«it».getOrDefault(«expected.empty»)''', expected)
			]
		}
		
		val unwrappedExpr = getUnwrapConversion(actual).apply(expr)
		
		itemToItem(unwrappedExpr, expected, scope)
	}
	private def JavaExpression wrapperToWrapper(JavaExpression expr, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType
		val expectedItemType = expected.itemType
		
		// Strategy:
		// - if no conversion is needed, return the given expression.
		// - Otherwise, first convert the item type to the expected item type (if necessary),
		// - then convert the wrapper type to the expected wrapper type (if necessary).
		
		val optionalWrappedItemConversion = getWrappedItemConversion(actual, expectedItemType, scope)
		val optionalWrapperConversion = getWrapperConversion(
			optionalWrappedItemConversion.empty ? actual : actual.changeItemType(expectedItemType),
			expected
		)
		
		if (optionalWrappedItemConversion.isEmpty && optionalWrapperConversion.isEmpty) {
			return expr
		}
		
		val totalConversion = if (optionalWrappedItemConversion.isEmpty) {
				optionalWrapperConversion.orElseThrow
			} else if (optionalWrapperConversion.isEmpty) {
				optionalWrappedItemConversion.orElseThrow
			} else {
				optionalWrapperConversion.orElseThrow.compose(optionalWrappedItemConversion.orElseThrow)
			}
		
		return totalConversion.apply(expr)
	}
	
	private def Optional<Function<JavaExpression, ? extends JavaStatementBuilder>> getItemConversion(JavaType actual, JavaType expected, JavaScope scope) {
		if (actual == expected) {
			return Optional.empty
		}
		
		if (actual.toReferenceType == expected.toReferenceType) {
			// Autoboxing and unboxing
			return Optional.of([it])
		} else if (actual.toReferenceType.extendsNumber && expected.toReferenceType.extendsNumber) {
			// Number type to number type
			return Optional.of([getNumberConversionExpression(it, expected)])
		} else if (actual instanceof RJavaWithMetaValue && expected instanceof RJavaWithMetaValue) {
			return Optional.of([metaToMetaConversionExpression(it, expected as RJavaWithMetaValue, scope)])
		} else if (actual instanceof RJavaWithMetaValue) {
			return Optional.of([metaToItemConversionExpression(it, expected, scope)])
		} else if (expected instanceof RJavaFieldWithMeta || expected instanceof RJavaReferenceWithMeta) {
			return Optional.of([itemToMetaConversionExpression(it, expected as RJavaWithMetaValue, scope)])
		} 
		//TODO: add a case for each of the meta containers
		return Optional.empty
	}
	private def Function<JavaExpression, JavaExpression> getWrapConversion(JavaType wrapperType) {
		if (wrapperType.isList) {
			[getItemToListConversionExpression(it)]
		} else if (wrapperType.isMapperS || wrapperType.isMapper) {
			[getItemToMapperSConversionExpression(it)]
		} else if (wrapperType.isMapperC) {
			[getItemToMapperCConversionExpression(it)]
		} else if (wrapperType.isComparisonResult) {
			[getItemToComparisonResultConversionExpression(it)]
		} else {
			throw unexpectedWrapperException(wrapperType)
		}
	}
	private def Function<JavaExpression, JavaExpression> getUnwrapConversion(JavaType wrapperType) {
		if (wrapperType.isList) {
			[getListToItemConversionExpression(it)]
		} else if (wrapperType.extendsMapper) {
			[getMapperToItemConversionExpression(it)]
		} else {
			throw unexpectedWrapperException(wrapperType)
		}
	}
	private def Optional<Function<JavaExpression, JavaExpression>> getWrappedItemConversion(JavaType actual, JavaType expectedItemType, JavaScope scope) {
		val actualItemType = actual.itemType
		
		getItemConversion(actualItemType, expectedItemType, scope)
			.map[itemConversion|
				if (actual.isList) {
					[getListItemConversionExpression(it, itemConversion, scope)]
				} else if (actual.isMapperS) {
					[getMapperSItemConversionExpression(it, expectedItemType.toReferenceType, itemConversion, scope)]
				} else if (actual.isMapperC) {
					[getMapperCItemConversionExpression(it, itemConversion, scope)]
				} else if (actual.isMapperListOfLists) {
					[getMapperListOfListsItemConversionExpression(it, itemConversion, scope)]
				} else {
					throw unexpectedWrapperException(actual)
				}
			]
	}
	private def Optional<Function<JavaExpression, JavaExpression>> getWrapperConversion(JavaType actual, JavaType expected) {
		return Optional.ofNullable(
			if (actual.isComparisonResult) {
				if (expected.isMapperS) {
					// Case ComparisonResult to MapperS
					[JavaExpression.from('''«it».asMapper()''', MAPPER_S.wrap(BOOLEAN))]
				} else if (expected.isMapperC) {
					// Case ComparisonResult to MapperC
					// Not handled
				} else if (expected.isMapperListOfLists) {
					// Case ComparisonResult to MapperListOfLists
					// Not handled
				} else if (expected.isList) {
					// Case ComparisonResult to List
					[JavaExpression.from('''«it».getMulti()''', LIST.wrap(BOOLEAN))]
				}
			} else if (actual.extendsMapper) {
				if (expected.isComparisonResult) {
					// Case Mapper to ComparisonResult
					[JavaExpression.from('''«ComparisonResult».of(«it»)''', COMPARISON_RESULT)]
				} else if (expected.extendsMapper) {
					if (actual.isMapperS && actual.hasWildcardArgument && expected.isMapperS && !expected.hasWildcardArgument) {
						// Case immutable MapperS<? extends T> to mutable MapperS<T>
						[JavaExpression.from('''«it».map("Make mutable", «Function».identity())''', MAPPER_S.wrap(expected.itemType))]
					} else if (actual.isMapperC && actual.hasWildcardArgument && expected.isMapperC && !expected.hasWildcardArgument) {
						// Case immutable MapperC<? extends T> to mutable MapperC<T>
						[JavaExpression.from('''«it».map("Make mutable", «Function».identity())''', MAPPER_C.wrap(expected.itemType))]
					} else if (actual.isMapperS && expected.isMapperC) {
						// Case MapperS to MapperC
						[JavaExpression.from('''«MapperC».of(«it»)''', MAPPER_C.wrap(expected.itemType))]
					} else if (actual.isMapperC && expected.isMapperS) {
						// Case MapperC to MapperS
						[JavaExpression.from('''«MapperS».of(«it».get())''', MAPPER_S.wrap(expected.itemType))]
					}
				} else if (expected.isMapperListOfLists) {
					// Case Mapper to MapperListOfLists
					// Not handled
				} else if (expected.isList) {
					// Case Mapper to List
					if (actual.hasWildcardArgument && !expected.hasWildcardArgument) {
						[JavaExpression.from('''new «ArrayList»<>(«it».getMulti())''', LIST.wrap(expected.itemType))]
					} else {
						[JavaExpression.from('''«it».getMulti()''', LIST.wrap(expected.itemType))]
					}
				}
			} else if (actual.isMapperListOfLists) {
				// Not handled
			} else if (actual.isList) {
				if (expected.isComparisonResult) {
					// Case List to ComparisonResult
					[JavaExpression.from('''«ComparisonResult».of(«MapperC».of(«it»))''', COMPARISON_RESULT)]
				} else if (expected.isMapperS) {
					// Case List to MapperS
					[JavaExpression.from('''«MapperS».of(«it».get(0))''', MAPPER_S.wrap(expected.itemType))]
				} else if (expected.isMapperC || expected.isMapper) {
					// Case List to MapperC/Mapper
					[JavaExpression.from('''«MapperC».<«expected.itemType»>of(«it»)''', MAPPER_C.wrap(expected.itemType))]
				} else if (expected.isMapperListOfLists) {
					// Case List to MapperListOfLists
					// Not handled
				} else if (expected.isList && actual.hasWildcardArgument && !expected.hasWildcardArgument) {
					// Case immutable List<? extends T> to mutable List<T>
					[JavaExpression.from('''new «ArrayList»(«it»)''', LIST.wrap(expected.itemType))]
				}
			}
		)
	}
	private def JavaStatementBuilder convertNullSafe(JavaExpression expr, Function<JavaExpression, ? extends JavaStatementBuilder> conversion, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType
		if (actual instanceof JavaPrimitiveType) {
			return expr.mapExpression(conversion)
		}
		
		return expr
				.declareAsVariable(true, actual.simpleName.toFirstLower, scope)
				.mapExpression[varExpr|
					conversion.apply(varExpr)
						.mapExpression[
							new JavaConditionalExpression(
							JavaExpression.from('''«varExpr» == null''', JavaPrimitiveType.BOOLEAN),
							expected.empty,
							it,
							typeUtil
						)]
				]
	}
	
	private def JavaExpression empty(JavaType expected) {
		val itemType = expected.itemType
		if (expected.isList) {
			JavaExpression.from('''«Collections».<«itemType»>emptyList()''', LIST.wrap(itemType))
		} else if (expected.isMapperS || expected.isMapper) {
			JavaExpression.from('''«MapperS».<«itemType»>ofNull()''', MAPPER_S.wrap(itemType))
		} else if (expected.isMapperC) {
			JavaExpression.from('''«MapperC».<«itemType»>ofNull()''', MAPPER_C.wrap(itemType))
		} else if (expected.isComparisonResult) {
			JavaExpression.from('''«ComparisonResult».successEmptyOperand("")''', COMPARISON_RESULT)
		} else if (expected == JavaPrimitiveType.BOOLEAN) {
			JavaExpression.from('''false''', JavaPrimitiveType.BOOLEAN)
		} else if (expected instanceof JavaPrimitiveType) {
			throw new IllegalArgumentException("No empty representation for primitive type `" + expected + "`.")
		} else {
			JavaExpression.NULL
		}
	}
	
	private def JavaStatementBuilder metaToMetaConversionExpression(JavaExpression expression, RJavaWithMetaValue expected, JavaScope scope) {
		val actual = expression.expressionType
			if (actual instanceof RJavaWithMetaValue) {
			val valueExpr = JavaExpression.from('''«expression».getValue()''', actual.valueType)
			itemToMetaConversionExpression(valueExpr, expected, scope)
		} else {
			JavaExpression.NULL
		}
	}
	
	/*
	 * 1. Unwrap the meta by calling getValue() on the expression
	 * 2. Map expression to a call to itemToItem(it, expected)
	 */
	
	private def JavaStatementBuilder metaToItemConversionExpression(JavaExpression expression, JavaType expected, JavaScope scope) {
		val actual = expression.expressionType
		if (actual instanceof RJavaWithMetaValue) {
			val valueExpr = JavaExpression.from('''«expression».getValue()''', actual.valueType)
			itemToItem(valueExpr, expected, scope)
		} else {
			JavaExpression.NULL
		}
	}
	
	/*
	 * 1. Check if the expected type is a FieldWithMeta or ReferencWithMeta
	 * 2. Call itemToItem() on the actual type and the getValue() from the expected
	 * 3. Depending on the expected meta type create expression with a ReferenceWithMetaXXXX or FieldWithMetaXXX setting its value 
	 * to the previous expression
	 */
	private def JavaStatementBuilder itemToMetaConversionExpression(JavaExpression expression, RJavaWithMetaValue expected, JavaScope scope) { 
		val expectedValueType = expected.valueType
		val itemExpr = itemToItem(expression, expectedValueType, scope)
		
		if (expected instanceof RJavaFieldWithMeta) {
			val exprReturnType = FIELD_WITH_META.wrap(expectedValueType)
			JavaExpression.from('''FieldWithMeta«expectedValueType».builder().setValue(«itemExpr»).build()''', exprReturnType)
		} else if (expected instanceof RJavaReferenceWithMeta) {
			val exprReturnType = REFERENCE_WITH_META.wrap(expectedValueType)
			JavaExpression.from('''ReferenceWithMeta«expectedValueType».builder().setValue(«itemExpr»).build()''', exprReturnType)
		} else {
			JavaExpression.NULL
		}
		
	}
	
	private def JavaExpression getNumberConversionExpression(JavaExpression expression, JavaType expected) {
		val actual = expression.expressionType
		if (actual.toReferenceType.isInteger) {
			if (expected.toReferenceType.isLong) {
				JavaExpression.from(
					if (actual == JavaPrimitiveType.INT) {
						if (expected == JavaPrimitiveType.LONG) {
							// Case int to long
							'''«expression»'''
						} else {
							// Case int to Long
							'''(long) «expression»'''
						}
					} else {
						// Case Integer to long/Long
						'''«expression».longValue()'''
					}, JavaPrimitiveType.LONG)
			} else if (expected.isBigInteger) {
				// Case int/Integer to BigInteger
				JavaExpression.from('''«BigInteger».valueOf(«expression»)''', BIG_INTEGER)
			} else if (expected.isBigDecimal) {
				// Case int/Integer to BigDecimal
				JavaExpression.from('''«BigDecimal».valueOf(«expression»)''', BIG_DECIMAL)
			} else {
				throw unexpectedCaseException(actual, expected)
			}
		} else if (actual.toReferenceType.isLong) {
			if (expected.toReferenceType.isInteger) {
				// Case long/Long to int/Integer
				JavaExpression.from('''«Math».toIntExact(«expression»)''', JavaPrimitiveType.INT)
			} else if (expected.isBigInteger) {
				// Case long/Long to BigInteger
				JavaExpression.from('''«BigInteger».valueOf(«expression»)''', BIG_INTEGER)
			} else if (expected.isBigDecimal) {
				// Case long/Long to BigDecimal
				JavaExpression.from('''«BigDecimal».valueOf(«expression»)''', BIG_DECIMAL)
			} else {
				throw unexpectedCaseException(actual, expected)
			}
		} else if (actual.isBigInteger) {
			if (expected.toReferenceType.isInteger) {
				// Case BigInteger to int/Integer
				JavaExpression.from('''«expression».intValueExact()''', JavaPrimitiveType.INT)
			} else if (expected.toReferenceType.isLong) {
				// Case BigInteger to long/Long
				JavaExpression.from('''«expression».longValueExact()''', JavaPrimitiveType.LONG)
			} else if (expected.isBigDecimal) {
				// Case BigInteger to BigDecimal
				JavaExpression.from('''new «BigDecimal»(«expression»)''', BIG_DECIMAL)
			} else {
				throw unexpectedCaseException(actual, expected)
			}
		} else if (actual.isBigDecimal) {
			if (expected.toReferenceType.isInteger) {
				// Case BigDecimal to int/Integer
				JavaExpression.from('''«expression».intValueExact()''', JavaPrimitiveType.INT)
			} else if (expected.toReferenceType.isLong) {
				// Case BigDecimal to long/Long
				JavaExpression.from('''«expression».longValueExact()''', JavaPrimitiveType.LONG)
			} else if (expected.isBigInteger) {
				// Case BigDecimal to BigInteger
				JavaExpression.from('''«expression».toBigIntegerExact()''', BIG_INTEGER)
			} else {
				throw unexpectedCaseException(actual, expected)
			}
		} else {
			throw unexpectedCaseException(actual, expected)
		}
	}
	private def JavaExpression getItemToListConversionExpression(JavaExpression expression) {
		JavaExpression.from('''«Collections».singletonList(«expression»)''', LIST.wrap(expression.expressionType))
	}
	private def JavaExpression getItemToMapperSConversionExpression(JavaExpression expression) {
		JavaExpression.from('''«MapperS».of(«expression»)''', MAPPER_S.wrap(expression.expressionType))
	}
	private def JavaExpression getItemToMapperCConversionExpression(JavaExpression expression) {
		JavaExpression.from('''«MapperC».of(«Collections».singletonList(«expression»))''', MAPPER_C.wrap(expression.expressionType))
	}
	private def JavaExpression getItemToComparisonResultConversionExpression(JavaExpression expression) {
		JavaExpression.from('''«ComparisonResult».of(«MapperS».of(«expression»))''', COMPARISON_RESULT)
	}
	private def JavaExpression getListToItemConversionExpression(JavaExpression expression) {
		JavaExpression.from('''«MapperC».of(«expression»).get()''', expression.expressionType.itemType)
	}
	private def JavaExpression getMapperToItemConversionExpression(JavaExpression expression) {
		JavaExpression.from('''«expression».get()''', expression.expressionType.itemType)
	}
	private def JavaExpression getListItemConversionExpression(JavaExpression expression, Function<JavaExpression, ? extends JavaStatementBuilder> itemConversion, JavaScope scope) {
		val actualItemType = expression.expressionType.itemType
		val lambdaScope = scope.lambdaScope
		val lambdaParam = lambdaScope.createUniqueIdentifier(actualItemType.simpleName.toFirstLower)
		val resultItem = itemConversion.apply(new JavaVariable(lambdaParam, actualItemType))
		JavaExpression.from(
			'''
			«expression».stream()
				.<«resultItem.expressionType»>map(«lambdaParam» -> «resultItem.toLambdaBody»)
				.collect(«Collectors».toList())
			''',
			LIST.wrap(resultItem.expressionType)
		)
	}
	private def JavaExpression getMapperSItemConversionExpression(JavaExpression expression, JavaReferenceType expectedType, Function<JavaExpression, ? extends JavaStatementBuilder> itemConversion, JavaScope scope) {
		val actualItemType = expression.expressionType.itemType
		val lambdaScope = scope.lambdaScope
		val lambdaParam = lambdaScope.createUniqueIdentifier(actualItemType.simpleName.toFirstLower)
		val inputToItem = new JavaVariable(lambdaParam, actualItemType)
		val resultType = MAPPER_S.wrap(expectedType)
		val resultItemNullSafe = convertNullSafe(inputToItem, itemConversion, expectedType, scope)
		JavaExpression.from(
			'''«expression».<«resultType.itemType»>map("Type coercion", «lambdaParam» -> «resultItemNullSafe.toLambdaBody»)''',
			resultType
		)
	}
	private def JavaExpression getMapperCItemConversionExpression(JavaExpression expression, Function<JavaExpression, ? extends JavaStatementBuilder> itemConversion, JavaScope scope) {
		val actualItemType = expression.expressionType.itemType
		val lambdaScope = scope.lambdaScope
		val lambdaParam = lambdaScope.createUniqueIdentifier(actualItemType.simpleName.toFirstLower)
		val resultItem = itemConversion.apply(new JavaVariable(lambdaParam, actualItemType))
		val resultType = MAPPER_C.wrap(resultItem.expressionType)
		JavaExpression.from(
			'''«expression».<«resultType.itemType»>map("Type coercion", «lambdaParam» -> «resultItem.toLambdaBody»)''',
			resultType
		)
	}
	private def JavaExpression getMapperListOfListsItemConversionExpression(JavaExpression expression, Function<JavaExpression, ? extends JavaStatementBuilder> itemConversion, JavaScope scope) {
		val actualItemType = expression.expressionType.itemType
		val listToListLambdaScope = scope.lambdaScope
		val mapperCParam = listToListLambdaScope.createUniqueIdentifier("mapperC")
		val resultMapperC = getMapperCItemConversionExpression(new JavaVariable(mapperCParam, MAPPER_C.wrap(actualItemType)), itemConversion, listToListLambdaScope)
		val resultType = MAPPER_LIST_OF_LISTS.wrap(resultMapperC.expressionType.itemType)
		JavaExpression.from(
			'''«expression».<«resultType.itemType»>mapListToList(«mapperCParam» -> «resultMapperC.toLambdaBody»)''',
			resultType
		)
	}
	
	private def unexpectedCaseException(JavaType actual, JavaType expected) {
		new IllegalArgumentException("Cannot coerce from " + actual + " to " + expected + ".")
	}
	private def unexpectedWrapperException(JavaType expectedWrapper) {
		new IllegalArgumentException("Cannot wrap to " + expectedWrapper + ".")
	}
}
