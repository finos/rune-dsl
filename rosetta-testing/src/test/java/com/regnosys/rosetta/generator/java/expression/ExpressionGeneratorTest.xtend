package com.regnosys.rosetta.generator.java.expression

import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCardinality
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaIntLiteral
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.eclipse.emf.common.util.ECollections
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import com.regnosys.rosetta.rosetta.expression.ModifiableBinaryOperation
import com.regnosys.rosetta.rosetta.expression.CardinalityModifier
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation
import com.regnosys.rosetta.rosetta.expression.EqualityOperation
import com.regnosys.rosetta.rosetta.expression.LogicalOperation
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.rosetta.TypeCall
import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.tests.util.ExpressionParser
import com.rosetta.util.DottedPath
import javax.inject.Inject
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable
import com.regnosys.rosetta.generator.ImplicitVariableRepresentation
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import java.math.BigInteger
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.rosetta.util.types.JavaType
import java.util.List
import java.util.Collection
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.tests.util.ModelHelper

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ExpressionGeneratorTest {
	@Inject extension ExpressionGenerator expressionGenerator
	@Inject extension ExpressionParser
	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeUtil
	@Inject extension JavaTypeTranslator
	@Inject extension RObjectFactory
	@Inject extension ModelHelper
	
	DottedPath testPackageName = DottedPath.of("com", "regnosys", "test")
	
	
	private def void assertJavaCode(String expectedCode, CharSequence expr, JavaType expectedType, List<RosettaModel> context) {
		assertJavaCode(expectedCode, expr, expectedType, context, emptyList)
	}
	private def void assertJavaCode(String expectedCode, CharSequence expr, Class<?> expectedType, List<RosettaModel> context, Collection<? extends CharSequence> attrs) {
		assertJavaCode(expectedCode, expr, JavaType.from(expectedType), context, attrs)
	}
	private def void assertJavaCode(String expectedCode, CharSequence expr, JavaType expectedType, List<RosettaModel> context, Collection<? extends CharSequence> attrs) {
		val attributes = attrs.map[parseAttribute(context)].toList
		val parsedExpr = expr.parseExpression(context, attributes)
		
		val pkg = DottedPath.of("test", "ns")
		val scope = new JavaScope(pkg)
		
		val statements = newArrayList
		statements.addAll(
			attributes
				.map[buildRAttribute]
				.map[new JavaLocalVariableDeclarationStatement(false, toMetaJavaType, scope.createIdentifier(it, name))])
		statements.add(parsedExpr.javaCode(expectedType, scope).completeAsReturn)
		
		val actual = statements.reduce[s1, s2|s1.append(s2)]
		assertEquals(expectedCode, buildClass(pkg, '''«actual»''', scope).replace("package test.ns;", "").trim + System.lineSeparator)
	}
	

	
	@Test
	def void testFeatureCallToIncompatibleRestrictionUsesCorrectGetter() {
		val context = '''
		type Foo:
			attr number (1..1)
		
		type Bar extends Foo:
			restrict attr int (1..1)
		
		func Round:
			inputs:
				inp number (1..1)
			output:
				result int (1..1)
		'''.parseRosettaWithNoIssues
		
		val expected = '''
		
		'''
		
		assertJavaCode(expected, '''bar -> attr + Round(bar -> attr)''', Integer, #[context], #["bar Bar (1..1)"])	
		
	}
	
	@Test
	def void shouldEscapeStrings() {
		val scope = new JavaScope(testPackageName)
		
		val gen = 
		'''"Hello \"world\"!"'''
			.parseExpression
			.javaCode(STRING, scope)
			.formatGeneratedFunction(scope)
		
		assertEquals(
			'''
			return "Hello \"world\"!";
			'''.toString,
			gen
		)
	}
	
	/**
	 *  Foo -> attr1 > 5
	 */
	@Test
	def void shouldGenerateGreaterThanExpression() {
		val lhsMockClass = createData("Foo")
		val lhsFeatureCall = createItemFeatureCall(lhsMockClass, "attr1", "number")
		
		val rhsIntLiteral = createIntLiteral(5)
		
		val comparisonOp = createModifiableBinaryOperation(">", lhsFeatureCall, rhsIntLiteral, ComparisonOperation)
		
		val scope = new JavaScope(testPackageName)
		scope.createIdentifier(new ImplicitVariableRepresentation(lhsMockClass), "foo")
		val generatedFunction = expressionGenerator.javaCode(comparisonOp, COMPARISON_RESULT, scope)
		
		assertNotNull(generatedFunction)
		assertEquals(
			'''
			import com.rosetta.model.lib.expression.CardinalityOperator;
			import com.rosetta.model.lib.mapper.MapperS;
			import java.math.BigDecimal;
			
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;
			
			return greaterThan(MapperS.of(foo).<BigDecimal>map("getAttr1", _foo -> _foo.getAttr1()), MapperS.of(BigDecimal.valueOf(5)), CardinalityOperator.All);
			'''.toString,
			formatGeneratedFunction(generatedFunction, scope)
		)
	}

	/**
	 *  Foo -> attr1 > 5 or Foo -> attr2 > 5
	 */
	@Test
	def void shouldGenerateGreaterThanExpressionsWithOr1() {
		val mockClass = createData("Foo")
		
		val lhsFeatureCall = createItemFeatureCall(mockClass, "attr1", "number")
		val lhsComparisonOp = createModifiableBinaryOperation(">", lhsFeatureCall, createIntLiteral(5), ComparisonOperation)
		
		val rhsFeatureCall = createItemFeatureCall(mockClass, "attr2", "number")
		val rhsComparisonOp = createModifiableBinaryOperation(">", rhsFeatureCall, createIntLiteral(5), ComparisonOperation)
		
		val orOp = createBinaryOperation("or", lhsComparisonOp, rhsComparisonOp, LogicalOperation)
		
		val scope = new JavaScope(testPackageName)
		scope.createIdentifier(new ImplicitVariableRepresentation(mockClass), "foo")
		val generatedFunction = expressionGenerator.javaCode(orOp, COMPARISON_RESULT, scope)
		
		assertNotNull(generatedFunction)
		assertEquals(
			'''
			import com.rosetta.model.lib.expression.CardinalityOperator;
			import com.rosetta.model.lib.mapper.MapperS;
			import java.math.BigDecimal;
			
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;
			
			return greaterThan(MapperS.of(foo).<BigDecimal>map("getAttr1", _foo -> _foo.getAttr1()), MapperS.of(BigDecimal.valueOf(5)), CardinalityOperator.All).or(greaterThan(MapperS.of(foo).<BigDecimal>map("getAttr2", _foo -> _foo.getAttr2()), MapperS.of(BigDecimal.valueOf(5)), CardinalityOperator.All));
			'''.toString,
			formatGeneratedFunction(generatedFunction, scope)
		)
	}
	
	/**
	 *  Foo -> attr exists
	 */
	@Test
	def void shouldGenerateExistsExpression() {
		val lhsMockClass = createData("Foo")
		val lhsFeatureCall = createItemFeatureCall(lhsMockClass, "attr", "number")
		val lhsExistsOp = createExistsExpression(lhsFeatureCall)
		
		val scope = new JavaScope(testPackageName)
		scope.createIdentifier(new ImplicitVariableRepresentation(lhsMockClass), "foo")
		val generatedFunction = expressionGenerator.javaCode(lhsExistsOp, COMPARISON_RESULT, scope)
		
		assertNotNull(generatedFunction)
		assertEquals(
			'''
			import com.rosetta.model.lib.mapper.MapperS;
			import java.math.BigDecimal;
			
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;
			
			return exists(MapperS.of(foo).<BigDecimal>map("getAttr", _foo -> _foo.getAttr()));
			'''.toString,
			formatGeneratedFunction(generatedFunction, scope)
		)
	}

	/**
	 *  Foo -> attr1 exists or Foo -> attr2 exists
	 */
	@Test
	def void shouldGenerateExistsExpressionsWithOr1() {
		val mockClass = createData("Foo")
		
		val lhsFeatureCall = createItemFeatureCall(mockClass, "attr1", "boolean")
		val lhsExistsOp = createExistsExpression(lhsFeatureCall)
		
		val rhsFeatureCall = createItemFeatureCall(mockClass, "attr2", "boolean")
		val rhsExistsOp = createExistsExpression(rhsFeatureCall)
		
		val orOp = createBinaryOperation("or", lhsExistsOp, rhsExistsOp, LogicalOperation)
		
		val scope = new JavaScope(testPackageName)
		scope.createIdentifier(new ImplicitVariableRepresentation(mockClass), "foo")
		val generatedFunction = expressionGenerator.javaCode(orOp, COMPARISON_RESULT, scope)
		
		assertNotNull(generatedFunction)
		assertEquals(
			'''
			import com.rosetta.model.lib.mapper.MapperS;
			
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;
			
			return exists(MapperS.of(foo).<Boolean>map("getAttr1", _foo -> _foo.getAttr1())).or(exists(MapperS.of(foo).<Boolean>map("getAttr2", _foo -> _foo.getAttr2())));
			'''.toString,
			formatGeneratedFunction(generatedFunction, scope)
		)
	}

	// ( Foo -> attr1 = Foo -> attr2 ) or ( Foo -> attr1 = Foo -> attr2 )
	
	@Test
	def void shouldGenerateEqualityExprWithOr() {
		val mockClass = createData("Foo")
		
		val featureCall1 = createItemFeatureCall(mockClass, "attr1", "number")
		val featureCall2 = createItemFeatureCall(mockClass, "attr2", "number")
		
		val lhsEqualsOp = createModifiableBinaryOperation("=", featureCall1, featureCall2, EqualityOperation)
		
		val featureCall3 = createItemFeatureCall(mockClass, "attr3", "string")
		val featureCall4 = createItemFeatureCall(mockClass, "attr4", "string")
		
		val rhsEqualsOp = createModifiableBinaryOperation("=", featureCall3, featureCall4, EqualityOperation)
		
		val orOp = createBinaryOperation("or", lhsEqualsOp, rhsEqualsOp, LogicalOperation)
		
		val scope = new JavaScope(testPackageName)
		scope.createIdentifier(new ImplicitVariableRepresentation(mockClass), "foo")
		val generatedFunction = expressionGenerator.javaCode(orOp, COMPARISON_RESULT, scope)
		
		assertNotNull(generatedFunction)
		assertEquals(
			'''
			import com.rosetta.model.lib.expression.CardinalityOperator;
			import com.rosetta.model.lib.mapper.MapperS;
			import java.math.BigDecimal;
			
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;
			
			return areEqual(MapperS.of(foo).<BigDecimal>map("getAttr1", _foo -> _foo.getAttr1()), MapperS.of(foo).<BigDecimal>map("getAttr2", _foo -> _foo.getAttr2()), CardinalityOperator.All).or(areEqual(MapperS.of(foo).<String>map("getAttr3", _foo -> _foo.getAttr3()), MapperS.of(foo).<String>map("getAttr4", _foo -> _foo.getAttr4()), CardinalityOperator.All));
			'''.toString,
			formatGeneratedFunction(generatedFunction, scope)
		)
	}
	
	@Test
	def void shouldGenerateEnumValueRef() {
		val mockClass = createData("Foo")
		
		val featureCall1 = createItemFeatureCall(mockClass, "attr1", "number")
		val featureCall2 = createItemFeatureCall(mockClass, "attr2", "number")
		
		val lhsEqualsOp = createModifiableBinaryOperation("=", featureCall1, featureCall2, EqualityOperation)
		
		val featureCall3 = createItemFeatureCall(mockClass, "attr3", "string")
		val featureCall4 = createItemFeatureCall(mockClass, "attr4", "string")
		
		val rhsEqualsOp = createModifiableBinaryOperation("=", featureCall3, featureCall4, EqualityOperation)
		
		val orOp = createBinaryOperation("or", lhsEqualsOp, rhsEqualsOp, LogicalOperation)
		
		val scope = new JavaScope(testPackageName)
		scope.createIdentifier(new ImplicitVariableRepresentation(mockClass), "foo")
		val generatedFunction = expressionGenerator.javaCode(orOp, COMPARISON_RESULT, scope)
		
		assertNotNull(generatedFunction)
		assertEquals(
			'''
			import com.rosetta.model.lib.expression.CardinalityOperator;
			import com.rosetta.model.lib.mapper.MapperS;
			import java.math.BigDecimal;
			
			import static com.rosetta.model.lib.expression.ExpressionOperators.*;
			
			return areEqual(MapperS.of(foo).<BigDecimal>map("getAttr1", _foo -> _foo.getAttr1()), MapperS.of(foo).<BigDecimal>map("getAttr2", _foo -> _foo.getAttr2()), CardinalityOperator.All).or(areEqual(MapperS.of(foo).<String>map("getAttr3", _foo -> _foo.getAttr3()), MapperS.of(foo).<String>map("getAttr4", _foo -> _foo.getAttr4()), CardinalityOperator.All));
			'''.toString,
			formatGeneratedFunction(generatedFunction, scope)
		)
	}
	
	private def String formatGeneratedFunction(JavaStatementBuilder generatedFunction, JavaScope topScope) {
		buildClass(testPackageName, '''«generatedFunction.completeAsReturn»''', topScope).replace("package " + testPackageName + ";", "").trim + System.lineSeparator
	}

	// Mock utils
	 
	private def RosettaExistsExpression createExistsExpression(RosettaExpression argument) {
		val mockExistsExpression = mock(RosettaExistsExpression); 
		when(mockExistsExpression.argument).thenReturn(argument);
		return mockExistsExpression;
	}
	
 	private def <T extends RosettaBinaryOperation> T createBinaryOperation(String operator, RosettaExpression left, RosettaExpression right, Class<T> op) {
		val mockBinaryOperation = mock(op) 
		when(mockBinaryOperation.operator).thenReturn(operator)
		when(mockBinaryOperation.left).thenReturn(left)
		when(mockBinaryOperation.right).thenReturn(right)
		return mockBinaryOperation
	}
	
	private def <T extends ModifiableBinaryOperation> T createModifiableBinaryOperation(String operator, RosettaExpression left, RosettaExpression right, Class<T> op) {
		val mockBinaryOperation = createBinaryOperation(operator, left, right, op)
		when(mockBinaryOperation.cardMod).thenReturn(CardinalityModifier.NONE)
		return mockBinaryOperation
	}
	
	private def RosettaFeatureCall createItemFeatureCall(Data rosettaClass, String attributeName, String attributeType) {
		val mockReference = mock(RosettaImplicitVariable)
		when(mockReference.name).thenReturn("item")
		when(mockReference.eContainer).thenReturn(rosettaClass)

		val mockCardinality = mock(RosettaCardinality)
		when(mockCardinality.sup).thenReturn(1)
		
		val mockBasicType = mock(RosettaBasicType)
		when(mockBasicType.name).thenReturn(attributeType)
		when(mockBasicType.parameters).thenReturn(ECollections.emptyEList)
		
		val mockTypeCall = mock(TypeCall)
		when(mockTypeCall.type).thenReturn(mockBasicType)
		when(mockTypeCall.arguments).thenReturn(ECollections.emptyEList)
		
		val mockAttribute = mock(Attribute)
		when(mockAttribute.name).thenReturn(attributeName)
		when(mockAttribute.card).thenReturn(mockCardinality)
		when(mockAttribute.eContainer).thenReturn(rosettaClass)
		when(mockAttribute.typeCall).thenReturn(mockTypeCall)
		when(mockAttribute.annotations).thenReturn(ECollections.emptyEList)
		
		val mockFeatureCall = mock(RosettaFeatureCall)
		when(mockFeatureCall.feature).thenReturn(mockAttribute)
		when(mockFeatureCall.receiver).thenReturn(mockReference)
		return mockFeatureCall
	}

	private def RosettaIntLiteral createIntLiteral(int value) {
		val mockIntLiteral = mock(RosettaIntLiteral)
		when(mockIntLiteral.value).thenReturn(BigInteger.valueOf(value))
		return mockIntLiteral
	}
	
	private def Data createData(String className) {
		val mockData = mock(Data)
		val model = mock(RosettaModel)
		when(model.name).thenReturn('com.rosetta.test')
		when(mockData.model).thenReturn(model)
		when(mockData.name).thenReturn(className)
		when(mockData.annotations).thenReturn(ECollections.emptyEList)
		return mockData
	}
}
