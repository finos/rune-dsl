package com.regnosys.rosetta.generator.java.expression

import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.InjectWith
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import javax.inject.Inject
import org.junit.jupiter.api.Test
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.rosetta.util.types.JavaType
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
import com.rosetta.util.types.JavaPrimitiveType
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class TypeCoercionTest {
	@Inject TypeCoercionService coercionService
	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeUtil
	
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
		assertCoercion(expected, '''42''', JavaPrimitiveType.INT, MapperS.wrap(Integer))
		
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
		assertCoercion(expected, '''«BigDecimal».valueOf(42)''', BigDecimal, MapperC.wrap(BigInteger))
		
		expected = '''
		import java.util.Collections;
		
		
		return Collections.<Long>emptyList();
		'''
		assertCoercion(expected, '''null''', Void, List.wrap(Long))
				
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
		assertCoercion(expected, '''«MapperS».of("ABC")''', MapperS.wrap(String), String)
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		{
			final Long _long = MapperS.of(42).get();
			return _long == null ? null : Math.toIntExact(_long);
		}
		'''
		assertCoercion(expected, '''«MapperS».of(42)''', MapperS.wrap(Long), Integer)
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperC;
		import java.math.BigInteger;
		import java.util.Arrays;
		
		
		{
			final Integer integer = MapperC.of(Arrays.asList(1, 2, 3)).get();
			return integer == null ? null : BigInteger.valueOf(integer);
		}
		'''
		assertCoercion(expected, '''«MapperC».of(«Arrays».asList(1, 2, 3))''', MapperC.wrap(Integer), BigInteger)
		
		expected = '''
		import com.rosetta.model.lib.expression.ComparisonResult;
		
		
		return ComparisonResult.success().get();
		'''
		assertCoercion(expected, '''«ComparisonResult».success()''', ComparisonResult, Boolean)
		
		expected = '''
		return null;
		'''
		assertCoercion(expected, '''«MapperS».ofNull()''', MapperS.wrap(Void), Integer)
	}
	
	@Test
	def void testWrapperToWrapperConversion() {		
		var String expected
		
		expected = '''
		import com.rosetta.model.lib.expression.ComparisonResult;
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		return ComparisonResult.of(MapperS.of(true));
		'''
		assertCoercion(expected, '''«MapperS».of(true)''', MapperS.wrap(Boolean), ComparisonResult)
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperS;
		
		
		return MapperS.of(42).<Integer>map("Type coercion", _long -> _long == null ? null : Math.toIntExact(_long)).getMulti();
		'''
		assertCoercion(expected, '''«MapperS».of(42)''', MapperS.wrap(Long), List.wrap(Integer))
		
		expected = '''
		import com.rosetta.model.lib.mapper.MapperC;
		import java.math.BigInteger;
		import java.util.Arrays;
		
		
		return MapperC.of(Arrays.asList(1, 2, 3)).<BigInteger>map("Type coercion", integer -> BigInteger.valueOf(integer)).getMulti();
		'''
		assertCoercion(expected, '''«MapperC».of(«Arrays».asList(1, 2, 3))''', MapperC.wrap(Integer), List.wrap(BigInteger))
		
		expected = '''
		import com.rosetta.model.lib.expression.ComparisonResult;
		
		
		return ComparisonResult.success().asMapper();
		'''
		assertCoercion(expected, '''«ComparisonResult».success()''', ComparisonResult, MapperS.wrap(Boolean))
		
		expected = '''
		import java.util.Collections;
		
		
		return Collections.<String>emptyList();
		'''
		assertCoercion(expected, '''«MapperS».ofNull()''', MapperS.wrap(Void), List.wrap(String))
	}
}