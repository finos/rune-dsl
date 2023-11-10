package com.regnosys.rosetta.generator.java.expression

import com.rosetta.util.types.JavaType
import java.math.BigInteger
import java.math.BigDecimal
import com.regnosys.rosetta.generator.java.JavaScope
import com.rosetta.util.types.JavaClass
import java.util.List
import com.rosetta.util.types.JavaParameterizedType
import com.rosetta.util.types.JavaWildcardTypeArgument
import com.rosetta.util.types.JavaReferenceType
import java.util.Optional
import java.util.Collections
import java.util.Arrays
import java.util.stream.Collectors
import com.rosetta.model.lib.mapper.Mapper
import com.rosetta.model.lib.mapper.MapperS
import com.rosetta.model.lib.mapper.MapperC
import com.rosetta.model.lib.expression.ComparisonResult
import com.rosetta.model.lib.mapper.MapperListOfLists
import java.util.function.Function
import com.regnosys.rosetta.generator.java.statement.JavaStatementBuilder
import com.regnosys.rosetta.generator.java.statement.JavaExpression
import com.rosetta.util.types.JavaPrimitiveType
import com.rosetta.model.lib.mapper.MapperUtils
import com.regnosys.rosetta.generator.java.statement.JavaVariable

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
 * Item to item coercions and item to wrapper coercions are performed null-safe.
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
 * For a precise definition, see the methods `TypeCoercionService#isWrapper` and `TypeCoercionService#getItemType`.
 */
