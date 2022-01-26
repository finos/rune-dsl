package com.regnosys.rosetta.generator.java.rule

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
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

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class DataRuleGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ConditionHelper

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
	def void dataRuleWithDoIfAndFunction() {
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
	def void dataRuleWithDoIfAndFunctionAndElse() {
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
	def void dataRuleCoinHead() {
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

		val validationResult = classes.runDataRule(coinInstance, 'CoinCoinHeadRule')
		assertFalse(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if head = True then tail = False"))
		assertThat(validationResult.failureReason.orElse(""), is("[Coin->getTail] [true] does not equal [Boolean] [false]"))
	}

	@Test
	def void dataRuleCoinTail() {
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

		val validationResult = classes.runDataRule(coinInstance, 'CoinCoinTailRule')
		assertTrue(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if tail = True then head = False"))
	}
	
	@Test
	def void dataRuleCoinEdge() {
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
		
		val validationResult = classes.runDataRule(coinInstance, 'CoinEdgeRule')
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
		val validationResult = classes.runDataRule(coinInstance, 'CondTestDataRule0')

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

		val bar1 = classes.createInstanceUsingBuilder('Bar', of('z', 'v1'), of())
		val result1 = classes.runDataRule(bar1, 'BarDataRule0')
		assertFalse(result1.isSuccess)
		
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('y', 'v1', 'z', 'v2'), of())
		val result2 = classes.runDataRule(bar2, 'BarDataRule0')
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

		val bar1 = classes.createInstanceUsingBuilder('Bar', of('z', 'v1'), of())
		val result1 = classes.runDataRule(bar1, 'FooDataRule0')
		assertFalse(result1.isSuccess)
		
		val bar2 = classes.createInstanceUsingBuilder('Bar', of('x', 'v1', 'y', 'v2', 'z', 'v3'), of())
		val result2 = classes.runDataRule(bar2, 'FooDataRule0')
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
				
		val validationResult = classes.runDataRule(fooInstance, 'FooListDataRule')
		assertTrue(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if bar -> baz exists then bar -> baz -> bazValue = 1.0"))
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
				
		val validationResult = classes.runDataRule(fooInstance, 'FooListDataRule')
		assertFalse(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if bar -> baz exists then bar -> baz -> bazValue = 1.0"))
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
				
		val validationResult = classes.runDataRule(fooInstance, 'FooListDataRule')
		assertTrue(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if bar -> baz exists then bar -> baz -> bazValue = 1.0"))
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
			
		val validationResult = classes.runDataRule(fooInstance, 'FooListDataRule')
		assertFalse(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if bar -> baz exists then bar -> baz -> bazValue = 1.0"))
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
			
		val validationResult = classes.runDataRule(fooInstance, 'FooListDataRule')
		assertFalse(validationResult.isSuccess)
		assertThat(validationResult.definition, is("if bar -> baz exists then bar -> baz -> bazValue = 1.0"))
		assertThat(validationResult.failureReason.orElse(""), is("[Foo->getBar[0]->getBaz[0]->getBazValue, Foo->getBar[1]->getBaz[0]->getBazValue] [1.0, 2.0] does not equal [BigDecimal] [1.0]"))
	}
	
	def Map<String, Class<?>> createListClassAndDataRule() {
		val code = '''
			type Foo:
				bar Bar (0..*)
				
				condition ListDataRule:
					if bar -> baz exists
					then bar -> baz -> bazValue = 1.0
			
			type Bar:
				baz Baz (0..*)
			
			type Baz:
				bazValue number (0..1)
			
		'''.generateCode
		//.writeClasses("ListAndDataRule")
		return code.compileToClasses
	}
}
