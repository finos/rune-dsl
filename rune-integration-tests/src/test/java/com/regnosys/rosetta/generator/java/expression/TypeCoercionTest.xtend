package com.regnosys.rosetta.generator.java.expression

import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.InjectWith
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import javax.inject.Inject
import org.junit.jupiter.api.Test
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.rosetta.util.types.JavaType
import java.math.BigDecimal
import com.rosetta.util.DottedPath
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import static org.junit.jupiter.api.Assertions.*
import java.math.BigInteger
import com.rosetta.model.lib.mapper.MapperS
import com.rosetta.model.lib.mapper.MapperC
import com.rosetta.model.lib.expression.ComparisonResult
import java.util.Arrays
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.rosetta.util.types.JavaPrimitiveType
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.types.RJavaFieldWithMeta
import com.regnosys.rosetta.generator.java.types.RJavaReferenceWithMeta
import com.regnosys.rosetta.generator.java.scoping.JavaPackageName

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class TypeCoercionTest {
	@Inject TypeCoercionService coercionService
	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeUtil typeUtil
	@Inject ExpressionScopeUtility scopeUtil
	
	private def void assertCoercion(String expectedCode, StringConcatenationClient expr, Class<?> actual, JavaType expected) {
		assertCoercion(expectedCode, expr, JavaType.from(actual), expected)
	}
	private def void assertCoercion(String expectedCode, StringConcatenationClient expr, JavaType actual, Class<?> expected) {
		assertCoercion(expectedCode, expr, actual, JavaType.from(expected))
	}
	private def void assertCoercion(String expectedCode, StringConcatenationClient expr, Class<?> actual, Class<?> expected) {
		assertCoercion(expectedCode, expr, JavaType.from(actual), JavaType.from(expected))
	}
	private def void assertCoercion(String expectedCode, StringConcatenationClient expr, JavaType actual, JavaType expected) {
		val pkg = DottedPath.of("test", "ns")
		val scope = scopeUtil.createTestExpressionScope(pkg)
		
		val coercedExpr = coercionService.addCoercions(JavaExpression.from(expr, actual), expected, scope)
		assertEquals(expectedCode, buildClass(pkg, '''«coercedExpr.completeAsReturn»''', scope.getFileScope()).replace("package test.ns;", "").trim + System.lineSeparator)
	}
	
	@Test
	def void testConvertBigDecimalToFieldWithMetaInteger() {
		val expected = '''
		import java.math.BigDecimal;
		import test.FieldWithMetaInteger;
		
		
		{
			final BigDecimal bigDecimal = BigDecimal.valueOf(10);
			return bigDecimal == null ? FieldWithMetaInteger.builder().build() : FieldWithMetaInteger.builder().setValue(bigDecimal.intValueExact()).build();
		}
		'''
		
		val expectedType = new RJavaFieldWithMeta(INTEGER, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil)
		
		assertCoercion(expected, '''BigDecimal.valueOf(10)''', BigDecimal, expectedType)	
		
	}
	
	
	@Test
	def void testConvertFieldWithMetaIntegerToBigDecimal() {
		val expected = '''
		import java.math.BigDecimal;
		import test.FieldWithMetaInteger;
		
		
		{
			final FieldWithMetaInteger fieldWithMetaInteger = FieldWithMetaInteger.builder().setValue(10).build();
			if (fieldWithMetaInteger == null) {
				return null;
			}
			final Integer integer = fieldWithMetaInteger.getValue();
			return integer == null ? null : BigDecimal.valueOf(integer);
		}
		'''
		
		val actualType = new RJavaFieldWithMeta(INTEGER, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil)
		
		assertCoercion(expected, '''FieldWithMetaInteger.builder().setValue(10).build()''', actualType, BigDecimal)	
	}
	
	@Test
	def void testConvertMetaReferenceToMetaField() {
		val expected = '''
		import test.FieldWithMetaString;
		import test.ReferenceWithMetaString;
		
		
		{
			final ReferenceWithMetaString referenceWithMetaString = ReferenceWithMetaString.builder().setValue("foo").build();
			if (referenceWithMetaString == null) {
				return FieldWithMetaString.builder().build();
			}
			final String string = referenceWithMetaString.getValue();
			return string == null ? FieldWithMetaString.builder().build() : FieldWithMetaString.builder().setValue(string).build();
		}
		'''
		
		val actualType = new RJavaReferenceWithMeta(STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil)
		val expectedType = new RJavaFieldWithMeta(STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil)
		
		assertCoercion(expected, '''ReferenceWithMetaString.builder().setValue("foo").build()''', actualType, expectedType)	
	}
	
	@Test
	def void testConvertMetaFieldToMetaReference() {
		val expected = '''
		import test.FieldWithMetaString;
		import test.ReferenceWithMetaString;
		
		
		{
			final FieldWithMetaString fieldWithMetaString = FieldWithMetaString.builder().setValue("foo").build();
			if (fieldWithMetaString == null) {
				return ReferenceWithMetaString.builder().build();
			}
			final String string = fieldWithMetaString.getValue();
			return string == null ? ReferenceWithMetaString.builder().build() : ReferenceWithMetaString.builder().setValue(string).build();
		}
		'''
		
		val actualType = new RJavaFieldWithMeta(STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil)
		val expectedType = new RJavaReferenceWithMeta(STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil)
		
		assertCoercion(expected, '''FieldWithMetaString.builder().setValue("foo").build()''', actualType, expectedType)	
	}
	
	
	@Test
	def void testConvertStringToMeta() {
		val expected = '''
		import test.FieldWithMetaString;
		
		
		{
			final String string = "foo";
			return string == null ? FieldWithMetaString.builder().build() : FieldWithMetaString.builder().setValue(string).build();
		}
		'''		
		assertCoercion(expected, '''"foo"''', String, new RJavaFieldWithMeta(STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil))
		
		val expected2 = '''
		import test.ReferenceWithMetaString;
		
		
		{
			final String string = "foo";
			return string == null ? ReferenceWithMetaString.builder().build() : ReferenceWithMetaString.builder().setValue(string).build();
		}
		'''
		assertCoercion(expected2, '''"foo"''', String, new RJavaReferenceWithMeta(STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil))		
	}
	
	@Test
	def void testConvertMetaToString() {
		val String expected = '''
		import test.FieldWithMetaString;
		
		
		{
			final FieldWithMetaString fieldWithMetaString = FieldWithMetaString.builder().setValue("foo").build();
			return fieldWithMetaString == null ? null : fieldWithMetaString.getValue();
		}
		'''
						
		assertCoercion(expected, '''FieldWithMetaString.builder().setValue("foo").build()''', new RJavaFieldWithMeta(STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil), String)	
		
		val expected2 = '''
		import test.ReferenceWithMetaString;
		
		
		{
			final ReferenceWithMetaString referenceWithMetaString = ReferenceWithMetaString.builder().setValue("foo").build();;
			return referenceWithMetaString == null ? null : referenceWithMetaString.getValue();
		}
		'''	
				
		assertCoercion(expected2, '''ReferenceWithMetaString.builder().setValue("foo").build();''', new RJavaReferenceWithMeta(STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil), String)
	}
	
	@Test
	def void testItemToItemConversion() {		
		var String expected
		
		expected = '''
		return (long) 42;
		'''
		assertCoercion(expected, '''42''', JavaPrimitiveType.INT, Long)
		
		expected = '''
		import java.math.BigDecimal;
		
		
		{
			final Integer integer = Integer.valueOf(42);
			return integer == null ? null : BigDecimal.valueOf(integer);
		}
		'''
		assertCoercion(expected, '''«Integer».valueOf(42)''', Integer, BigDecimal)
		
		expected = '''
		import java.math.BigInteger;
		
		
		{
			final BigInteger bigInteger = BigInteger.valueOf(42);
			return bigInteger == null ? null : bigInteger.longValueExact();
		}
		'''
		assertCoercion(expected, '''«BigInteger».valueOf(42)''', BigInteger, Long)
		
		expected = '''
		{
			final Boolean _boolean = Boolean.valueOf(true);
			return _boolean == null ? false : _boolean;
		}
		'''
		assertCoercion(expected, '''«Boolean».valueOf(true)''', Boolean, JavaPrimitiveType.BOOLEAN)
		
		expected = '''
		return null;
		'''
		assertCoercion(expected, '''null''', Void, Long)
	}
	
	@Test
	def void testItemToWrapperConversion() {		
		var String expected
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		return MapperS.of(42);
		'''
		assertCoercion(expected, '''42''', JavaPrimitiveType.INT, MAPPER_S.wrap(Integer))
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperC;
		import java.math.BigDecimal;
		import java.math.BigInteger;
		import java.util.Collections;
		
		
		{
			final BigDecimal bigDecimal = BigDecimal.valueOf(42);
			return bigDecimal == null ? MapperC.<BigInteger>ofNull() : MapperC.of(Collections.singletonList(bigDecimal.toBigIntegerExact()));
		}
		'''
		assertCoercion(expected, '''«BigDecimal».valueOf(42)''', BigDecimal, MAPPER_C.wrap(BigInteger))
		
		expected = '''
		import java.util.Collections;
		
		
		return Collections.<Long>emptyList();
		'''
		assertCoercion(expected, '''null''', Void, LIST.wrap(Long))
				
		expected = '''
		import com.rosetta.model.lib.expression.ComparisonResult;
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		return ComparisonResult.of(MapperS.of(Boolean.valueOf(true)));
		'''
		assertCoercion(expected, '''«Boolean».valueOf(true)''', Boolean, ComparisonResult)
	}
	
	@Test
	def void testWrapperToItemConversion() {		
		var String expected
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		return MapperS.of("ABC").get();
		'''
		assertCoercion(expected, '''«MapperS».of("ABC")''', MAPPER_S.wrap(String), String)
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		{
			final Long _long = MapperS.of(42).get();
			return _long == null ? null : Math.toIntExact(_long);
		}
		'''
		assertCoercion(expected, '''«MapperS».of(42)''', MAPPER_S.wrap(Long), Integer)
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperC;
		import java.math.BigInteger;
		import java.util.Arrays;
		
		
		{
			final Integer integer = MapperC.of(Arrays.asList(1, 2, 3)).get();
			return integer == null ? null : BigInteger.valueOf(integer);
		}
		'''
		assertCoercion(expected, '''«MapperC».of(«Arrays».asList(1, 2, 3))''', MAPPER_C.wrap(Integer), BigInteger)
		
		expected = '''
		import com.rosetta.model.lib.expression.ComparisonResult;
		
		
		return ComparisonResult.success().get();
		'''
		assertCoercion(expected, '''«ComparisonResult».success()''', ComparisonResult, Boolean)
		
		expected = '''
		return null;
		'''
		assertCoercion(expected, '''«MapperS».ofNull()''', MAPPER_S.wrap(Void), Integer)
	}
	
	@Test
	def void testWrapperToWrapperConversion() {		
		var String expected
		
		expected = '''
		import com.rosetta.model.lib.expression.ComparisonResult;
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		return ComparisonResult.of(MapperS.of(true));
		'''
		assertCoercion(expected, '''«MapperS».of(true)''', MAPPER_S.wrap(Boolean), ComparisonResult)
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		return MapperS.of(42).<Integer>map("Type coercion", _long -> _long == null ? null : Math.toIntExact(_long)).getMulti();
		'''
		assertCoercion(expected, '''«MapperS».of(42)''', MAPPER_S.wrap(Long), LIST.wrap(Integer))
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperC;
		import java.math.BigInteger;
		import java.util.Arrays;
		
		
		return MapperC.of(Arrays.asList(1, 2, 3)).<BigInteger>map("Type coercion", integer -> BigInteger.valueOf(integer)).getMulti();
		'''
		assertCoercion(expected, '''«MapperC».of(«Arrays».asList(1, 2, 3))''', MAPPER_C.wrap(Integer), LIST.wrap(BigInteger))
		
		expected = '''
		import com.rosetta.model.lib.expression.ComparisonResult;
		
		
		return ComparisonResult.success().asMapper();
		'''
		assertCoercion(expected, '''«ComparisonResult».success()''', ComparisonResult, MAPPER_S.wrap(Boolean))
		
		expected = '''
		import java.util.Collections;
		
		
		return Collections.<String>emptyList();
		'''
		assertCoercion(expected, '''«MapperS».ofNull()''', MAPPER_S.wrap(Void), LIST.wrap(String))
	}
}