class TypeCoercionService {
	def JavaStatementBuilder addCoercions(JavaStatementBuilder expr, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType
		if (actual.itemType.isVoid) {
			return expected.empty
		}
		if (actual == expected) {
			return expr
		}
		
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
	
	private def JavaStatementBuilder itemToItem(JavaStatementBuilder expr, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType
		// Strategy:
		// - if no item conversion is needed, return the given expression.
		// - Otherwise, assign last expression to a local variable.
		// - Check if that variable is null:
		//   - if it is null, return null,
		//   - otherwise, convert variable to expected type.
		
		getItemConversion(actual, expected)
			.map[itemConversion|
				convertNullSafe(
					expr,
					itemConversion,
					expected,
					scope
				)
			].orElse(expr)
	}
	private def JavaStatementBuilder itemToWrapper(JavaStatementBuilder expr, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType
		val expectedItemType = expected.itemType
		
		// Strategy:
		// - assign last expression to a local variable.
		// - check if that variable is null:
		//   - if it is null, return an empty wrapper of the expected type,
		//   - otherwise, convert the variable to the expected item type (if necessary) and then wrap the result to the expected wrapper type.
		
		val wrapConversion = getWrapConversion(expected)
		
		// Exception: wrapping to a mapper is null safe, so no need to do a null check.
		if (expected.extendsMapper) {
			getItemConversion(actual, expectedItemType)
				.map[itemConversion|
					convertNullSafe(
						expr,
						wrapConversion.compose(itemConversion),
						expected,
						scope
					)
				].orElse(expr.mapExpression(wrapConversion))
		} else {
			val totalConversion = getItemConversion(actual, expectedItemType)
				.map[itemConversion|
					wrapConversion.compose(itemConversion)
				].orElse(wrapConversion)
			
			convertNullSafe(
				expr,
				totalConversion,
				expected,
				scope
			)
		}
	}
	private def JavaStatementBuilder wrapperToItem(JavaStatementBuilder expr, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType		
		// Strategy:
		// - unwrap the given expression.
		// - perform item to item conversion.
		
		val unwrappedExpr = expr.mapExpression(getUnwrapConversion(actual))
		
		itemToItem(unwrappedExpr, expected, scope)
	}
	private def JavaStatementBuilder wrapperToWrapper(JavaStatementBuilder expr, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType
		val expectedItemType = expected.itemType
		
		// Strategy:
		// - if no conversion is needed, return the given expression.
		// - Otherwise, first convert the item type to the expected item type (if necessary),
		// - then convert the wrapper type to the expected wrapper type (if necessary).
		
		val optionalWrappedItemConversion = getWrappedItemConversion(actual, expectedItemType, scope)
		val optionalWrapperConversion = getWrapperConversion(actual, expected)
		
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
		
		return expr.mapExpression(totalConversion)
	}
	
	private def Optional<Function<JavaExpression, JavaExpression>> getItemConversion(JavaType actual, JavaType expected) {
		if (actual == expected) {
			return Optional.empty
		}
		
		if (actual.extendsNumber && expected.extendsNumber) {
			return Optional.of([getNumberConversionExpression(it, actual, expected)])
		}
		
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
		
		getItemConversion(actualItemType, expectedItemType)
			.map[itemConversion|
				if (actual.isList) {
					[getListItemConversionExpression(it, itemConversion, scope)]
				} else if (actual.isMapperS) {
					[getMapperSItemConversionExpression(it, itemConversion, scope)]
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
					[JavaExpression.from('''«it».asMapper()''', expected)]
				} else if (expected.isMapperC) {
					// Case ComparisonResult to MapperC
					// Not handled
				} else if (expected.isMapperListOfLists) {
					// Case ComparisonResult to MapperListOfLists
					// Not handled
				} else if (expected.isList) {
					// Case ComparisonResult to List
					[JavaExpression.from('''«it».getMulti()''', expected)]
				}
			} else if (actual.extendsMapper) {
				if (expected.isComparisonResult) {
					// Case Mapper to ComparisonResult
					[JavaExpression.from('''«MapperUtils».toComparisonResult(«it»)''', expected)]
				} else if (expected.extendsMapper) {
					// Case Mapper to other Mapper
					// Not handled for the case of differing Mapper types
				} else if (expected.isMapperListOfLists) {
					// Case Mapper to MapperListOfLists
					// Not handled
				} else if (expected.isList) {
					// Case Mapper to List
					[JavaExpression.from('''«it».getMulti()''', expected)]
				}
			} else if (actual.isMapperListOfLists) {
				// Not handled
			} else if (actual.isList) {
				if (expected.isComparisonResult) {
					// Case List to ComparisonResult
					[JavaExpression.from('''«ComparisonResult».of(«it»)''', expected)]
				} else if (expected.isMapperS) {
					// Case List to MapperS
					[JavaExpression.from('''«ComparisonResult».of(«it»).asMapper()''', expected)]
				} else if (expected.isMapperC || expected.isMapper) {
					// Case List to MapperC/Mapper
					[JavaExpression.from('''«MapperC».of(«it»)''', expected)]
				} else if (expected.isMapperListOfLists) {
					// Case List to MapperListOfLists
					// Not handled
				}
			}
		)
	}
	private def JavaStatementBuilder convertNullSafe(JavaStatementBuilder expr, Function<JavaExpression, JavaExpression> conversion, JavaType expected, JavaScope scope) {
		val actual = expr.expressionType
		return expr
				.declareAsVariable(true, actual.simpleName.toFirstLower, scope)
				.mapExpression[JavaExpression.from('''«it» == null ? «expected.empty» : «conversion.apply(it)»''', expected)]
	}
	
	private def JavaExpression empty(JavaType expected) {
		JavaExpression.from(
			if (expected.isList) {
				'''«Collections».emptyList()'''
			} else if (expected.isMapperS || expected.isMapper) {
				'''«MapperS».ofNull()'''
			} else if (expected.isMapperC) {
				'''«MapperC».ofNull()'''
			} else if (expected.isComparisonResult) {
				'''«ComparisonResult».successEmptyOperand("")'''
			} else {
				'''null'''
			},
			expected
		)
	}
	
