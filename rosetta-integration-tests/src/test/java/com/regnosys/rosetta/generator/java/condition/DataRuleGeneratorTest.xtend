package com.regnosys.rosetta.generator.java.condition

import com.google.common.collect.ImmutableList
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import java.io.File
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.util.HashMap
import java.util.Map
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.core.Is.is
import static org.junit.jupiter.api.Assertions.*
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class DataRuleGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ConditionTestHelper

	@Test
	def void shouldGenerateConditionWithIfElseIf() {
		val model = '''
			type Foo:
				bar string (0..1)
				baz string (0..1)
			
			    condition:
			        if bar="Y" then baz exists
			        else if (bar="I" or bar="N") then baz is absent
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.validation.datarule.FooDataRule0")
		assertEquals(
			'''
				package com.rosetta.test.model.validation.datarule;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.annotations.RosettaDataRule;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.expression.ComparisonResult;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.path.RosettaPath;
				import com.rosetta.model.lib.validation.ValidationResult;
				import com.rosetta.model.lib.validation.ValidationResult.ValidationType;
				import com.rosetta.model.lib.validation.Validator;
				import com.rosetta.test.model.Foo;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				/**
				 * @version test
				 */
				@RosettaDataRule("FooDataRule0")
				@ImplementedBy(FooDataRule0.Default.class)
				public interface FooDataRule0 extends Validator<Foo> {
					
					String NAME = "FooDataRule0";
					String DEFINITION = "if bar=\"Y\" then baz exists else if (bar=\"I\" or bar=\"N\") then baz is absent";
					
					ValidationResult<Foo> validate(RosettaPath path, Foo foo);
					
					class Default implements FooDataRule0 {
					
						@Override
						public ValidationResult<Foo> validate(RosettaPath path, Foo foo) {
							ComparisonResult result = executeDataRule(foo);
							if (result.get()) {
								return ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "Foo", path, DEFINITION);
							}
							
							String failureMessage = result.getError();
							if (failureMessage == null || failureMessage.contains("Null") || failureMessage == "") {
								failureMessage = "Condition has failed.";
							}
							return ValidationResult.failure(NAME, ValidationType.DATA_RULE, "Foo", path, DEFINITION, failureMessage);
						}
						
						private ComparisonResult executeDataRule(Foo foo) {
							try {
								if (areEqual(MapperS.of(foo).<String>map("getBar", _foo -> _foo.getBar()), MapperS.of("Y"), CardinalityOperator.All).getOrDefault(false)) {
									return exists(MapperS.of(foo).<String>map("getBaz", _foo -> _foo.getBaz()));
								}
								if (areEqual(MapperS.of(foo).<String>map("getBar", _foo -> _foo.getBar()), MapperS.of("I"), CardinalityOperator.All).or(areEqual(MapperS.of(foo).<String>map("getBar", _foo -> _foo.getBar()), MapperS.of("N"), CardinalityOperator.All)).getOrDefault(false)) {
									return notExists(MapperS.of(foo).<String>map("getBaz", _foo -> _foo.getBaz()));
								}
								return ComparisonResult.successEmptyOperand("");
							}
							catch (Exception ex) {
								return ComparisonResult.failure(ex.getMessage());
							}
						}
					}
					
					@SuppressWarnings("unused")
					class NoOp implements FooDataRule0 {
					
						@Override
						public ValidationResult<Foo> validate(RosettaPath path, Foo foo) {
							return ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "Foo", path, DEFINITION);
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses

		val foo1 = classes.createInstanceUsingBuilder('Foo', of('bar', 'Y', 'baz', 'a'))
		assertTrue(classes.runCondition(foo1, 'FooDataRule0').isSuccess)

		val foo2 = classes.createInstanceUsingBuilder('Foo', of('bar', 'Y'))
		assertFalse(classes.runCondition(foo2, 'FooDataRule0').isSuccess)

		val foo3 = classes.createInstanceUsingBuilder('Foo', of('bar', 'I'))
		assertTrue(classes.runCondition(foo3, 'FooDataRule0').isSuccess)

		val foo4 = classes.createInstanceUsingBuilder('Foo', of('bar', 'I', 'baz', 'a'))
		assertFalse(classes.runCondition(foo4, 'FooDataRule0').isSuccess)

		val foo5 = classes.createInstanceUsingBuilder('Foo', of('bar', 'N'))
		assertTrue(classes.runCondition(foo5, 'FooDataRule0').isSuccess)
		
		val foo6 = classes.createInstanceUsingBuilder('Foo', of('bar', 'N', 'baz', 'a'))
		assertFalse(classes.runCondition(foo6, 'FooDataRule0').isSuccess)
		
		val foo7 = classes.createInstanceUsingBuilder('Foo', of())
		assertTrue(classes.runCondition(foo7, 'FooDataRule0').isSuccess)
	}

	@Test
	def void shouldGenerateConditionWithNestedIfElseIf() {
		val model = '''
			type Foo:
				bar string (0..1)
				baz string (0..1)
			
			    condition:
			    	if bar exists then
				        if bar="Y" then baz exists
				        else if (bar="I" or bar="N") then baz is absent
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.validation.datarule.FooDataRule0")
		assertEquals(
			'''
				package com.rosetta.test.model.validation.datarule;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.annotations.RosettaDataRule;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.expression.ComparisonResult;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.path.RosettaPath;
				import com.rosetta.model.lib.validation.ValidationResult;
				import com.rosetta.model.lib.validation.ValidationResult.ValidationType;
				import com.rosetta.model.lib.validation.Validator;
				import com.rosetta.test.model.Foo;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				/**
				 * @version test
				 */
				@RosettaDataRule("FooDataRule0")
				@ImplementedBy(FooDataRule0.Default.class)
				public interface FooDataRule0 extends Validator<Foo> {
					
					String NAME = "FooDataRule0";
					String DEFINITION = "if bar exists then if bar=\"Y\" then baz exists else if (bar=\"I\" or bar=\"N\") then baz is absent";
					
					ValidationResult<Foo> validate(RosettaPath path, Foo foo);
					
					class Default implements FooDataRule0 {
					
						@Override
						public ValidationResult<Foo> validate(RosettaPath path, Foo foo) {
							ComparisonResult result = executeDataRule(foo);
							if (result.get()) {
								return ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "Foo", path, DEFINITION);
							}
							
							String failureMessage = result.getError();
							if (failureMessage == null || failureMessage.contains("Null") || failureMessage == "") {
								failureMessage = "Condition has failed.";
							}
							return ValidationResult.failure(NAME, ValidationType.DATA_RULE, "Foo", path, DEFINITION, failureMessage);
						}
						
						private ComparisonResult executeDataRule(Foo foo) {
							try {
								if (exists(MapperS.of(foo).<String>map("getBar", _foo -> _foo.getBar())).getOrDefault(false)) {
									if (areEqual(MapperS.of(foo).<String>map("getBar", _foo -> _foo.getBar()), MapperS.of("Y"), CardinalityOperator.All).getOrDefault(false)) {
										return exists(MapperS.of(foo).<String>map("getBaz", _foo -> _foo.getBaz()));
									}
									if (areEqual(MapperS.of(foo).<String>map("getBar", _foo -> _foo.getBar()), MapperS.of("I"), CardinalityOperator.All).or(areEqual(MapperS.of(foo).<String>map("getBar", _foo -> _foo.getBar()), MapperS.of("N"), CardinalityOperator.All)).getOrDefault(false)) {
										return notExists(MapperS.of(foo).<String>map("getBaz", _foo -> _foo.getBaz()));
									}
									return ComparisonResult.successEmptyOperand("");
								}
								return ComparisonResult.successEmptyOperand("");
							}
							catch (Exception ex) {
								return ComparisonResult.failure(ex.getMessage());
							}
						}
					}
					
					@SuppressWarnings("unused")
					class NoOp implements FooDataRule0 {
					
						@Override
						public ValidationResult<Foo> validate(RosettaPath path, Foo foo) {
							return ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "Foo", path, DEFINITION);
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses

		val foo1 = classes.createInstanceUsingBuilder('Foo', of('bar', 'Y', 'baz', 'a'))
		assertTrue(classes.runCondition(foo1, 'FooDataRule0').isSuccess)

		val foo2 = classes.createInstanceUsingBuilder('Foo', of('bar', 'Y'))
		assertFalse(classes.runCondition(foo2, 'FooDataRule0').isSuccess)

		val foo3 = classes.createInstanceUsingBuilder('Foo', of('bar', 'I'))
		assertTrue(classes.runCondition(foo3, 'FooDataRule0').isSuccess)

		val foo4 = classes.createInstanceUsingBuilder('Foo', of('bar', 'I', 'baz', 'a'))
		assertFalse(classes.runCondition(foo4, 'FooDataRule0').isSuccess)

		val foo5 = classes.createInstanceUsingBuilder('Foo', of('bar', 'N'))
		assertTrue(classes.runCondition(foo5, 'FooDataRule0').isSuccess)
		
		val foo6 = classes.createInstanceUsingBuilder('Foo', of('bar', 'N', 'baz', 'a'))
		assertFalse(classes.runCondition(foo6, 'FooDataRule0').isSuccess)
		
		val foo7 = classes.createInstanceUsingBuilder('Foo', of())
		assertTrue(classes.runCondition(foo7, 'FooDataRule0').isSuccess)
	}


	@Test
	def void quoteExists() {
		val code = '''
				type Quote:
					quotePrice QuotePrice (0..1)
					condition Quote_Price:
						if quotePrice exists
						then quotePrice -> bidPrice exists or quotePrice -> offerPrice exists
				
				type QuotePrice:
					bidPrice number (0..1)
					offerPrice number (0..1)
		'''.generateCode
		code.compileToClasses
	}

	@Test
	def void nestedAnds() {
		val code = '''
				type Quote:
					quotePrice QuotePrice (0..1)
					condition Quote_Price:
						if quotePrice exists
						then (
							quotePrice -> price1 exists
							and quotePrice -> price2 exists
							and quotePrice -> price3 exists
						)
				
				type QuotePrice:
					price1 number (0..1)
					price2 number (0..1)
					price3 number (0..1)
				
		'''.generateCode
		//writeOutClasses(code)
		code.compileToClasses
	}

	@Test
	def void numberAttributeisHandled() {
		val code = '''
				type Quote:
					quotePrice QuotePrice (0..1)
					condition Quote_Price:
						if quotePrice exists
						then quotePrice -> bidPrice = 0.0
				
				type QuotePrice:
					bidPrice number (0..1)
		'''.generateCode
		//writeOutClasses(code)
		code.compileToClasses
	}
	
	@Test
	def void conditionWithDoIfAndFunction() {
		val code = '''
				func Foo:
					inputs:
						price number (0..1)
					output:
						something number (1..1)
				
				type Quote:
					price number (0..1)
					
					condition:
						if price exists
						then Foo( price ) = 5.0
		'''.generateCode
		code.compileToClasses
	}
	
	@Test
	def void conditionWithDoIfAndFunctionAndElse() {
		val code = '''
				func Foo:
					inputs:
						price number (0..1)
					output:
						something number (1..1)
				
				type Quote:
					price number (0..1)
					
					condition:
						if price exists
						then Foo( price ) = 5.0
						else True
		'''.generateCode
		code.compileToClasses
	}
	
	@Test
	def void conditionCoinHead() {
		val code = '''
			type Coin:
				head boolean (0..1)
				tail boolean (0..1)
				
				condition CoinHeadRule:
					if head = True
					then tail = False

		'''.generateCode
		//writeOutClasses(code)

		val classes = code.compileToClasses
		val coinInstance = classes.createInstanceUsingBuilder('Coin', of('head', true, 'tail', true), of())

		val validationResult = classes.runCondition(coinInstance, 'CoinCoinHeadRule')
		assertFalse(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if head = True then tail = False"))
		assertThat(validationResult.failureReason.orElse(""), is("[Coin->getTail] [true] does not equal [Boolean] [false]"))
	}

	@Test
	def void conditionCoinTail() {
		val code = '''
			type Coin:
				head boolean (0..1)
				tail boolean (0..1)
				
				condition CoinTailRule:
					if tail = True
					then head = False
		'''.generateCode
		//code.printDataRule('CoinTailRule')
		val classes = code.compileToClasses

		val coinInstance = classes.createInstanceUsingBuilder('Coin', of('head', false, 'tail', true), of())

		val validationResult = classes.runCondition(coinInstance, 'CoinCoinTailRule')
		assertTrue(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if tail = True then head = False"))
	}
	
	@Test
	def void conditionCoinEdge() {
		val code = '''
			type Coin:
				head boolean (0..1)
				tail boolean (0..1)
				
				condition EdgeRule:
					if tail = False
					then head = False
			
		'''.generateCode
		//code.printDataRule('CoinEdgeRule')

		val classes = code.compileToClasses

		val coinInstance = classes.createInstanceUsingBuilder('Coin', of('head', false, 'tail', false), of())
		
		val validationResult = classes.runCondition(coinInstance, 'CoinEdgeRule')
		assertTrue(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if tail = False then head = False"))
	}

	
	@Test
	def void conditionCount() {
		val code = '''
			type CondTest:
				multiAttr number (0..*)
				
				condition:
					multiAttr count >= 0
		'''.generateCode

		val classes = code.compileToClasses

		val coinInstance = classes.createInstanceUsingBuilder('CondTest', of(), of('multiAttr', #[BigDecimal.ONE]))
		val validationResult = classes.runCondition(coinInstance, 'CondTestDataRule0')

		assertTrue(validationResult.isSuccess)
	}
	
	@Test
	def void shouldCheckConditionWithInheritedAttribute() {
		val code = '''
			type Foo:
				x string (0..1)
				y string (0..1)
				
				condition:
					x exists
			
			type Bar extends Foo:
				z string (0..1)
				
				condition:
					y exists
		'''.generateCode
		
		val classes = code.compileToClasses

		val bar1 = classes.createInstanceUsingBuilder('Bar', of('z', 'v1'))
		val result1 = classes.runCondition(bar1, 'BarDataRule0')
		assertFalse(result1.isSuccess)
		
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('y', 'v1', 'z', 'v2'))
		val result2 = classes.runCondition(bar2, 'BarDataRule0')
		assertTrue(result2.isSuccess)
	}
	
	@Test
	def void shouldCheckConditionWithInheritedAttribute2() {
		val code = #['''
			namespace ns1
			
			type Foo:
				x string (0..1)
				y string (0..1)
				
				condition:
					x exists
			''','''
			namespace ns2
			
			import ns1.*
			
			type Bar extends Foo:
				z string (0..1)
				
				condition:
					y exists
		'''].generateCode
		
		val classes = code.compileToClasses

		val namespace = new RootPackage('ns2')
		val bar1 = classes.createInstanceUsingBuilder(namespace, 'Bar', of('z', 'v1'))
		val result1 = classes.runCondition(namespace, bar1, 'BarDataRule0')
		assertFalse(result1.isSuccess)
		
		val bar2 = classes.createInstanceUsingBuilder(namespace, 'Bar', of('y', 'v1', 'z', 'v2'))
		val result2 = classes.runCondition(namespace, bar2, 'BarDataRule0')
		assertTrue(result2.isSuccess)
	}
	
	@Test
	def void shouldCheckInheritedCondition() {
		val code = '''
			type Foo:
				x string (0..1)
				y string (0..1)
				
				condition:
					x exists
			
			type Bar extends Foo:
				z string (0..1)
				
				condition:
					y exists
		'''.generateCode
		
		val classes = code.compileToClasses

		val bar1 = classes.createInstanceUsingBuilder('Bar', of('z', 'v1'))
		val result1 = classes.runCondition(bar1, 'FooDataRule0')
		assertFalse(result1.isSuccess)
		
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('x', 'v1', 'y', 'v2', 'z', 'v3'))
		val result2 = classes.runCondition(bar2, 'FooDataRule0')
		assertTrue(result2.isSuccess)
	}
		
	@Deprecated
	/**
	 * @deprecated This function if for loacl debugging and checked in code shouldn't use it
	 */
	def writeOutClasses(HashMap<String, String> map) {
		for (entry: map.entrySet) {
			val name = entry.key;
			val pathName = name.replace('.', File.separator)
			val path = Paths.get("src-test/java",pathName+ ".java")
			//println(path.toAbsolutePath)
			Files.createDirectories(path.parent);
			Files.write(path, entry.value.bytes)
		}
	}
	
	@Test
	def void listDataRule_simple() {
		val classes = createListClassAndDataRule()
		
		// baz
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(1)), of())
		// bar
		val barInstance = classes.createInstanceUsingBuilder('Bar', of(), of('baz', ImmutableList.of(bazInstance)))
		// foo
		val fooInstance = classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance)))
				
		val validationResult = classes.runCondition(fooInstance, 'FooListDataRule')
		assertTrue(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if bar -> baz exists then bar -> baz -> bazValue all = 1.0"))
	}
	
	@Test
	def void listDataRule_simpleNoMatch() {
		val classes = createListClassAndDataRule()
		
		// baz
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(2.0)), of())
		// bar
		val barInstance = classes.createInstanceUsingBuilder('Bar', of(), of('baz', ImmutableList.of(bazInstance)))
		// foo
		val fooInstance = classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance)))
				
		val validationResult = classes.runCondition(fooInstance, 'FooListDataRule')
		assertFalse(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if bar -> baz exists then bar -> baz -> bazValue all = 1.0"))
		assertThat(validationResult.failureReason.orElse(""), is("[Foo->getBar[0]->getBaz[0]->getBazValue] [2.0] does not equal [BigDecimal] [1.0]"))
	}
	
	@Test
	def void listDataRule_manyElements() {
		val classes = createListClassAndDataRule()
		
		// Multiple Bar elements but exact one Baz element (to be used for relational operation)
		
		// baz
		val bazInstance = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(1.0)), of())
		val bazInstance2 = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(1.0)), of())
		// bar
		val barInstance1 = classes.createInstanceUsingBuilder('Bar', of(), of('baz', ImmutableList.of(bazInstance)))
		val barInstance2 = classes.createInstanceUsingBuilder('Bar', of(), of('baz', ImmutableList.of(bazInstance2)))
		// foo
		val fooInstance = classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance1, barInstance2)))
				
		val validationResult = classes.runCondition(fooInstance, 'FooListDataRule')
		assertTrue(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if bar -> baz exists then bar -> baz -> bazValue all = 1.0"))
	}
	
	@Test
	def void listDataRule_manyElements_fail1() {
		val classes = createListClassAndDataRule()
		
		// baz
		val bazInstance1 = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(1.0)), of())
		val bazInstance2 = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(2.0)), of())
		// bar
		val barInstance = classes.createInstanceUsingBuilder('Bar', of(), of('baz', ImmutableList.of(bazInstance1, bazInstance2)))
		// foo
		val fooInstance = classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance)))
			
		val validationResult = classes.runCondition(fooInstance, 'FooListDataRule')
		assertFalse(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if bar -> baz exists then bar -> baz -> bazValue all = 1.0"))
		assertThat(validationResult.failureReason.orElse(""), is("[Foo->getBar[0]->getBaz[0]->getBazValue, Foo->getBar[0]->getBaz[1]->getBazValue] [1.0, 2.0] does not equal [BigDecimal] [1.0]"))
	}
	
	@Test
	def void listDataRule_manyElements_fail2() {
		val classes = createListClassAndDataRule()
		
		// baz
		val bazInstance1 = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(1.0)), of())
		val bazInstance2 = classes.createInstanceUsingBuilder('Baz', of('bazValue', BigDecimal.valueOf(2.0)), of())
		// bar
		val barInstance1 = classes.createInstanceUsingBuilder('Bar', of(), of('baz', ImmutableList.of(bazInstance1)))
		val barInstance2 = classes.createInstanceUsingBuilder('Bar', of(), of('baz', ImmutableList.of(bazInstance2)))
		// foo
		val fooInstance = classes.createInstanceUsingBuilder('Foo', of(), of('bar', ImmutableList.of(barInstance1, barInstance2)))
			
		val validationResult = classes.runCondition(fooInstance, 'FooListDataRule')
		assertFalse(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if bar -> baz exists then bar -> baz -> bazValue all = 1.0"))
		assertThat(validationResult.failureReason.orElse(""), is("[Foo->getBar[0]->getBaz[0]->getBazValue, Foo->getBar[1]->getBaz[0]->getBazValue] [1.0, 2.0] does not equal [BigDecimal] [1.0]"))
	}
	
	def Map<String, Class<?>> createListClassAndDataRule() {
		val code = '''
			type Foo:
				bar Bar (0..*)
				
				condition ListDataRule:
					if bar -> baz exists
					then bar -> baz -> bazValue all = 1.0
			
			type Bar:
				baz Baz (0..*)
			
			type Baz:
				bazValue number (0..1)
			
		'''.generateCode
		//.writeClasses("ListAndDataRule")
		return code.compileToClasses
	}
}
