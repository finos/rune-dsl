package com.regnosys.rosetta.generator.java.expression

import com.google.common.collect.ImmutableList
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.model.lib.RosettaModelObject
import java.math.BigDecimal
import java.util.Map
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.core.Is.is
import static org.junit.jupiter.api.Assertions.*
import com.regnosys.rosetta.generator.java.function.FunctionGeneratorHelper
import org.junit.jupiter.api.Disabled
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class RosettaBinaryOperationTest {
	
	@Inject extension CodeGeneratorTestHelper
	@Inject extension FunctionGeneratorHelper
	
	Map<String, Class<?>> classes
	
	@BeforeEach
	def void setUp() {
		val code = '''
			type NumberList:
				numbers number (0..*)
			
			type Foo:
				bar Bar (0..*)
				bar2 Bar (0..*)
				baz Baz (0..1)
			
			type Bar:
				before number (0..1)
				after number (0..1)
			
			type Baz:
				bazValue number (0..1)
				other number (0..1)
			
			func FeatureCallEqualToLiteral:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar -> before any = 5
			
			func FeatureCallNotEqualToLiteral:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar -> before all <> 5
			
			func FeatureCallEqualToFeatureCall:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar -> before = foo -> bar -> after
			
			func FeatureCallListEqualToFeatureCall:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar -> before any = foo -> baz -> other
			
			func FeatureCallNotEqualToFeatureCall:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar -> before <> foo -> bar -> after
			
			func FeatureCallListNotEqualToFeatureCall:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar -> before all <> foo -> baz -> other
			
			func FeatureCallsEqualToLiteralOr:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar -> before any = 5 or foo -> baz -> other = 5
			
			func FeatureCallsEqualToLiteralAnd:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar -> before any = 5 and foo -> bar -> after any = 5
			
			««« TODO tests compilation only, add unit test

			func MultipleOrFeatureCallsEqualToMultipleOrFeatureCalls:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				alias values : [foo -> bar -> before, foo -> baz -> other]
				set result:
					values contains foo -> bar -> after
						or values contains foo -> baz -> bazValue

			««« TODO tests compilation only, add unit test

			func MultipleAndFeatureCallsEqualToMultipleOrFeatureCalls:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					[foo -> bar -> before,  foo -> baz -> other] = [foo -> bar -> after, foo -> baz -> bazValue]

			««« TODO tests compilation only, add unit test

			func FeatureCallComparisonOr:
				inputs: foo Foo(1..1)
				output: result boolean (1..1)
				set result:
					(foo -> bar -> before any = foo -> baz -> other) or (foo -> bar -> after any = foo -> baz -> bazValue)

			««« TODO tests compilation only, add unit test

			func FeatureCallComparisonAnd:
				inputs: foo Foo(1..1)
				output: result boolean (1..1)
				set result:
					(foo -> bar -> before any = foo -> baz -> other) and (foo -> bar -> after any = foo -> baz -> bazValue)
			
			««« TODO tests compilation only, add unit test

			func MultipleOrFeatureCallEqualToLiteral:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					[foo -> bar -> before, foo -> bar -> after, foo -> baz -> other] contains 5.0
			
			««« TODO tests compilation only, add unit test

			func MultipleAndFeatureCallEqualToLiteral:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					[foo -> bar -> before, foo -> bar -> after, foo -> baz -> other] any = 5.0
			
			««« TODO tests compilation only, add unit test

			func AliasFeatureCallEqualToLiteral:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					AliasBefore(foo) -> numbers any = 5
			
			««« TODO tests compilation only, add unit test

			func AliasFeatureCallEqualToFeatureCall:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					AliasBefore(foo) = AliasAfter(foo)

			««« TODO tests compilation only, add unit test

			func AliasFeatureCallsEqualToLiteralOr:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					AliasBefore(foo) -> numbers any = 5 or  AliasOther(foo) -> numbers any = 5
			
			««« TODO tests compilation only, add unit test

			func AliasFeatureCallsEqualToLiteralAnd:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					AliasBefore(foo) -> numbers any = 5 and AliasOther(foo) -> numbers any = 5
			
			««« TODO tests compilation only, add unit test

			func AliasMultipleOrFeatureCallsEqualToMultipleOrFeatureCalls:
				inputs: foo Foo(1..1)
				output: result boolean (1..1)
				set result:
					(AliasBefore(foo) -> numbers exists
					or AliasOther(foo) -> numbers exists) =
					(AliasAfter(foo) -> numbers contains foo -> baz -> bazValue)
			
			««« TODO tests compilation only, add unit test

			func AliasMultipleOrs:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					AliasBeforeOrAfterOrOther(foo) -> numbers contains 5.0
			
			««« TODO tests compilation only, add unit test

			func MultipleGreaterThanComparisonsWithOrAnd:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar first -> before > 5 
						or ( foo -> baz -> other > 10 and foo -> bar first -> after > 15 )
						or foo -> baz -> bazValue > 20
			
			func FeatureCallGreatherThan:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> bar first -> before > foo -> bar2 first -> before
			
			««« Aliases

			func AliasBefore:
				inputs: foo Foo (1..1)
				output: result NumberList (1..1)
				set result -> numbers: foo -> bar -> before
			
			func AliasAfter:
				inputs: foo Foo (1..1)
				output: result NumberList (1..1)
				set result -> numbers : foo -> bar -> after
			
			func AliasOther:
				inputs: foo Foo (1..1)
				output: result NumberList (1..1)
				set result -> numbers : foo -> baz -> other
			
			
			func AliasBeforeOrAfterOrOther:
				inputs: foo Foo (1..1)
				output: result NumberList (1..1)
				set result -> numbers : [
					foo -> bar -> before,
					foo -> bar -> after,
					foo -> baz -> other
				]

			'''.generateCode
		//code.writeClasses("QualifyEventsComparisonTest")
		classes = code.compileToClasses
	}
	
	@Test
	def shouldCompareEqualObjects() {
		val bar = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val baz = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(5)), of())
		val foo = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', baz), of('bar', ImmutableList.of(bar))))

		assertResult("FeatureCallEqualToLiteral", foo, true)
		assertResult("FeatureCallEqualToFeatureCall", foo, true)
		assertResult("FeatureCallsEqualToLiteralOr", foo, true)
		assertResult("FeatureCallsEqualToLiteralAnd", foo, true)
		assertResult("FeatureCallNotEqualToLiteral", foo, false)
		assertResult("FeatureCallNotEqualToFeatureCall", foo, false)
		assertResult("FeatureCallListEqualToFeatureCall", foo, true)
		assertResult("FeatureCallListNotEqualToFeatureCall", foo, false)
	}

	@Test
	def shouldCompareEqualObjectsWithMultipleCardinality() {
		val bar1 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val baz = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(5)), of())
		val foo = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', baz), of('bar', ImmutableList.of(bar1, bar2))))
		
		assertResult("FeatureCallEqualToLiteral", foo, true)
		assertResult("FeatureCallEqualToFeatureCall", foo, true)
		assertResult("FeatureCallsEqualToLiteralOr", foo, true)
		assertResult("FeatureCallsEqualToLiteralAnd", foo, true)
		assertResult("FeatureCallNotEqualToLiteral", foo, false)
		assertResult("FeatureCallNotEqualToFeatureCall", foo, false)
		assertResult("FeatureCallListEqualToFeatureCall", foo, true)
		assertResult("FeatureCallListNotEqualToFeatureCall", foo, false)
	}
	
	@Test
	def shouldCompareObjectsWithZeroCardinality() {
		val baz = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(5)), of())
		val foo = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', baz), of()))
		
		assertResult("FeatureCallEqualToLiteral", foo, false)
		assertResult("FeatureCallEqualToFeatureCall", foo, false)
		assertResult("FeatureCallNotEqualToLiteral", foo, true)
		assertResult("FeatureCallNotEqualToFeatureCall", foo, true)
		assertResult("FeatureCallListEqualToFeatureCall", foo, false)
		assertResult("FeatureCallListNotEqualToFeatureCall", foo, true)
	}
	
	@Test
	def shouldCompareObjectsWithUnsetValues() {
		val bar1 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(5), 'after', BigDecimal.valueOf(5)), of())
		val baz = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(5)), of())
		val foo = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', baz), of('bar', ImmutableList.of(bar1, bar2))))
		
		assertResult("FeatureCallListEqualToFeatureCall", foo, false)
		assertResult("FeatureCallListNotEqualToFeatureCall", foo, true)
	}
	
	@Test
	def shouldCompareUnequalObjects() {
		val bar = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(4), 'after', BigDecimal.valueOf(5)), of())
		val baz = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(10)), of())
		val foo = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', baz), of('bar', ImmutableList.of(bar))))

		assertResult("FeatureCallEqualToLiteral", foo, false)
		assertResult("FeatureCallEqualToFeatureCall", foo, false)
		assertResult("FeatureCallsEqualToLiteralOr", foo, false)
		assertResult("FeatureCallsEqualToLiteralAnd", foo, false)
		assertResult("FeatureCallNotEqualToLiteral", foo, true)
		assertResult("FeatureCallListEqualToFeatureCall", foo, false)
		assertResult("FeatureCallListNotEqualToFeatureCall", foo, true)
	}
	
	@Test
	def shouldCompareUnequalObjectsWithMultipleCardinality() {
		val bar1 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(4), 'after', BigDecimal.valueOf(5)), of())
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('before', BigDecimal.valueOf(4), 'after', BigDecimal.valueOf(5)), of())
		val baz = classes.createInstanceUsingBuilder('Baz', of('other', BigDecimal.valueOf(10)), of())
		val foo = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('baz', baz), of('bar', ImmutableList.of(bar1, bar2))))
		
		assertResult("FeatureCallEqualToLiteral", foo, false)
		assertResult("FeatureCallEqualToFeatureCall", foo, false)
		assertResult("FeatureCallsEqualToLiteralOr", foo, false)
		assertResult("FeatureCallsEqualToLiteralAnd", foo, false)
		assertResult("FeatureCallNotEqualToLiteral", foo, true)
		assertResult("FeatureCallListEqualToFeatureCall", foo, false)
		assertResult("FeatureCallListNotEqualToFeatureCall", foo, true)
	}
	
	@Test
	def void shouldGenerateBooleanOrComparisonResult() {
		val model = '''
			func FuncFoo:
				inputs: 
					foo Foo (1..1)
				output: 
					result boolean (1..1)
			
				set result:
					foo -> attrBoolean or foo -> attrNumber = 5
			
			type Foo:
				attrBoolean boolean (1..1)
				attrNumber number (1..1)
		'''
		val code = model.generateCode
		val funcFoo = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.expression.ComparisonResult;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Foo;
				import java.math.BigDecimal;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param foo 
					* @return result 
					*/
					public Boolean evaluate(Foo foo) {
						Boolean result = doEvaluate(foo);
						
						return result;
					}
				
					protected abstract Boolean doEvaluate(Foo foo);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected Boolean doEvaluate(Foo foo) {
							Boolean result = null;
							return assignOutput(result, foo);
						}
						
						protected Boolean assignOutput(Boolean result, Foo foo) {
							result = ComparisonResult.of(MapperS.of(foo).<Boolean>map("getAttrBoolean", _foo -> _foo.getAttrBoolean())).or(areEqual(MapperS.of(foo).<BigDecimal>map("getAttrNumber", _foo -> _foo.getAttrNumber()), MapperS.of(BigDecimal.valueOf(5)), CardinalityOperator.All)).get();
							
							return result;
						}
					}
				}
			'''.toString,
			funcFoo
		)
		code.compileToClasses
	}

	@Test
	def void shouldGenerateBooleanAndComparisonResult() {
		val model = '''
			func FuncFoo:
				inputs: 
					foo Foo (1..1)
				output: 
					result boolean (1..1)
			
				set result:
					foo -> attrBoolean and foo -> attrNumber = 5
			
			type Foo:
				attrBoolean boolean (1..1)
				attrNumber number (1..1)
		'''
		val code = model.generateCode
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateBooleanAndComparisonResult2() {
		val model = '''
			func FuncFoo:
				inputs: 
					foo Foo (1..1)
				output: 
					result boolean (1..1)
			
				set result:
					foo -> attrNumber = 5 and foo -> attrBoolean
			
			type Foo:
				attrBoolean boolean (1..1)
				attrNumber number (1..1)
		'''
		val code = model.generateCode
		code.compileToClasses
	}

	@Disabled
	@Test
	def void shouldGenerateBooleanAndComparisonResult3() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		foo Foo (1..1)
				output:
					result boolean (1..1)
				
				set result:
					( foo -> x1 and foo -> x2 ) exists
			
			type Foo:
				x1 boolean (1..1)
				x2 boolean (1..1)
		'''
		val code = model.generateCode
		val funcFoo = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.ComparisonResult;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Foo;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param foo 
					* @return result 
					*/
					public Boolean evaluate(Foo foo) {
						Boolean result = doEvaluate(foo);
						
						return result;
					}
				
					protected abstract Boolean doEvaluate(Foo foo);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected Boolean doEvaluate(Foo foo) {
							Boolean result = null;
							return assignOutput(result, foo);
						}
						
						protected Boolean assignOutput(Boolean result, Foo foo) {
							result = exists(ComparisonResult.of(MapperS.of(foo).<Boolean>map("getX1", _foo -> _foo.getX1())).and(ComparisonResult.of(MapperS.of(foo).<Boolean>map("getX2", _foo -> _foo.getX2())))).get();
							
							return result;
						}
					}
				}
			'''.toString,
			funcFoo
		)
		code.compileToClasses
	}
	
	@Disabled
	@Test
	def void shouldGenerateBooleanAndComparisonResult4() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		foo Foo (1..1)
				output:
					result boolean (1..1)
				
				set result:
					( foo -> x1 and foo -> x2 ) exists
			
			type Foo:
				x1 boolean (0..*)
				x2 boolean (1..1)
		'''
		val code = model.generateCode
		val funcFoo = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.ComparisonResult;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Foo;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param foo 
					* @return result 
					*/
					public Boolean evaluate(Foo foo) {
						Boolean result = doEvaluate(foo);
						
						return result;
					}
				
					protected abstract Boolean doEvaluate(Foo foo);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected Boolean doEvaluate(Foo foo) {
							Boolean result = null;
							return assignOutput(result, foo);
						}
						
						protected Boolean assignOutput(Boolean result, Foo foo) {
							result = exists(ComparisonResult.of(MapperS.of(foo).<Boolean>mapC("getX1", _foo -> _foo.getX1())).and(ComparisonResult.of(MapperS.of(foo).<Boolean>map("getX2", _foo -> _foo.getX2())))).get();
							
							return result;
						}
					}
				}
			'''.toString,
			funcFoo
		)
		code.compileToClasses
	}

	// Util methods
		
	def assertResult(String funcName, RosettaModelObject input, boolean expectedResult) {
		val func = classes.createFunc(funcName);
		val res = func.invokeFunc(Boolean, input)
		assertThat(res, is(expectedResult))
	}
}