	private def JavaExpression getNumberConversionExpression(JavaExpression expression, JavaType actual, JavaType expected) {
		JavaExpression.from(
			if (actual.isInteger) {
				if (expected.isLong) {
					// Case Integer to Long
					'''«expression».longValue()'''
				} else if (expected.isBigInteger) {
					// Case Integer to BigInteger
					'''«BigInteger».valueOf(«expression»)'''
				} else if (expected.isBigDecimal) {
					// Case Integer to BigDecimal
					'''«BigDecimal».valueOf(«expression»)'''
				} else {
					throw unexpectedCaseException(actual, expected)
				}
			} else if (actual.isLong) {
				if (expected.isInteger) {
					// Case Long to Integer
					'''«Math».toIntExact(«expression»)'''
				} else if (expected.isBigInteger) {
					// Case Long to BigInteger
					'''«BigInteger».valueOf(«expression»)'''
				} else if (expected.isBigDecimal) {
					// Case Long to BigDecimal
					'''«BigDecimal».valueOf(«expression»)'''
				} else {
					throw unexpectedCaseException(actual, expected)
				}
			} else if (actual.isBigInteger) {
				if (expected.isInteger) {
					// Case BigInteger to Integer
					'''«expression».intValueExact()'''
				} else if (expected.isLong) {
					// Case BigInteger to Long
					'''«expression».longValueExact()'''
				} else if (expected.isBigDecimal) {
					// Case BigInteger to BigDecimal
					'''new «BigDecimal»(«expression»)'''
				} else {
					throw unexpectedCaseException(actual, expected)
				}
			} else if (actual.isBigDecimal) {
				if (expected.isInteger) {
					// Case BigDecimal to Integer
					'''«expression».intValueExact()'''
				} else if (expected.isLong) {
					// Case BigDecimal to Long
					'''«expression».longValueExact()'''
				} else if (expected.isBigInteger) {
					// Case BigDecimal to BigInteger
					'''«expression».toBigIntegerExact()'''
				} else {
					throw unexpectedCaseException(actual, expected)
				}
			} else {
				throw unexpectedCaseException(actual, expected)
			},
			actual
		)
	}
	private def JavaExpression getItemToListConversionExpression(JavaExpression expression) {
		JavaExpression.from('''«Arrays».asList(«expression»)''', List.wrap(expression.expressionType))
	}
	private def JavaExpression getItemToMapperSConversionExpression(JavaExpression expression) {
		JavaExpression.from('''«MapperS».of(«expression»)''', MapperS.wrap(expression.expressionType))
	}
	private def JavaExpression getItemToMapperCConversionExpression(JavaExpression expression) {
		JavaExpression.from('''«MapperC».of(«Arrays».asList(«expression»))''', MapperC.wrap(expression.expressionType))
	}
	private def JavaExpression getItemToComparisonResultConversionExpression(JavaExpression expression) {
		JavaExpression.from('''«ComparisonResult».of(«MapperS».of(«expression»))''', JavaType.from(ComparisonResult))
	}
	private def JavaExpression getListToItemConversionExpression(JavaExpression expression) {
		JavaExpression.from('''«expression».get(0)''', expression.expressionType.itemType)
	}
	private def JavaExpression getMapperToItemConversionExpression(JavaExpression expression) {
		JavaExpression.from('''«expression».get()''', expression.expressionType.itemType)
	}
	private def JavaExpression getListItemConversionExpression(JavaExpression expression, Function<JavaExpression, JavaExpression> itemConversion, JavaScope scope) {
		val actualItemType = expression.expressionType.itemType
		val lambdaScope = scope.lambdaScope
		val lambdaParam = lambdaScope.createUniqueIdentifier(actualItemType.simpleName.toFirstLower)
		val resultItem = itemConversion.apply(new JavaVariable(lambdaParam, actualItemType))
		JavaExpression.from(
			'''
			«expression».stream()
				.map(«lambdaParam» -> «resultItem»)
				.collect(«Collectors».toList())
			''',
			List.wrap(resultItem.expressionType)
		)
	}
	private def JavaExpression getMapperSItemConversionExpression(JavaExpression expression, Function<JavaExpression, JavaExpression> itemConversion, JavaScope scope) {
		val actualItemType = expression.expressionType.itemType
		val lambdaScope = scope.lambdaScope
		val lambdaParam = lambdaScope.createUniqueIdentifier(actualItemType.simpleName.toFirstLower)
		val resultItem = itemConversion.apply(new JavaVariable(lambdaParam, actualItemType))
		JavaExpression.from(
			'''«expression».map("Type coercion", «lambdaParam» -> «lambdaParam» == null ? null : «resultItem»)''',
			MapperS.wrap(resultItem.expressionType)
		)
	}
	private def JavaExpression getMapperCItemConversionExpression(JavaExpression expression, Function<JavaExpression, JavaExpression> itemConversion, JavaScope scope) {
		val actualItemType = expression.expressionType.itemType
		val lambdaScope = scope.lambdaScope
		val lambdaParam = lambdaScope.createUniqueIdentifier(actualItemType.simpleName.toFirstLower)
		val resultItem = itemConversion.apply(new JavaVariable(lambdaParam, actualItemType))
		JavaExpression.from(
			'''«expression».map("Type coercion", «lambdaParam» -> «resultItem»)''',
			MapperC.wrap(resultItem.expressionType)
		)
	}
	private def JavaExpression getMapperListOfListsItemConversionExpression(JavaExpression expression, Function<JavaExpression, JavaExpression> itemConversion, JavaScope scope) {
		val actualItemType = expression.expressionType.itemType
		val listToListLambdaScope = scope.lambdaScope
		val mapperCParam = listToListLambdaScope.createUniqueIdentifier("mapperC")
		val resultMapperC = getMapperCItemConversionExpression(new JavaVariable(mapperCParam, MapperC.wrap(actualItemType)), itemConversion, listToListLambdaScope)
		JavaExpression.from(
			'''«expression».mapListToList(«mapperCParam» -> «resultMapperC»)''',
			MapperListOfLists.wrap(resultMapperC.expressionType.itemType)
		
		)
	}
	
	
	private def boolean extendsNumber(JavaType t) {
		if (t instanceof JavaClass) {
			return JavaClass.from(Number).isAssignableFrom(t)
		}
		return false
	}
	private def boolean isInteger(JavaType t) {
		t == JavaType.from(Integer)
	}
	private def boolean isLong(JavaType t) {
		t == JavaType.from(Long)
	}
	private def boolean isBigInteger(JavaType t) {
		t == JavaType.from(BigInteger)
	}
	private def boolean isBigDecimal(JavaType t) {
		t == JavaType.from(BigDecimal)
	}
	
