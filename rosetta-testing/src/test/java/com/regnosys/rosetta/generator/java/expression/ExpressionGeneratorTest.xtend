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
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import com.regnosys.rosetta.rosetta.expression.ModifiableBinaryOperation
import com.regnosys.rosetta.rosetta.expression.CardinalityModifier
import com.regnosys.rosetta.rosetta.expression.ComparisonOperation
import com.regnosys.rosetta.rosetta.expression.EqualityOperation
import com.regnosys.rosetta.rosetta.expression.LogicalOperation
import com.regnosys.rosetta.generator.java.util.ImportingStringConcatenation
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.rosetta.TypeCall
import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.tests.util.ExpressionParser
import com.rosetta.util.DottedPath
import javax.inject.Inject
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable
import com.regnosys.rosetta.generator.ImplicitVariableRepresentation

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ExpressionGeneratorTest {
	@Inject extension ExpressionGenerator expressionGenerator
	@Inject extension ExpressionParser
	
	DottedPath testPackageName = DottedPath.of("com", "regnosys", "test")
	
	@Test
	def void shouldEscapeStrings() {
		val scope = new JavaScope(testPackageName)
		
		val gen = 
		'''"Hello \"world\"!"'''
			.parseExpression
			.javaCode(scope)
			.formatGeneratedFunction(scope)
		
		assertThat(gen, is('''MapperS.of("Hello \"world\"!")'''))
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
		val generatedFunction = expressionGenerator.javaCode(comparisonOp, scope)
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction(generatedFunction, scope),
			is('greaterThan(MapperS.of(foo).<BigDecimal>map(\"getAttr1\", _foo -> _foo.getAttr1()), MapperS.of(Integer.valueOf(5)), CardinalityOperator.All)'))
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
		val generatedFunction = expressionGenerator.javaCode(orOp, scope)
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction(generatedFunction, scope), 
			is('greaterThan(MapperS.of(foo).<BigDecimal>map(\"getAttr1\", _foo -> _foo.getAttr1()), MapperS.of(Integer.valueOf(5)), CardinalityOperator.All).or(greaterThan(MapperS.of(foo).<BigDecimal>map(\"getAttr2\", _foo -> _foo.getAttr2()), MapperS.of(Integer.valueOf(5)), CardinalityOperator.All))'))
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
		val generatedFunction = expressionGenerator.javaCode(lhsExistsOp, scope)
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction(generatedFunction, scope), 
			is('exists(MapperS.of(foo).<BigDecimal>map(\"getAttr\", _foo -> _foo.getAttr()))'))
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
		val generatedFunction = expressionGenerator.javaCode(orOp, scope)
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction(generatedFunction, scope), 
			is('exists(MapperS.of(foo).<Boolean>map(\"getAttr1\", _foo -> _foo.getAttr1())).or(exists(MapperS.of(foo).<Boolean>map(\"getAttr2\", _foo -> _foo.getAttr2())))'))
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
		val generatedFunction = expressionGenerator.javaCode(orOp, scope)
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction(generatedFunction, scope), 
			is('areEqual(MapperS.of(foo).<BigDecimal>map(\"getAttr1\", _foo -> _foo.getAttr1()), MapperS.of(foo).<BigDecimal>map(\"getAttr2\", _foo -> _foo.getAttr2()), CardinalityOperator.All).or(areEqual(MapperS.of(foo).<String>map(\"getAttr3\", _foo -> _foo.getAttr3()), MapperS.of(foo).<String>map(\"getAttr4\", _foo -> _foo.getAttr4()), CardinalityOperator.All))'))
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
		val generatedFunction = expressionGenerator.javaCode(orOp, scope)
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction(generatedFunction, scope), 
			is('areEqual(MapperS.of(foo).<BigDecimal>map(\"getAttr1\", _foo -> _foo.getAttr1()), MapperS.of(foo).<BigDecimal>map(\"getAttr2\", _foo -> _foo.getAttr2()), CardinalityOperator.All).or(areEqual(MapperS.of(foo).<String>map(\"getAttr3\", _foo -> _foo.getAttr3()), MapperS.of(foo).<String>map(\"getAttr4\", _foo -> _foo.getAttr4()), CardinalityOperator.All))'))
	}
	
	private def String formatGeneratedFunction(StringConcatenationClient generatedFunction, JavaScope topScope) {
		val isc = new ImportingStringConcatenation(topScope)
		val resolvedCode = isc.preprocess(generatedFunction)
		isc.append(resolvedCode)
		isc.toString.replace('\n','').replace('\t','')
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
		when(mockIntLiteral.value).thenReturn(value)
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
