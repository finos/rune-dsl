package com.regnosys.rosetta.generator.java.expression

import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.InjectWith
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import javax.inject.Inject
import org.junit.jupiter.api.Test
import com.regnosys.rosetta.generator.java.statement.JavaExpression
import com.rosetta.util.types.JavaType
import com.rosetta.util.types.JavaParameterizedType
import com.rosetta.util.types.JavaClass
import java.math.BigDecimal
import com.regnosys.rosetta.generator.java.JavaScope
import com.rosetta.util.DottedPath
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import static org.junit.jupiter.api.Assertions.*
import java.math.BigInteger
import com.rosetta.model.lib.mapper.MapperS
import java.util.List
import com.rosetta.model.lib.mapper.MapperC
import com.rosetta.model.lib.expression.ComparisonResult
import java.util.Arrays
import org.eclipse.xtend2.lib.StringConcatenationClient

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class TypeCoercionTest {
	@Inject TypeCoercionService coercionService
	@Inject extension ImportManagerExtension
	
	private def JavaType wrapper(Class<?> wrapperType, Class<?> itemType) {
		new JavaParameterizedType(JavaClass.from(wrapperType), JavaClass.from(itemType))
	}
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
		val scope = new JavaScope(pkg)
		
		val coercedExpr = coercionService.addCoercions(JavaExpression.from(expr, actual), expected, scope)
		assertEquals(expectedCode, buildClass(pkg, '''«coercedExpr.completeAsReturn»''', scope).replace("package test.ns;", "").trim + System.lineSeparator)
	}
	
	@Test
	def void testItemToItemConversion() {		
		var String expected
		
		expected = '''
		{
			final Integer integer = 42;
			return integer == null ? null : integer.longValue();
		}
		'''
		assertCoercion(expected, '''42''', Integer, Long)
		
		expected = '''
		import java.math.BigDecimal;
		
		
		{
			final Integer integer = 42;
			return integer == null ? null : BigDecimal.valueOf(integer);
		}
		'''
		assertCoercion(expected, '''42''', Integer, BigDecimal)
		
		expected = '''
		import java.math.BigInteger;
		
		
		{
			final BigInteger bigInteger = 42;
			return bigInteger == null ? null : bigInteger.longValueExact();
		}
		'''
		assertCoercion(expected, '''42''', BigInteger, Long)
		
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
		
		
		{
			final Integer integer = 42;
			return integer == null ? MapperS.ofNull() : MapperS.of(integer);
		}
		'''
		assertCoercion(expected, '''42''', Integer, wrapper(MapperS, Integer))
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperC;
		import java.math.BigDecimal;
		import java.util.Arrays;
		
		
		{
			final BigDecimal bigDecimal = 42;
			return bigDecimal == null ? MapperC.ofNull() : MapperC.of(Arrays.asList(bigDecimal.toBigIntegerExact()));
		}
		'''
		assertCoercion(expected, '''42''', BigDecimal, wrapper(MapperC, BigInteger))
		
		expected = '''
		import java.util.Collections;
		
		
		return Collections.emptyList();
		'''
		assertCoercion(expected, '''null''', Void, wrapper(List, Long))
				
		expected = '''
		import com.rosetta.model.lib.expression.ComparisonResult;
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		{
			final Boolean _boolean = true;
			return _boolean == null ? ComparisonResult.successEmptyOperand("") : ComparisonResult.of(MapperS.of(_boolean));
		}
		'''
		assertCoercion(expected, '''true''', Boolean, ComparisonResult)
	}
	
	@Test
	def void testWrapperToItemConversion() {		
		var String expected
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		return MapperS.of("ABC").get();
		'''
		assertCoercion(expected, '''«MapperS».of("ABC")''', wrapper(MapperS, String), String)
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		{
			final Long _long = MapperS.of(42).get();
			return _long == null ? null : Math.toIntExact(_long);
		}
		'''
		assertCoercion(expected, '''«MapperS».of(42)''', wrapper(MapperS, Long), Integer)
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperC;
		import java.math.BigInteger;
		import java.util.Arrays;
		
		
		{
			final Integer integer = MapperC.of(Arrays.asList(1, 2, 3)).get();
			return integer == null ? null : BigInteger.valueOf(integer);
		}
		'''
		assertCoercion(expected, '''«MapperC».of(«Arrays».asList(1, 2, 3))''', wrapper(MapperC, Integer), BigInteger)
		
		expected = '''
		import com.rosetta.model.lib.expression.ComparisonResult;
		
		
		return ComparisonResult.success().get();
		'''
		assertCoercion(expected, '''«ComparisonResult».success()''', ComparisonResult, Boolean)
		
		expected = '''
		return null;
		'''
		assertCoercion(expected, '''«MapperS».ofNull()''', wrapper(MapperS, Void), Integer)
	}
	
	@Test
	def void testWrapperToWrapperConversion() {		
		var String expected
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.model.lib.mapper.MapperUtils;
		
		
		return MapperUtils.toComparisonResult(MapperS.of(true));
		'''
		assertCoercion(expected, '''«MapperS».of(true)''', wrapper(MapperS, Boolean), ComparisonResult)
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		return MapperS.of(42).map("Type coercion", _long -> _long == null ? null : Math.toIntExact(_long)).getMulti();
		'''
		assertCoercion(expected, '''«MapperS».of(42)''', wrapper(MapperS, Long), wrapper(List, Integer))
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperC;
		import java.math.BigInteger;
		import java.util.Arrays;
		
		
		return MapperC.of(Arrays.asList(1, 2, 3)).map("Type coercion", integer -> BigInteger.valueOf(integer)).getMulti();
		'''
		assertCoercion(expected, '''«MapperC».of(«Arrays».asList(1, 2, 3))''', wrapper(MapperC, Integer), wrapper(List, BigInteger))
		
		expected = '''
		import com.rosetta.model.lib.expression.ComparisonResult;
		
		
		return ComparisonResult.success().asMapper();
		'''
		assertCoercion(expected, '''«ComparisonResult».success()''', ComparisonResult, wrapper(MapperS, Boolean))
		
		expected = '''
		import java.util.Collections;
		
		
		return Collections.emptyList();
		'''
		assertCoercion(expected, '''«MapperS».ofNull()''', wrapper(MapperS, Void), wrapper(List, String))
	}
}