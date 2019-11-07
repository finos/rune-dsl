package com.regnosys.rosetta.generator.java.qualify

import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator.ParamMap
import com.regnosys.rosetta.generator.java.util.ImportingStringConcatination
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCardinality
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaIntLiteral
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.tests.RosettaInjectorProvider
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

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaExpressionJavaGeneratorTest {
	@Inject ExpressionGenerator expressionHandler
	
	/**
	 *  ( Foo -> attr1 ) > 5
	 */
	@Test
	def void shouldGenerateGreaterThanExpression() {
		val lhsMockClass = createRosettaClass("Foo")
		val lhsFeatureCall = createFeatureCall(lhsMockClass, "attr1")
		
		val rhsIntLiteral = createIntLiteral(5)
		
		val comparisonOp = createBinaryOperation(">", lhsFeatureCall, rhsIntLiteral)
		
		val generatedFunction = expressionHandler.javaCode(comparisonOp, new ParamMap(lhsMockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('greaterThan(MapperS.of(foo).<Foo>map("getAttr1", Foo::getAttr1), MapperS.of(Integer.valueOf(5)))'))
	}

	/**
	 *  ( Foo -> attr1 ) > 5 or ( Foo -> attr2 ) > 5
	 */
	@Test
	def void shouldGenerateGreaterThanExpressionsWithOr1() {
		val mockClass = createRosettaClass("Foo")
		
		val lhsFeatureCall = createFeatureCall(mockClass, "attr1")
		val lhsComparisonOp = createBinaryOperation(">", lhsFeatureCall, createIntLiteral(5))
		
		val rhsFeatureCall = createFeatureCall(mockClass, "attr2")
		val rhsComparisonOp = createBinaryOperation(">", rhsFeatureCall, createIntLiteral(5))
		
		val orOp = createBinaryOperation("or", lhsComparisonOp, rhsComparisonOp)
		
		val generatedFunction = expressionHandler.javaCode(orOp, new ParamMap(mockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('greaterThan(MapperS.of(foo).<Foo>map("getAttr1", Foo::getAttr1), MapperS.of(Integer.valueOf(5))).or(greaterThan(MapperS.of(foo).<Foo>map("getAttr2", Foo::getAttr2), MapperS.of(Integer.valueOf(5))))'))
	}

	/**
	 *  ( Foo -> attr1 or Foo -> attr2 ) > 5
	 */
	@Test
	def void shouldGenerateGreaterThanExpressionsWithOr2() {
		val mockClass = createRosettaClass("Foo")
		
		val lhsFeatureCall = createFeatureCall(mockClass, "attr1")
		val rhsFeatureCall = createFeatureCall(mockClass, "attr2")
		
		val orOp = createBinaryOperation("or", lhsFeatureCall, rhsFeatureCall)
		
		val comparisonOp = createBinaryOperation(">", orOp, createIntLiteral(5))
		
		val generatedFunction = expressionHandler.javaCode(comparisonOp, new ParamMap(mockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('greaterThan(MapperTree.or(MapperTree.of(MapperS.of(foo).<Foo>map("getAttr1", Foo::getAttr1)), MapperTree.of(MapperS.of(foo).<Foo>map("getAttr2", Foo::getAttr2))), MapperTree.of(MapperS.of(Integer.valueOf(5))))'))
	}
	
	/**
	 *  ( Foo -> attr1 or ( Foo -> attr2 and Foo -> attr3 ) or Foo -> attr4 ) > 5
	 */
	@Test
	def void shouldGenerateGreaterThanExpressionsWithOrAnd() {
		val mockClass = createRosettaClass("Foo")
		
		val featureCall1 = createFeatureCall(mockClass, "attr1")
		val featureCall2 = createFeatureCall(mockClass, "attr2")
		val featureCall3 = createFeatureCall(mockClass, "attr3")
		val featureCall4 = createFeatureCall(mockClass, "attr4")
		
		val andOp = createBinaryOperation("and", featureCall2, featureCall3)
		val orOp2 = createBinaryOperation("or", andOp, featureCall4)
		val orOp1 = createBinaryOperation("or", featureCall1, orOp2)
		
		val comparisonOp = createBinaryOperation(">", orOp1, createIntLiteral(5))
		
		val generatedFunction = expressionHandler.javaCode(comparisonOp, new ParamMap(mockClass))
		
		assertNotNull(generatedFunction)
		assertEquals('greaterThan(MapperTree.or(MapperTree.of(MapperS.of(foo).<Foo>map("getAttr1", Foo::getAttr1)), MapperTree.or(MapperTree.and(MapperTree.of(MapperS.of(foo).<Foo>map("getAttr2", Foo::getAttr2)), MapperTree.of(MapperS.of(foo).<Foo>map("getAttr3", Foo::getAttr3))), MapperTree.of(MapperS.of(foo).<Foo>map("getAttr4", Foo::getAttr4)))), MapperTree.of(MapperS.of(Integer.valueOf(5))))',formatGeneratedFunction('''«generatedFunction»'''))
	}
	
	/**
	 *  ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )
	 */
	@Test
	def void shouldGenerateGreaterThanExpressionsWithAnd() {
		val mockClass = createRosettaClass("Foo")
		
		val featureCall1 = createFeatureCall(mockClass, "attr1")
		val featureCall2 = createFeatureCall(mockClass, "attr2")
		val featureCall3 = createFeatureCall(mockClass, "attr3")
		val featureCall4 = createFeatureCall(mockClass, "attr4")
		
		val lhsAndOp = createBinaryOperation("and", featureCall1, featureCall2)
		val rhsAndOp = createBinaryOperation("and", featureCall3, featureCall4)
		
		val comparisonOp = createBinaryOperation(">", lhsAndOp, rhsAndOp)
		
		val generatedFunction = expressionHandler.javaCode(comparisonOp, new ParamMap(mockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('greaterThan(MapperTree.and(MapperTree.of(MapperS.of(foo).<Foo>map("getAttr1", Foo::getAttr1)), MapperTree.of(MapperS.of(foo).<Foo>map("getAttr2", Foo::getAttr2))), MapperTree.and(MapperTree.of(MapperS.of(foo).<Foo>map("getAttr3", Foo::getAttr3)), MapperTree.of(MapperS.of(foo).<Foo>map("getAttr4", Foo::getAttr4))))'))
	}
	
	/**
	 *  ( Foo -> attr ) exists
	 */
	@Test
	def void shouldGenerateExistsExpression() {
		val lhsMockClass = createRosettaClass("Foo")
		val lhsFeatureCall = createFeatureCall(lhsMockClass, "attr")
		val lhsExistsOp = createExistsExpression(lhsFeatureCall)
		
		val generatedFunction = expressionHandler.javaCode(lhsExistsOp, new ParamMap(lhsMockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('exists(MapperS.of(foo).<Foo>map("getAttr", Foo::getAttr), false)'))
	}

	/**
	 *  ( Foo -> attr1 ) exists or ( Foo -> attr2 ) exists
	 */
	@Test
	def void shouldGenerateExistsExpressionsWithOr1() {
		val mockClass = createRosettaClass("Foo")
		
		val lhsFeatureCall = createFeatureCall(mockClass, "attr1")
		val lhsExistsOp = createExistsExpression(lhsFeatureCall)
		
		val rhsFeatureCall = createFeatureCall(mockClass, "attr2")
		val rhsExistsOp = createExistsExpression(rhsFeatureCall)
		
		val orOp = createBinaryOperation("or", lhsExistsOp, rhsExistsOp)
		
		val generatedFunction = expressionHandler.javaCode(orOp, new ParamMap(mockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('exists(MapperS.of(foo).<Foo>map("getAttr1", Foo::getAttr1), false).or(exists(MapperS.of(foo).<Foo>map("getAttr2", Foo::getAttr2), false))'))
	}
	
	/**
	 *  ( Foo -> attr1 or Foo -> attr2 ) exists
	 */
	@Test
	def void shouldGenerateExistsExpressionsWithOr2() {
		val mockClass = createRosettaClass("Foo")
		
		val lhsFeatureCall = createFeatureCall(mockClass, "attr1")
		val rhsFeatureCall = createFeatureCall(mockClass, "attr2")
		
		val orOp = createBinaryOperation("or", lhsFeatureCall, rhsFeatureCall)
		
		val existsExpression = createExistsExpression(orOp)
		
		val generatedFunction = expressionHandler.javaCode(existsExpression, new ParamMap(mockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('exists(MapperTree.or(MapperTree.of(MapperS.of(foo).<Foo>map("getAttr1", Foo::getAttr1)), MapperTree.of(MapperS.of(foo).<Foo>map("getAttr2", Foo::getAttr2))), false)'))
	}

	// ( Foo -> attr1 = Foo -> attr2 ) or ( Foo -> attr1 = Foo -> attr2 )
	
	@Test
	def void shouldGenerateEqualityExprWithOr() {
		val mockClass = createRosettaClass("Foo")
		
		val featureCall1 = createFeatureCall(mockClass, "attr1")
		val featureCall2 = createFeatureCall(mockClass, "attr2")
		
		val lhsEqualsOp = createBinaryOperation("=", featureCall1, featureCall2)
		
		val featureCall3 = createFeatureCall(mockClass, "attr3")
		val featureCall4 = createFeatureCall(mockClass, "attr4")
		
		val rhsEqualsOp = createBinaryOperation("=", featureCall3, featureCall4)
		
		val orOp = createBinaryOperation("or", lhsEqualsOp, rhsEqualsOp)
		
		val generatedFunction = expressionHandler.javaCode(orOp, new ParamMap(mockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('areEqual(MapperS.of(foo).<Foo>map("getAttr1", Foo::getAttr1), MapperS.of(foo).<Foo>map("getAttr2", Foo::getAttr2)).or(areEqual(MapperS.of(foo).<Foo>map("getAttr3", Foo::getAttr3), MapperS.of(foo).<Foo>map("getAttr4", Foo::getAttr4)))'))
	}
	
	@Test
	def void shouldGenerateEnumValueRef() {
		val mockClass = createRosettaClass("Foo")
		
		val featureCall1 = createFeatureCall(mockClass, "attr1")
		val featureCall2 = createFeatureCall(mockClass, "attr2")
		
		val lhsEqualsOp = createBinaryOperation("=", featureCall1, featureCall2)
		
		val featureCall3 = createFeatureCall(mockClass, "attr3")
		val featureCall4 = createFeatureCall(mockClass, "attr4")
		
		val rhsEqualsOp = createBinaryOperation("=", featureCall3, featureCall4)
		
		val orOp = createBinaryOperation("or", lhsEqualsOp, rhsEqualsOp)
		
		val generatedFunction = expressionHandler.javaCode(orOp, new ParamMap(mockClass))
		
		assertNotNull(generatedFunction)
		assertThat(formatGeneratedFunction('''«generatedFunction»'''), 
			is('areEqual(MapperS.of(foo).<Foo>map("getAttr1", Foo::getAttr1), MapperS.of(foo).<Foo>map("getAttr2", Foo::getAttr2)).or(areEqual(MapperS.of(foo).<Foo>map("getAttr3", Foo::getAttr3), MapperS.of(foo).<Foo>map("getAttr4", Foo::getAttr4)))'))
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
		when(mockExistsExpression.only).thenReturn(false);
		when(mockExistsExpression.single).thenReturn(false);
		when(mockExistsExpression.multiple).thenReturn(false);
		return mockExistsExpression;
	}
	
 	private def RosettaBinaryOperation createBinaryOperation(String operator, RosettaExpression left, RosettaExpression right) {
		val mockBinaryOperation = mock(RosettaBinaryOperation) 
		when(mockBinaryOperation.operator).thenReturn(operator)
		when(mockBinaryOperation.left).thenReturn(left)
		when(mockBinaryOperation.right).thenReturn(right)
		return mockBinaryOperation
	}
	
	private def RosettaFeatureCall createFeatureCall(RosettaClass rosettaClass, String attributeName) {
		val mockCallableCall = mock(RosettaCallableCall)
		when(mockCallableCall.callable).thenReturn(rosettaClass)

		val mockCardinality = mock(RosettaCardinality)
		when(mockCardinality.sup).thenReturn(1)
		
		val mockAttribute = mock(RosettaRegularAttribute)
		when(mockAttribute.name).thenReturn(attributeName)
		when(mockAttribute.card).thenReturn(mockCardinality)
		when(mockAttribute.eContainer).thenReturn(rosettaClass)
		when(mockAttribute.type).thenReturn(rosettaClass)
		
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
	
	private def RosettaClass createRosettaClass(String className) {
		val mockRosettaClass = mock(RosettaClass)
		when(mockRosettaClass.name).thenReturn(className)
		return mockRosettaClass
	}
}
