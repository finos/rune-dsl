package com.regnosys.rosetta.generator.java.translate

import javax.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import com.regnosys.rosetta.generator.java.function.FunctionGeneratorHelper
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.rosetta.model.lib.meta.Key
import com.rosetta.model.lib.meta.Reference
import org.junit.jupiter.api.Disabled

@InjectWith(RosettaInjectorProvider)
@ExtendWith(InjectionExtension)
class TranslateTest {
	
	@Inject extension FunctionGeneratorHelper
	@Inject extension CodeGeneratorTestHelper
	@Inject extension TranslateTestUtil
	
	@Test
	def void testSimpleTranslation() {
		val code = '''
	    type Foo:
	    	a string (1..1)
	    
	    type Bar:
	    	b Qux (1..1)
	    
	    type Qux:
	    	c string (1..1)
	    
	    translate source FooBar {
	        translate Bar to Foo {
	        	a: translate b, 42 to string
	        }
	        
	        translate qux Qux, context number to string:
	        	qux -> c + context to-string
	    }
		'''.generateCode
		
		val classes = code.compileToClasses
        
        val bar = classes.createInstanceUsingBuilder("Bar", #{
	    		"b" -> classes.createInstanceUsingBuilder("Qux", #{
	    			"c" -> "My favourite number is "
	    		})
	        })
	    val expectedResult = classes.createInstanceUsingBuilder("Foo", #{
	    		"a" -> "My favourite number is 42"
	        })
        
        val translation = classes.createTranslation("FooBar", #["Bar"], "Foo");
        assertEquals(expectedResult, translation.invokeFunc(expectedResult.class, #[bar]))
	}
	
	@Test
	def void testTranslateClassNameIsLegalLength() {
		val code = '''
	    type Foo:
	    	a1 string (1..1)
	    	a2 string (1..1)
	    	a3 string (1..1)
	    	a4 string (1..1)
	    	a5 string (1..1)
	    
	    type BarOneWithVeryVeryLongName:
	    	b string (1..1)
	    
	    type BarTwoWithVeryVeryLongName:
	    	b string (1..1)
	    	
	    type BarThreeWithVeryVeryLongName:
	    	b string (1..1)
	    	
	    type BarFourWithVeryVeryLongName:
	    	b string (1..1)
	    	
	    type BarFiveWithVeryVeryLongName:
	    	b string (1..1)
	    	
	    type BarSixWithVeryVeryLongName:
	    	b string (1..1)	
	    
	    translate source LongTranslate {
	        translate bar1 BarOneWithVeryVeryLongName, bar2 BarTwoWithVeryVeryLongName, bar3 BarThreeWithVeryVeryLongName, bar4 BarFourWithVeryVeryLongName, bar5 BarFiveWithVeryVeryLongName to Foo {
	        	a1: bar1 -> b,
	           	a2: bar2 -> b,
	           	a3: bar3 -> b,
	           	a4: bar4 -> b,
	           	a5: bar5 -> b
	        }
	    }
		'''.generateCode
		
		val classes = code.compileToClasses
		

	 	val translation = classes.createTranslation("LongTranslate", #["BarOneWithVeryVeryLongName", "BarTwoWithVeryVeryLongName", "BarThreeWithVeryVeryLongName", "BarFourWithVeryVeryLongName", "BarFiveWithVeryVeryLongName"], "Foo");
  
        val className = translation.class.canonicalName.replaceAll("^com\\.rosetta\\.test\\.model\\.translate\\.", "")
        
  		assertTrue(className.length + 4 <= 255,  "Translator class name too long")
	}
	
	@Test
	@Disabled
	def void testTranslationWithMultiCardinality() {
		val code = '''
	    type Foo:
	    	a string (0..*)
	    
	    type Bar:
	    	bs Qux (0..*)
	    
	    type Qux:
	    	cs string (0..*)
	    
	    translate source FooBar {
	        translate Bar to Foo {
	        	a: [
	        			translate bs, [1, 2] to string,
	        			"Another string"
	        		]
	        }
	        
	        translate qux Qux, context number to string:
	        	qux -> cs join ", " + ": " + context to-string
	    }
		'''.generateCode
		
		val classes = code.compileToClasses
        
        val bar = classes.createInstanceUsingBuilder("Bar", #{
	    		"bs" -> #[
	    			classes.createInstanceUsingBuilder("Qux", #{
		    			"cs" -> #["a", "b"]
		    		}),
		    		classes.createInstanceUsingBuilder("Qux", #{
		    			"cs" -> #[]
		    		}),
		    		classes.createInstanceUsingBuilder("Qux", #{
		    			"cs" -> #["This", "is", "ignored"]
		    		})
	    		]
	        })
	    val expectedResult = classes.createInstanceUsingBuilder("Foo", #{
	    		"a" -> #["a, b: 1", ": 2", "Another string"]
	        })
        
        val translation = classes.createTranslation("FooBar", #["Bar"], "Foo");
        assertEquals(expectedResult, translation.invokeFunc(expectedResult.class, #[bar]))
	}
	
	@Test
	def void testTranslationWithMultiCardinality2() {
		val code = '''
	    type Foo:
	    	a string (0..*)
	    
	    type Bar:
	    	bs Qux (0..*)
	    
	    type Qux:
	    	cs string (0..*)
	    
	    translate source FooBar {
	        translate Bar to Foo {
	        	a: [
	        			translate bs to string,
	        			"Another string"
	        		]
	        }
	        
	        translate qux Qux to string:
	        	qux -> cs join ", "
	    }
		'''.generateCode
		
		val classes = code.compileToClasses
        
        val bar = classes.createInstanceUsingBuilder("Bar", #{
	    		"bs" -> #[
	    			classes.createInstanceUsingBuilder("Qux", #{
		    			"cs" -> #["a", "b"]
		    		}),
		    		classes.createInstanceUsingBuilder("Qux", #{
		    			"cs" -> #[]
		    		}),
		    		classes.createInstanceUsingBuilder("Qux", #{
		    			"cs" -> #["This", "is", "ignored"]
		    		})
	    		]
	        })
	    val expectedResult = classes.createInstanceUsingBuilder("Foo", #{
	    		"a" -> #["a, b", "", "This, is, ignored", "Another string"]
	        })
        
        val translation = classes.createTranslation("FooBar", #["Bar"], "Foo");
        assertEquals(expectedResult, translation.invokeFunc(expectedResult.class, #[bar]))
	}
	
	@Test
	@Disabled
	def void testTranslationWithMetadata() {
		val code = '''
	    metaType key string
	    metaType id string
	    metaType reference string
	    metaType template string
	    metaType location string
	    metaType address string
	    
	    type Foo:
	    	[metadata key]
	    	a int (1..1)
	    		[metadata id]
	    	self Foo (1..1)
	    		[metadata reference]
	    	value string (0..1)
	    		[metadata scheme]
	    
	    type Bar:
	    	[metadata key]
	    	[metadata template]
	    	a int (1..1)
	    		[metadata address]
	    	b Foo (1..1)
	    		[metadata location]
	    
	    type Inp:
	    
	    translate source FooBar {
	        Foo from Inp:
	        	[meta key from "self"]
	        	+ a
	           		[from 42]
	           		[meta id from "favoriteNumber"]
	           	+ self
	           		[meta reference from "self"]
	           	+ value
	           		[from "a"]
	           		[meta scheme from "schemeA"]
	        
	        Bar from Inp:
	        	[meta key from "My key"]
	        	[meta template from "My template"]
	        	+ a
	        		[from 42]
	        		[meta address from "Some address"]
	        	+ b
	        		[from item]
	        		[meta location from "My location"]
	    }
		'''.generateCode
				
		val classes = code.compileToClasses
        
        val inp = classes.createInstanceUsingBuilder("Inp", #{})
	    val expectedFoo = classes.createInstanceUsingBuilder("Foo", #{
	    		"meta" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "MetaFields", #{
					"externalKey" -> "self"
				}),
	    		"a" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "FieldWithMetaInteger", #{
					"value" -> 42,
					"meta" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "MetaFields", #{
    					"externalKey" -> "favoriteNumber"
    				})
				}),
				"self" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.test.model.metafields"), "ReferenceWithMetaFoo", #{
					"externalReference" -> "self"
				}),
				"value" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "FieldWithMetaString", #{
					"value" -> "a",
					"meta" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "MetaFields", #{
    					"scheme" -> "schemeA"
    				})
				})
	        })
	    val expectedBar = classes.createInstanceUsingBuilder("Bar", #{
		    	"meta" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "MetaAndTemplateFields", #{
					"externalKey" -> "My key",
					"template" -> "My template"
				}),
				"a" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "ReferenceWithMetaInteger", #{
					"value" -> 42,
					"reference" -> Reference.builder.setScope("DOCUMENT").setReference("Some address")
				}),
				"b" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.test.model.metafields"), "FieldWithMetaFoo", #{
					"value" -> expectedFoo,
					"meta" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "MetaFields", #{
						"key" -> #[Key.builder.setScope("DOCUMENT").setKeyValue("My location")]
					})
				})
		    })
        
        val translation = classes.createTranslation("FooBar", #["Inp"], "Bar");
        assertEquals(expectedBar, translation.invokeFunc(expectedBar.class, #[inp]))
	}
	
	@Test
	def void testTranslateSourceWithNestedRootElements() {
		val code = '''
	    type Foo:
	    	country string (0..1)
	    	amount number (1..1)
	    
	    type Bar:
	    	country string (1..1)
	    	payment Payment (1..1)
	    	complexAttribute number (1..1)
	    
	    type Payment:
	    	amount number (1..1)
	    	currency FooBar.CurrencyCodeEnum (1..1)
	    
	    translate source FooBar {
	        type Context:
	        	globalCurrency CurrencyCodeEnum (1..1)
	        	defaultCountry string (1..1)
	        
	        enum CurrencyCodeEnum:
	        	EUR
	        	GBP
	        	USD
	        
	        translate foo Foo, c Context to Bar {
	        	country: GetActualCountry(foo, c),
	        	payment: translate foo -> amount, c to Payment,
	        	complexAttribute: CustomMapper(foo, c)
	        }
	        
	        translate amount number, c Context to Payment {
	        	amount: amount,
	        	currency: c -> globalCurrency
	        }
	        
	        func GetActualCountry:
	        	inputs:
	        		foo Foo (1..1)
	        		context Context (1..1)
	        	output:
	        		country string (1..1)
	        	set country:
	        		foo -> country default context -> defaultCountry
	        
	        func CustomMapper:
	        	inputs:
	        		foo Foo (1..1)
	        		context Context (1..1)
	        	output:
	        		complexOutput number (1..1)
	    }
		'''.generateCode
				
		val classes = code.compileToClasses
        
        
	}
}