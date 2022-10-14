package com.regnosys.rosetta.generator.java.expression

import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator.ParamMap
import com.regnosys.rosetta.generator.java.util.ImportingStringConcatination
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaCallableCall
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

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ExpressionGeneratorTest {
	@Inject ExpressionGenerator expressionHandler
	
	/**
	 *  Foo -> attr1 > 5
	 */
	@Test
	def void shouldGenerateGreaterThanExpression() {
		val lhsMockClass = createData("Foo")
		val lhsFeatureCall = createFeatureCall(lhsMockClass, "attr1")
		
		val rhsIntLiteral = createIntLiteral(5)
		
		val comparisonOp = createModifiableBinaryOperation(">", lhsFeatureCall, rhsIntLiteral, ComparisonOperation)
		
		val generatedFunction = expressionHandler.javaCode(comparisonOp, new ParamMap(lhsMockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('greaterThan(MapperS.of(foo).<Foo>map(\"getAttr1\", _foo -> _foo.getAttr1()), MapperS.of(Integer.valueOf(5)), CardinalityOperator.All)'))
	}

	/**
	 *  Foo -> attr1 > 5 or Foo -> attr2 > 5
	 */
	@Test
	def void shouldGenerateGreaterThanExpressionsWithOr1() {
		val mockClass = createData("Foo")
		
		val lhsFeatureCall = createFeatureCall(mockClass, "attr1")
		val lhsComparisonOp = createModifiableBinaryOperation(">", lhsFeatureCall, createIntLiteral(5), ComparisonOperation)
		
		val rhsFeatureCall = createFeatureCall(mockClass, "attr2")
		val rhsComparisonOp = createModifiableBinaryOperation(">", rhsFeatureCall, createIntLiteral(5), ComparisonOperation)
		
		val orOp = createBinaryOperation("or", lhsComparisonOp, rhsComparisonOp, LogicalOperation)
		
		val generatedFunction = expressionHandler.javaCode(orOp, new ParamMap(mockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('greaterThan(MapperS.of(foo).<Foo>map(\"getAttr1\", _foo -> _foo.getAttr1()), MapperS.of(Integer.valueOf(5)), CardinalityOperator.All).or(greaterThan(MapperS.of(foo).<Foo>map(\"getAttr2\", _foo -> _foo.getAttr2()), MapperS.of(Integer.valueOf(5)), CardinalityOperator.All))'))
	}
	
	/**
	 *  Foo -> attr exists
	 */
	@Test
	def void shouldGenerateExistsExpression() {
		val lhsMockClass = createData("Foo")
		val lhsFeatureCall = createFeatureCall(lhsMockClass, "attr")
		val lhsExistsOp = createExistsExpression(lhsFeatureCall)
		
		val generatedFunction = expressionHandler.javaCode(lhsExistsOp, new ParamMap(lhsMockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('exists(MapperS.of(foo).<Foo>map(\"getAttr\", _foo -> _foo.getAttr()))'))
	}

	/**
	 *  Foo -> attr1 exists or Foo -> attr2 exists
	 */
	@Test
	def void shouldGenerateExistsExpressionsWithOr1() {
		val mockClass = createData("Foo")
		
		val lhsFeatureCall = createFeatureCall(mockClass, "attr1")
		val lhsExistsOp = createExistsExpression(lhsFeatureCall)
		
		val rhsFeatureCall = createFeatureCall(mockClass, "attr2")
		val rhsExistsOp = createExistsExpression(rhsFeatureCall)
		
		val orOp = createBinaryOperation("or", lhsExistsOp, rhsExistsOp, LogicalOperation)
		
		val generatedFunction = expressionHandler.javaCode(orOp, new ParamMap(mockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('exists(MapperS.of(foo).<Foo>map(\"getAttr1\", _foo -> _foo.getAttr1())).or(exists(MapperS.of(foo).<Foo>map(\"getAttr2\", _foo -> _foo.getAttr2())))'))
	}

	// ( Foo -> attr1 = Foo -> attr2 ) or ( Foo -> attr1 = Foo -> attr2 )
	
	@Test
	def void shouldGenerateEqualityExprWithOr() {
		val mockClass = createData("Foo")
		
		val featureCall1 = createFeatureCall(mockClass, "attr1")
		val featureCall2 = createFeatureCall(mockClass, "attr2")
		
		val lhsEqualsOp = createModifiableBinaryOperation("=", featureCall1, featureCall2, EqualityOperation)
		
		val featureCall3 = createFeatureCall(mockClass, "attr3")
		val featureCall4 = createFeatureCall(mockClass, "attr4")
		
		val rhsEqualsOp = createModifiableBinaryOperation("=", featureCall3, featureCall4, EqualityOperation)
		
		val orOp = createBinaryOperation("or", lhsEqualsOp, rhsEqualsOp, LogicalOperation)
		
		val generatedFunction = expressionHandler.javaCode(orOp, new ParamMap(mockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('areEqual(MapperS.of(foo).<Foo>map(\"getAttr1\", _foo -> _foo.getAttr1()), MapperS.of(foo).<Foo>map(\"getAttr2\", _foo -> _foo.getAttr2()), CardinalityOperator.All).or(areEqual(MapperS.of(foo).<Foo>map(\"getAttr3\", _foo -> _foo.getAttr3()), MapperS.of(foo).<Foo>map(\"getAttr4\", _foo -> _foo.getAttr4()), CardinalityOperator.All))'))
	}
	
	@Test
	def void shouldGenerateEnumValueRef() {
		val mockClass = createData("Foo")
		
		val featureCall1 = createFeatureCall(mockClass, "attr1")
		val featureCall2 = createFeatureCall(mockClass, "attr2")
		
		val lhsEqualsOp = createModifiableBinaryOperation("=", featureCall1, featureCall2, EqualityOperation)
		
		val featureCall3 = createFeatureCall(mockClass, "attr3")
		val featureCall4 = createFeatureCall(mockClass, "attr4")
		
		val rhsEqualsOp = createModifiableBinaryOperation("=", featureCall3, featureCall4, EqualityOperation)
		
		val orOp = createBinaryOperation("or", lhsEqualsOp, rhsEqualsOp, LogicalOperation)
		
		val generatedFunction = expressionHandler.javaCode(orOp, new ParamMap(mockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('areEqual(MapperS.of(foo).<Foo>map(\"getAttr1\", _foo -> _foo.getAttr1()), MapperS.of(foo).<Foo>map(\"getAttr2\", _foo -> _foo.getAttr2()), CardinalityOperator.All).or(areEqual(MapperS.of(foo).<Foo>map(\"getAttr3\", _foo -> _foo.getAttr3()), MapperS.of(foo).<Foo>map(\"getAttr4\", _foo -> _foo.getAttr4()), CardinalityOperator.All))'))
	}
	
	private def String formatGeneratedFunction(StringConcatenationClient generatedFunction) {
		val isc = new ImportingStringConcatination()
		isc.append(generatedFunction)
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
	
	private def RosettaFeatureCall createFeatureCall(Data rosettaClass, String attributeName) {
		val mockCallableCall = mock(RosettaCallableCall)
		when(mockCallableCall.callable).thenReturn(rosettaClass)

		val mockCardinality = mock(RosettaCardinality)
		when(mockCardinality.sup).thenReturn(1)
		
		val mockAttribute = mock(Attribute)
		when(mockAttribute.name).thenReturn(attributeName)
		when(mockAttribute.card).thenReturn(mockCardinality)
		when(mockAttribute.eContainer).thenReturn(rosettaClass)
		when(mockAttribute.type).thenReturn(rosettaClass)
		when(mockAttribute.annotations).thenReturn(ECollections.emptyEList)
		
		val mockFeatureCall = mock(RosettaFeatureCall)
		when(mockFeatureCall.feature).thenReturn(mockAttribute)
		when(mockFeatureCall.receiver).thenReturn(mockCallableCall)
		return mockFeatureCall
	}

	private def RosettaIntLiteral createIntLiteral(int value) {
		val mockIntLiteral = mock(RosettaIntLiteral)
		when(mockIntLiteral.value).thenReturn(String.valueOf(value))
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