	private def boolean isVoid(JavaType t) {
		t == JavaType.from(Void)
	}
	
	private def boolean isList(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return t.baseType == JavaType.from(List)
		}
		return false
	}
	
	private def boolean extendsMapper(JavaType t) {
		if (JavaClass.from(Mapper).isAssignableFrom(t)) {
			return true
		} else if (t instanceof JavaParameterizedType) {
			return JavaClass.from(Mapper).isAssignableFrom(t.baseType)
		}
		return false
	}
	private def boolean isMapper(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return t.baseType == JavaClass.from(Mapper)
		}
		return false
	}
	private def boolean isMapperS(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return t.baseType == JavaClass.from(MapperS)
		}
		return false
	}
	private def boolean isMapperC(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return t.baseType == JavaClass.from(MapperC)
		}
		return false
	}
	private def boolean isComparisonResult(JavaType t) {
		t == JavaClass.from(ComparisonResult)
	}
	private def boolean isMapperListOfLists(JavaType t) {
		if (t instanceof JavaParameterizedType) {
			return t.baseType == JavaClass.from(MapperListOfLists)
		}
		return false
	}
	
	private def boolean isWrapper(JavaType t) {
		t.isList || t.extendsMapper || t.isMapperListOfLists
	}
	private def JavaType getItemType(JavaType t) {
		if (t.isWrapper) {
			if (t.isComparisonResult) {
				return JavaPrimitiveType.BOOLEAN.toReferenceType
			} else {
				val arg = (t as JavaParameterizedType).arguments.head
				if (arg instanceof JavaWildcardTypeArgument) {
					return arg.bound.orElseThrow
				}
				return arg as JavaReferenceType
			}
		}
		return t
	}
	
	private def JavaType wrap(Class<?> wrapperType, JavaType itemType) {
		new JavaParameterizedType(JavaClass.from(wrapperType), itemType as JavaReferenceType)
	}
	
	private def unexpectedCaseException(JavaType actual, JavaType expected) {
		new IllegalArgumentException("Cannot coerce from " + actual + " to " + expected + ".")
	}
	private def unexpectedWrapperException(JavaType expectedWrapper) {
		new IllegalArgumentException("Cannot wrap to " + expectedWrapper + ".")
	}
}
