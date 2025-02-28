package com.regnosys.rosetta.generator.java.expression

import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.tests.util.ExpressionParser
import com.rosetta.util.DottedPath
import javax.inject.Inject
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.rosetta.util.types.JavaType
import java.util.List
import java.util.Collection
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.tests.util.ModelHelper
import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import org.eclipse.xtend2.lib.StringConcatenationClient

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class ExpressionGeneratorTest {
	@Inject extension ExpressionGenerator expressionGenerator
	@Inject extension ExpressionParser
	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeUtil
	@Inject extension JavaTypeTranslator
	@Inject extension RObjectFactory
	@Inject extension ModelHelper
	@Inject extension JavaDependencyProvider
	@Inject extension JavaIdentifierRepresentationService
	
	
	private def void assertJavaCode(String expectedCode, CharSequence expr, Class<?> expectedType) {
		assertJavaCode(expectedCode, expr, expectedType, emptyList, emptyList)
	}
	private def void assertJavaCode(String expectedCode, CharSequence expr, Class<?> expectedType, List<RosettaModel> context, Collection<? extends CharSequence> attrs) {
		assertJavaCode(expectedCode, expr, JavaType.from(expectedType), context, attrs)
	}
	private def void assertJavaCode(String expectedCode, CharSequence expr, JavaType expectedType, List<RosettaModel> context, Collection<? extends CharSequence> attrs) {
		val attributes = attrs.map[parseAttribute(context)].toList
		val parsedExpr = expr.parseExpression(context, attributes)
		val dependencies = parsedExpr.javaDependencies
		
		val pkg = DottedPath.of("test", "ns")
		val scope = new JavaScope(pkg)
		
		val List<StringConcatenationClient> dependencyStatements = newArrayList
		dependencies
			.map[new JavaLocalVariableDeclarationStatement(false, it, scope.createIdentifier(toDependencyInstance, simpleName.toFirstLower))]
			.forEach[dependencyStatements.add('''@«Inject» «it»''')]
		
		val statements = newArrayList
		statements.addAll(
			attributes
				.map[buildRAttribute]
				.map[new JavaLocalVariableDeclarationStatement(false, toMetaJavaType, scope.createIdentifier(it, name))])
		statements.add(parsedExpr.javaCode(expectedType, scope).completeAsReturn)
		
		val actual = statements.reduce[s1, s2|s1.append(s2)]
		assertEquals(expectedCode, buildClass(pkg, '''«FOR dep : dependencyStatements SEPARATOR "\n" AFTER "\n\n"»«dep»«ENDFOR»«actual»''', scope).replace("package test.ns;", "").trim + System.lineSeparator)
	}
	

	
	@Test
	def void testFeatureCallToIncompatibleOverrideUsesCorrectGetter() {
		val context = '''
		type Foo:
			attr number (1..1)
		
		type Bar extends Foo:
			override attr int (1..1)
		
		func Round:
			inputs:
				inp number (1..1)
			output:
				result int (1..1)
		'''.parseRosettaWithNoIssues
		
		val expected = '''
		import com.rosetta.model.lib.expression.MapperMaths;
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.test.model.Bar;
		import com.rosetta.test.model.functions.Round;
		import java.math.BigDecimal;
		import javax.inject.Inject;
		
		
		@Inject Round round;
		
		{
			Bar bar;
			return MapperMaths.<Integer, Integer, Integer>add(MapperS.of(bar).<Integer>map("getAttr", _bar -> _bar.getAttrOverriddenAsInteger()), MapperS.of(round.evaluate(MapperS.of(bar).<BigDecimal>map("getAttr", _bar -> _bar.getAttr()).get()))).get();
		}
		'''
		
		assertJavaCode(expected, '''bar -> attr + Round(bar -> attr)''', Integer, #[context], #["bar Bar (1..1)"])	
		
	}
	
	@Test
	def void shouldEscapeStrings() {
		val expected = '''
		return "Hello \"world\"!";
		'''
		assertJavaCode(expected, '''"Hello \"world\"!"''', String)
	}
	
	/**
	 *  Foo -> attr1 > 5
	 */
	@Test
	def void shouldGenerateGreaterThanExpression() {
		val context = '''
		type Foo:
			attr1 number (1..1)
		'''.parseRosettaWithNoIssues
		val expected = '''
		import com.rosetta.model.lib.expression.CardinalityOperator;
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.test.model.Foo;
		import java.math.BigDecimal;
		
		import static com.rosetta.model.lib.expression.ExpressionOperators.*;
		
		{
			Foo foo;
			return greaterThan(MapperS.of(foo).<BigDecimal>map("getAttr1", _foo -> _foo.getAttr1()), MapperS.of(BigDecimal.valueOf(5)), CardinalityOperator.All);
		}
		'''
		assertJavaCode(expected, '''foo -> attr1 > 5''', COMPARISON_RESULT, #[context], #["foo Foo (1..1)"])
	}

	/**
	 *  Foo -> attr1 > 5 or Foo -> attr2 > 5
	 */
	@Test
	def void shouldGenerateGreaterThanExpressionsWithOr1() {
		val context = '''
		type Foo:
			attr1 number (1..1)
			attr2 number (1..1)
		'''.parseRosettaWithNoIssues
		val expected = '''
		import com.rosetta.model.lib.expression.CardinalityOperator;
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.test.model.Foo;
		import java.math.BigDecimal;
		
		import static com.rosetta.model.lib.expression.ExpressionOperators.*;
		
		{
			Foo foo;
			return greaterThan(MapperS.of(foo).<BigDecimal>map("getAttr1", _foo -> _foo.getAttr1()), MapperS.of(BigDecimal.valueOf(5)), CardinalityOperator.All).or(greaterThan(MapperS.of(foo).<BigDecimal>map("getAttr2", _foo -> _foo.getAttr2()), MapperS.of(BigDecimal.valueOf(5)), CardinalityOperator.All));
		}
		'''
		assertJavaCode(expected, '''foo -> attr1 > 5 or foo -> attr2 > 5''', COMPARISON_RESULT, #[context], #["foo Foo (1..1)"])
	}
	
	/**
	 *  Foo -> attr exists
	 */
	@Test
	def void shouldGenerateExistsExpression() {
		val context = '''
		type Foo:
			attr number (0..1)
		'''.parseRosettaWithNoIssues
		val expected = '''
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.test.model.Foo;
		import java.math.BigDecimal;
		
		import static com.rosetta.model.lib.expression.ExpressionOperators.*;
		
		{
			Foo foo;
			return exists(MapperS.of(foo).<BigDecimal>map("getAttr", _foo -> _foo.getAttr()));
		}
		'''
		assertJavaCode(expected, '''foo -> attr exists''', COMPARISON_RESULT, #[context], #["foo Foo (1..1)"])
	}
	
	@Test
	def void shouldGenerateEnumValueRef() {		
		val context = '''
		type Foo:
			attr1 number (0..1)
			attr2 number (0..1)
			attr3 string (0..1)
			attr4 string (0..1)
		'''.parseRosettaWithNoIssues
		val expected = '''
		import com.rosetta.model.lib.expression.CardinalityOperator;
		import com.rosetta.model.lib.mapper.MapperS;
		import com.rosetta.test.model.Foo;
		import java.math.BigDecimal;
		
		import static com.rosetta.model.lib.expression.ExpressionOperators.*;
		
		{
			Foo foo;
			return areEqual(MapperS.of(foo).<BigDecimal>map("getAttr1", _foo -> _foo.getAttr1()), MapperS.of(foo).<BigDecimal>map("getAttr2", _foo -> _foo.getAttr2()), CardinalityOperator.All).or(areEqual(MapperS.of(foo).<String>map("getAttr3", _foo -> _foo.getAttr3()), MapperS.of(foo).<String>map("getAttr4", _foo -> _foo.getAttr4()), CardinalityOperator.All));
		}
		'''
		assertJavaCode(expected, '''(foo -> attr1 = foo -> attr2) or (foo -> attr3 = foo -> attr4)''', COMPARISON_RESULT, #[context], #["foo Foo (1..1)"])
	}
}
