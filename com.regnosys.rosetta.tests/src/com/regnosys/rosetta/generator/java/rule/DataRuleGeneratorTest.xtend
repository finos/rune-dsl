package com.regnosys.rosetta.generator.java.rule

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.DataRuleHelper
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
	@Inject extension DataRuleHelper

	@Test
	def void quoteExists() {
		val code = '''
				class Quote {
					quotePrice QuotePrice (0..1);
				}
				
				class QuotePrice {
					bidPrice number (0..1);
					offerPrice number (0..1);
				}
				
				data rule Quote_Price
				when Quote -> quotePrice exists
				then (Quote -> quotePrice -> bidPrice or Quote -> quotePrice -> offerPrice) exists
		'''.generateCode
		code.compileToClasses
	}

	@Test
	def void nestedAnds() {
		val code = '''
				class Quote {
					quotePrice QuotePrice (0..1);
				}
				
				class QuotePrice {
					price1 number (0..1);
					price2 number (0..1);
					price3 number (0..1);
				}
				
				data rule Quote_Price
				when Quote -> quotePrice exists
				then (
					Quote -> quotePrice -> price1 
						and 
					Quote -> quotePrice -> price2
						and 
					Quote -> quotePrice -> price3
					
					) exists
		'''.generateCode
		//writeOutClasses(code)
		code.compileToClasses
	}

	@Test
	def void numberAttributeisHandled() {
		val code = '''
				class Quote {
					quotePrice QuotePrice (0..1);
				}
				
				class QuotePrice {
					bidPrice number (0..1);
				}
				
				data rule Quote_Price
				when Quote -> quotePrice exists
				then Quote -> quotePrice -> bidPrice = 0.0
		'''.generateCode
		writeOutClasses(code)
		code.compileToClasses
	}
	
	@Test
	def void dataRuleCoinHead() {
		val code = '''
			class Coin {
				head boolean (0..1);
				tail boolean (0..1);
			}
			data rule CoinHeadRule
				when Coin -> head = True
				then Coin -> tail = False
		'''.generateCode
		//writeOutClasses(code)

		val classes = code.compileToClasses
		val coinInstance = classes.createInstanceUsingBuilder('Coin', of('head', true, 'tail', true), of())

		val validationResult = classes.runDataRule(coinInstance, 'CoinHeadRule')
		assertFalse(validationResult.isSuccess)
		assertThat(validationResult.definition, is("when Coin -> head = True\nthen Coin -> tail = False"))
		assertThat(validationResult.failureReason.orElse(""), is("[Coin->getTail] [true] does not equal [Boolean] [false]"))
	}

	@Test
	def void dataRuleCoinTail() {
		val code = '''
			class Coin {
				head boolean (0..1);
				tail boolean (0..1);
			}
			data rule CoinTailRule
				when Coin -> tail = True
				then Coin -> head = False
		'''.generateCode
		//code.printDataRule('CoinTailRule')
		val classes = code.compileToClasses

		val coinInstance = classes.createInstanceUsingBuilder('Coin', of('head', false, 'tail', true), of())

		val validationResult = classes.runDataRule(coinInstance, 'CoinTailRule')
		assertTrue(validationResult.isSuccess)
		assertThat(validationResult.definition, is("when Coin -> tail = True\nthen Coin -> head = False"))
	}

	@Test
	def void dataRuleCoinEdge() {
		val code = '''
			class Coin {
				head boolean (0..1);
				tail boolean (0..1);
			}
			data rule CoinEdgeRule
				when Coin -> tail = False
				then Coin -> head = False
				
		'''.generateCode
		//code.printDataRule('CoinEdgeRule')

		val classes = code.compileToClasses

		val coinInstance = classes.createInstanceUsingBuilder('Coin', of('head', false, 'tail', false), of())

		val validationResult = classes.runDataRule(coinInstance, 'CoinEdgeRule')
		assertTrue(validationResult.isSuccess)
		assertThat(validationResult.definition, is("when Coin -> tail = False\nthen Coin -> head = False"))
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
				
		val validationResult = classes.runDataRule(fooInstance, 'ListDataRule')
		assertTrue(validationResult.isSuccess)
		assertThat(validationResult.definition, is("when Foo -> bar -> baz exists\nthen Foo -> bar -> baz -> bazValue = 1.0"))
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
				
		val validationResult = classes.runDataRule(fooInstance, 'ListDataRule')
		assertFalse(validationResult.isSuccess)
		assertThat(validationResult.definition, is("when Foo -> bar -> baz exists\nthen Foo -> bar -> baz -> bazValue = 1.0"))
		assertThat(validationResult.failureReason.orElse(""), is("[Foo->getBar[0]->getBaz[0]->getBazValue] [2.0] does not equal [BigDecimal] [1]"))
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
				
		val validationResult = classes.runDataRule(fooInstance, 'ListDataRule')
		assertTrue(validationResult.isSuccess)
		assertThat(validationResult.definition, is("when Foo -> bar -> baz exists\nthen Foo -> bar -> baz -> bazValue = 1.0"))
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
			
		val validationResult = classes.runDataRule(fooInstance, 'ListDataRule')
		assertFalse(validationResult.isSuccess)
		assertThat(validationResult.definition, is("when Foo -> bar -> baz exists\nthen Foo -> bar -> baz -> bazValue = 1.0"))
		assertThat(validationResult.failureReason.orElse(""), is("[Foo->getBar[0]->getBaz[0]->getBazValue, Foo->getBar[0]->getBaz[1]->getBazValue] [1.0, 2.0] does not equal [BigDecimal] [1]"))
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
			
		val validationResult = classes.runDataRule(fooInstance, 'ListDataRule')
		assertFalse(validationResult.isSuccess)
		assertThat(validationResult.definition, is("when Foo -> bar -> baz exists\nthen Foo -> bar -> baz -> bazValue = 1.0"))
		assertThat(validationResult.failureReason.orElse(""), is("[Foo->getBar[0]->getBaz[0]->getBazValue, Foo->getBar[1]->getBaz[0]->getBazValue] [1.0, 2.0] does not equal [BigDecimal] [1]"))
	}
	
	def Map<String, Class<?>> createListClassAndDataRule() {
		val code = '''
			class Foo {
				bar Bar (0..*);
			}
			
			class Bar {
				baz Baz (0..*);
			}
			
			class Baz {
				bazValue number (0..1);
			}
			
			data rule ListDataRule
				when Foo -> bar -> baz exists
				then Foo -> bar -> baz -> bazValue = 1.0
		'''.generateCode
		//.writeClasses("ListAndDataRule")
		return code.compileToClasses
	}
}
