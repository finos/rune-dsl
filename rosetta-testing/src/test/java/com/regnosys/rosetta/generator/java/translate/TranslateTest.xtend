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
	        Foo from Bar:
	        	+ a
	           		[from b, 42]
	        
	        string from Qux, context number:
	        	[from c + context to-string]
	    }
		'''.generateCode
		
		val classes = code.compileToClasses
        
        val bar = classes.createInstanceUsingBuilder("Bar", #{
	    		"b" -> classes.createInstanceUsingBuilder("Qux", #{
	    			"c" -> "My favorite number is "
	    		})
	        })
	    val expectedResult = classes.createInstanceUsingBuilder("Foo", #{
	    		"a" -> "My favorite number is 42"
	        })
        
        val translation = classes.createTranslation("FooBar", #["Bar"], "Foo");
        assertEquals(expectedResult, translation.invokeFunc(expectedResult.class, #[bar]))
	}
	
	@Test
	def void testTranslationWithMultiCardinality() {
		val code = '''
	    type Foo:
	    	a string (0..*)
	    
	    type Bar:
	    	bs Qux (0..*)
	    
	    type Qux:
	    	cs string (0..*)
	    
	    translate source FooBar {
	        Foo from Bar:
	        	+ a
	           		[from bs, [1, 2]]
	        
	        string from Qux, context number:
	        	[from cs join ", " + ": " + context to-string]
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
	    		"a" -> #["a, b: 1", ": 2"]
	        })
        
        val translation = classes.createTranslation("FooBar", #["Bar"], "Foo");
        assertEquals(expectedResult, translation.invokeFunc(expectedResult.class, #[bar]))
	}
	
	@Test
	def void testTranslationWithMetadata() {
		val code = '''
	    metaType key string
	    metaType id string
	    metaType reference string
	    
	    type Foo:
	    	[metadata key]
	    	a int (1..1)
	    		[metadata id]
	    	self Foo (1..1)
	    		[metadata reference]
	    	value string (0..*)
	    		[metadata scheme]
	    
	    type Bar:
	    	v string (1..1)
	    	t string (1..1)
	    
	    translate source FooBar {
	        Foo from Bar:
	        	[meta key from "self"]
	        	+ a
	           		[from 42]
	           		[meta id from "favoriteNumber"]
	           	+ self
	           		[meta reference from "self"]
	           	+ value
	           		[from ["a", "b", "c"]]
	           		[meta scheme from ["schemeA", "schemeB"]]
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
	    		"meta" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "MetaFields", #{
					"key" -> #[Key.builder.setKeyValue("self")]
				}),
	    		"a" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "FieldWithMetaInteger", #{
					"value" -> 42,
					"meta" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "MetaFields", #{
    					"id" -> "favoriteNumber"
    				})
				}),
				"self" -> classes.createInstanceUsingBuilder("Foo", #{
					"meta" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "MetaFields", #{
    					"reference" -> Reference.builder.setReference("self")
    				})
				}),
				"value" -> #[
					classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "FieldWithMetaString", #{
						"value" -> "a",
						"meta" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "MetaFields", #{
	    					"scheme" -> "schemeA"
	    				})
					}),
					classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "FieldWithMetaString", #{
						"value" -> "b",
						"meta" -> classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "MetaFields", #{
	    					"scheme" -> "schemeB"
	    				})
					}),
					classes.createInstanceUsingBuilder(new RootPackage("com.rosetta.model.metafields"), "FieldWithMetaString", #{
						"value" -> "c"
					})
				]
	        })
        
        val translation = classes.createTranslation("FooBar", #["Bar"], "Foo");
        assertEquals(expectedResult, translation.invokeFunc(expectedResult.class, #[bar]))
	}
	
}