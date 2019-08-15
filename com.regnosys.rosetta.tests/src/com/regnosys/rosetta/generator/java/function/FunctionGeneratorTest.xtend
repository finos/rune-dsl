package com.regnosys.rosetta.generator.java.function

import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.calculation.ImportingStringConcatination
import com.regnosys.rosetta.rosetta.RosettaFunction
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class FunctionGeneratorTest {

	@Inject extension ModelHelper
	@Inject extension CodeGeneratorTestHelper
	@Inject extension FunctionGenerator generator
	
	@Inject JavaQualifiedTypeProvider.Factory factory
	
	var ImportingStringConcatination concatenator;
	
	@BeforeEach
	def void setup() {
		concatenator = new ImportingStringConcatination
	}
	
	@Test
	def void shouldGenerateEnrichMethod() {
		val javaNames = factory.create(javaPackages)
		
		val function = '''
			class Bar {}
			
			spec Foo:
				output:
					result Bar (1..1)
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction).head
		
		val result = function.contributeEnrichMethod(javaNames)
		concatenator.append(result)
		
		assertEquals('protected abstract Bar doEvaluate();', concatenator.toString.trim)
		assertThat(concatenator.imports, hasItems(endsWith('.Bar')))
	}
	
	@Test
	def void shouldGenerateEnrichMethodWhenBasicTypeOutput() {
		val javaNames = factory.create(javaPackages)
		
		val function = '''
			class Bar {}
			
			spec Foo:
				output:
					result number (1..1)
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction).head
		
		val result = function.contributeEnrichMethod(javaNames)
		concatenator.append(result)
		
		assertEquals('protected abstract BigDecimal doEvaluate();', concatenator.toString.trim)
		assertThat(concatenator.imports, hasItems(endsWith('.BigDecimal')))
	}
	
	@Test
	def void shouldGenerateEnrichMethodWhenListOutput() {
		val javaNames = factory.create(javaPackages)
		
		val function = '''
			class Bar {}
			
			spec Foo:
				output:
					result Bar (1..*)
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction).head
		
		val result = function.contributeEnrichMethod(javaNames)
		concatenator.append(result)
		
		assertEquals('protected abstract List<Bar> doEvaluate();', concatenator.toString.trim )
		assertThat(concatenator.imports, hasItems(endsWith('.Bar'), endsWith('.List')))
	}
	
	@Test
	def void shouldGenerateEnrichMethodArgumentsWhenInputsExist() {
		val javaNames = factory.create(javaPackages)
		
		val function = '''
			class Bar {}
			class Foo {}
			
			spec FooFunc:
				inputs:
					input1 Foo (1..1)
					input2 string (1..1)
					
				output:
					result Bar (1..1)
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction).head
		
		val result = function.contributeEnrichMethod(javaNames)
		concatenator.append(result)
		
		assertEquals('protected abstract Bar doEvaluate(Foo input1, String input2);', concatenator.toString.trim)
		assertThat(concatenator.imports, hasItems(endsWith('.Foo'), endsWith('.Bar')))
	}
	
	@Test
	def void shouldGenerateEnrichMethodArgumentsWhenInputsIsMultiple() {
		val javaNames = factory.create(javaPackages)
		
		val function = '''
			class Bar {}
			class Foo {}
			
			spec FooFunc:
				inputs:
					input1 Foo (1..1)
					input2 string (1..*)
					
				output:
					result Bar (1..1)
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction).head
		
		val result = function.contributeEnrichMethod(javaNames)
		concatenator.append(result)
		
		assertThat(concatenator.toString.trim, is('protected abstract Bar doEvaluate(Foo input1, List<String> input2);'))
		assertThat(concatenator.imports, hasItems(endsWith('.Foo'), endsWith('.Bar')))
	}
	
	@Test
	def void shouldGenerateEvaluateMethodWhenRosettaClassReturnType() {
		val javaNames = factory.create(javaPackages)
		
		val functions = '''
			class Bar {}
			class Foo {}
			
			spec FooFunc:
				inputs:
					input1 Foo (1..1)
					input2 string (1..1)
					
				output:
					result Bar (1..1)
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction)
		
		val fooFunc = functions.filter[name == 'FooFunc'].head
		val deps = functions.filter[name != 'FooFunc']
		
		val result = fooFunc.contributeEvaluateMethod(javaNames, deps)
		concatenator.append(result)
		
		val expected = '''
			/**
			 * @param input1 
			 * @param input2 
			 * @return result 
			 */
			public Bar evaluate(Foo input1, String input2) {
				
				// Delegate to implementation
				//
				Bar result = doEvaluate(input1, input2);
				
				return result;
			}
		'''
		assertEquals(expected.trim, concatenator.toString.trim)
		assertThat(concatenator.imports, hasItems(endsWith('.Bar'), endsWith('.Foo')))
	}
	
	@Test
	def void shouldGenerateEvaluateMethodWhenBasicReturnType() {
		val javaNames = factory.create(javaPackages)
		
		val functions = '''
			class Bar {}
			class Foo {}
			
			spec FooFunc:
				inputs:
					input1 Foo (1..1)
					input2 string (1..1)
					
				output:
					result number (1..1)
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction)
		
		val fooFunc = functions.filter[name == 'FooFunc'].head
		val deps = functions.filter[name != 'FooFunc']
		
		val result = fooFunc.contributeEvaluateMethod(javaNames, deps)
		concatenator.append(result)
		
		val expected = '''
				
			/**
			 * @param input1 
			 * @param input2 
			 * @return result 
			 */
			public BigDecimal evaluate(Foo input1, String input2) {
				
				// Delegate to implementation
				//
				BigDecimal result = doEvaluate(input1, input2);
				
				return result;
			}
		'''
		assertEquals(expected, concatenator.toString)
		assertThat(concatenator.imports, hasItems(endsWith('.Foo')))
	}
	
	@Test
	def void shouldGenerateEvaluateMethodWhenMultipleReturn() {
		val javaNames = factory.create(javaPackages)
		
		val functions = '''
			class Bar {}
			class Foo {}
			
			spec FooFunc:
				inputs:
					input1 Foo (1..1)
					input2 string (1..1)
					
				output:
					results Bar (1..*)
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction)
		
		val result = functions.head.contributeEvaluateMethod(javaNames, functions.tail)
		concatenator.append(result)
		
		val expected = '''
			/**
			 * @param input1 
			 * @param input2 
			 * @return results 
			 */
			public List<Bar> evaluate(Foo input1, String input2) {
				
				// Delegate to implementation
				//
				List<Bar> results = doEvaluate(input1, input2);
				
				return results;
			}
		'''
		assertEquals(expected.trim, concatenator.toString.trim)
		assertThat(concatenator.imports, hasItems(endsWith('.Foo')))
	}
	
	@Test
	def void shouldGeneratePreConditions() {
		val javaNames = factory.create(javaPackages)
		
		val function = '''
			spec FooFunc:
				inputs:
					input1 number (1..1)
					
				output:
					result number (1..1)
					
				pre-condition <"pre condition 1 should pass">:
					input1 > 42;
					input1 < 84;
					
				pre-condition <"pre condition 2 should pass">:
					input1 < 100;
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction).head
		
		val result = function.contributePreConditions(javaNames)
		concatenator.append(result)
		
		val expected = '''
			// pre-conditions
			//
			assert
				greaterThan(MapperS.of(input1), MapperS.of(Integer.valueOf(42))).get() &&
				lessThan(MapperS.of(input1), MapperS.of(Integer.valueOf(84))).get()
					: "pre condition 1 should pass";
			assert
				lessThan(MapperS.of(input1), MapperS.of(Integer.valueOf(100))).get()
					: "pre condition 2 should pass";
		'''

		assertEquals(expected.trim, concatenator.toString.trim)
	}
	
	
	@Test
	def void shouldCompileWhenPreConditions() {		
		'''
			spec FooFunc:
				inputs:
					input1 number (1..1)
					
				output:
					result number (1..1)
					
				pre-condition <"pre condition 1 should pass">:
					input1 > 42;
					input1 < 84;
					
				pre-condition <"pre condition 2 should pass">:
					input1 < 100;
		'''.compileJava8
	}
	
	@Test
	def void shouldGeneratePostConditions() {
		val javaNames = factory.create(javaPackages)
		
		val function = '''
			spec FooFunc:
				inputs:
					input1 number (1..1)
					
				output:
					result number (1..1)
					
				post-condition <"post condition 1 should pass">:
					input1 > 42;
					input1 < 84;
					
				post-condition <"post condition 2 should pass">:
					input1 < 100;
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction).head
		
		val result = function.contributePostConditions(javaNames)
		concatenator.append(result)
		
		val expected = '''
		// post-conditions
		//
		assert
			greaterThan(MapperS.of(input1), MapperS.of(Integer.valueOf(42))).get() &&
			lessThan(MapperS.of(input1), MapperS.of(Integer.valueOf(84))).get()
				: "post condition 1 should pass";
		assert
			lessThan(MapperS.of(input1), MapperS.of(Integer.valueOf(100))).get()
				: "post condition 2 should pass";
		'''
		
		assertEquals(expected.trim, concatenator.toString.trim)
	}
	
	@Test
	def void shouldGenerateJavadoc() {		
		val function = '''
			spec FooFunc <"an example function level comment">:
				inputs:
					input1 number (1..1) <"is the one and only input">
					
				output:
					result number (1..1) <"is the number that is returned">
					
				post-condition <"post condition 1 should pass">:
					input1 > 42;
					input1 < 84;
					
				post-condition <"post condition 2 should pass">:
					input1 < 100;
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction).head
		
		val result = function.contributeJavaDoc
		concatenator.append(result)
		
		val expected = '''
			/**
			 * an example function level comment
			 */
		'''
		
		assertEquals(expected, concatenator.toString)
	}
	
	@Test
	def void shouldGenerateConstructor() {
		val javaNames = factory.create(javaPackages)
		
		val functions = '''
			spec FooFunc <"an example function level comment">:
				inputs:
					input1 number (1..1) <"is the one and only input">
					
				output:
					result number (1..1) <"is the number that is returned">
					
				post-condition <"post condition 1 should pass">:
					input1 > 42;
					input1 < 84;
					
				post-condition <"post condition 2 should pass">:
					input1 < BarFunc();
			
			spec BarFunc:
				output:
					result number (1..1)
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction)
		
		val result = functions.head.contributeConstructor(javaNames)
		concatenator.append(result)
		
		val expected = '''
			protected FooFunc(ClassToInstanceMap<RosettaFunction> classRegistry) {
				
				// On concrete instantiation, register implementation with function to implementation container
				//
				classRegistry.putInstance(FooFunc.class, this);
				this.classRegistry = classRegistry;	
			}
		'''
		
		assertEquals(expected.trim, concatenator.toString.trim)
		assertThat(concatenator.imports, hasItems(endsWith('.ClassToInstanceMap'), endsWith('.RosettaFunction')))
	}
	
	@Test
	def void shouldGenerateConstructorWhenNoDependencies() {
		val javaNames = factory.create(javaPackages)
		
		val function = '''
			spec FooFunc <"an example function level comment">:
				inputs:
					input1 number (1..1) <"is the one and only input">
					
				output:
					result number (1..1) <"is the number that is returned">
					
				post-condition <"post condition 1 should pass">:
					input1 > 42;
					input1 < 84;
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction).head
				
		val result = function.contributeConstructor(javaNames)
		concatenator.append(result)
		
		val expected = '''
		
		protected FooFunc(ClassToInstanceMap<RosettaFunction> classRegistry) {
			
			// On concrete instantiation, register implementation with function to implementation container
			//
			classRegistry.putInstance(FooFunc.class, this);
			this.classRegistry = classRegistry;	
		}
		'''
		
		assertEquals(expected, concatenator.toString)
	}
	
	@Test
	def void shouldGenerateFields() {
		val javaNames = factory.create(javaPackages)
		
		val result = contributeFields(javaNames)
		concatenator.append(result)
		
		val expected = '''protected final ClassToInstanceMap<RosettaFunction> classRegistry;'''
		
		assertEquals(expected.trim, concatenator.toString.trim)
		assertThat(concatenator.imports, hasItems(endsWith('.ClassToInstanceMap'), endsWith('.RosettaFunction')))
	}
	
	@Test
	def void shouldGenerateConditionWhenFunctionCall() {
		val javaNames = factory.create(javaPackages)
		
		val functions = '''
			spec FooFunc <"an example function level comment">:
				inputs:
					object1 Object1 (1..1) <"is the one and only input">
					object2 Object2 (1..1)
					
				output:
					result number (1..1) <"is the number that is returned">
					
				pre-condition <"post condition 1 should pass">:
					if object1 exists then
						object1 -> object2 -> result = BuzzFunc( object2 -> result ) and
						object1 -> object2 -> result = BarFunc( object2 -> result )
					else 
						object1 -> object2 is absent and
						object2 -> result count = 1;
			
			spec BuzzFunc:
				inputs:
					object2 Object2 (1..1)
				output:
					result number (1..1)
					
			spec BarFunc:
				inputs:
					object2 Object2 (1..1)
				output:
					result number (1..1)
			
			class Object1 {
				object2 Object2 (1..1);
			}
			
			class Object2 {
				result number (1..1);
			}
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction)
		
		val fooFunc = functions.filter[name == 'FooFunc'].head
		
		val result = fooFunc.contributePreConditions(javaNames)
		concatenator.append(result)
		
		val expected = '''
		// pre-conditions
		//
		assert
			doIf(exists(MapperS.of(object1), false),areEqual(MapperS.of(object1).<Object2>map("getObject2", Object1::getObject2).<BigDecimal>map("getResult", Object2::getResult), MapperS.of(buzzFunc.evaluate(MapperS.of(object2).<BigDecimal>map("getResult", Object2::getResult).get()))).and(areEqual(MapperS.of(object1).<Object2>map("getObject2", Object1::getObject2).<BigDecimal>map("getResult", Object2::getResult), MapperS.of(barFunc.evaluate(MapperS.of(object2).<BigDecimal>map("getResult", Object2::getResult).get())))),notExists(MapperS.of(object1).<Object2>map("getObject2", Object1::getObject2)).and(areEqual(MapperS.of(MapperS.of(object2).<BigDecimal>map("getResult", Object2::getResult).resultCount()), MapperS.of(Integer.valueOf(1))))).get()
				: "post condition 1 should pass";
		'''
		
		assertEquals(expected.trim, concatenator.toString.trim)
	}
	
	@Test
	def void shouldGenerateConditionWhenFunctionCalledInsideBUiltInFunction() {
		val javaNames = factory.create(javaPackages)
		
		val functions = '''
			spec FooFunc <"an example function level comment">:
				inputs:
					object1 Object1 (1..1) <"is the one and only input">
					input2 Object1 (0..1)
				output:
					result number (1..1) <"is the number that is returned">
					
				pre-condition <"post condition 1 should pass">:
					BuzzFunc(object1 -> object2, input2) > 23;
			
			spec BuzzFunc:
				inputs:
					input1 Object2 (1..1)
					input2 number (1..*)
				output:
					result number (1..1)
			
			class Object1 {
				object2 Object2 (1..1);
				object3 Object3 (1..*);
			}
			
			class Object2 {
				result number (1..1);
			}
			
			class Object3 {
				result number (1..1);
			}
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction)
		
		val fooFunc = functions.filter[name == 'FooFunc'].head
		
		val result = fooFunc.contributePreConditions(javaNames)
		concatenator.append(result)
		
		val expected = '''
		// pre-conditions
		//
		assert
			greaterThan(MapperS.of(buzzFunc.evaluate(MapperS.of(object1).<Object2>map("getObject2", Object1::getObject2).get(), MapperS.of(input2).get())), MapperS.of(Integer.valueOf(23))).get()
				: "post condition 1 should pass";
		'''
		
		assertEquals(expected.trim, concatenator.toString.trim)
	}
	
	@Disabled @Test // TODO Why are you expecting LocalDate being imported into FooFunc?
	def void shouldImportLocalDateWhenUsedInExpression() {
		val javaNames = factory.create(javaPackages)
		
		val functions = '''
			spec FooFunc <"an example function level comment">:
				inputs:
					input1 Object1 (1..1) <"is the one and only input">
				output:
					result number (1..1) <"is the number that is returned">
					
				pre-condition <"post condition 1 should pass">:
					input1 -> object2 -> result exists;
			
			class Object1 {
				object2 Object2 (1..1);
			}
			
			class Object2 {
				result date (1..1);
			}
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction)
		
		val fooFunc = functions.filter[name == 'FooFunc'].head
		
		val result = fooFunc.contributePreConditions(javaNames)
		concatenator.append(result)
		
		assertThat(concatenator.imports, hasItems(endsWith('java.time.LocalDate')))
	}
	
	@Test
	def void shouldUseLocalDate() {
		val javaNames = factory.create(javaPackages)
		
		val functions = '''
			spec FooFunc <"an example function level comment">:
				inputs:
					input1 date (1..1) <"is the one and only input">
				output:
					result date (1..1) <"is the number that is returned">
		'''.parseRosettaWithNoErrors.elements.filter(RosettaFunction)
		
		val fooFunc = functions.filter[name == 'FooFunc'].head
		
		val result = fooFunc.contributeEnrichMethod(javaNames)
		concatenator.append(result)
		
//		println(concatenator.imports)
//		println(concatenator.toString)
		
		assertThat(concatenator.imports, hasItems(endsWith('java.time.LocalDate')))
	}
	
	@Test
	def void shouldGenerateCode() {		
		val functions = '''
			spec FooFunc <"an example function level comment">:
				inputs:
					input1 number (1..1) <"is the one and only input">
					
				output:
					result number (1..1) <"is the number that is returned">
					
				post-condition <"post condition 1 should pass">:
					input1 > 42;
					input1 < BuzzFunc();
					
				post-condition <"post condition 2 should pass">:
					input1 < BarFunc();
			
			spec BarFunc:
				output:
					result number (1..1)
			
			spec BuzzFunc:
				output:
					result number (1..1)
		'''.generateCode(generator)
		
//		val fooFunc = functions.get(javaPackages.functions.packageName + '.FooFunc')
//		println(fooFunc)
	}
	
	
	@Test
	def void testFunctionGeneration() {
		assertEquals(
			'''
			package com.rosetta.test.model.functions;
			
			import com.rosetta.model.lib.meta.FieldWithMeta;
			
			import org.isda.cdm.*;
						
			import static com.rosetta.model.lib.validation.ValidatorHelper.*;
			
			import com.google.common.collect.ClassToInstanceMap;
			import com.rosetta.model.lib.functions.MapperS;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import java.lang.Integer;
			import java.lang.String;
			
			public abstract class FuncFoo implements RosettaFunction {
				
				protected final ClassToInstanceMap<RosettaFunction> classRegistry;
				
				protected FuncFoo(ClassToInstanceMap<RosettaFunction> classRegistry) {
					
					// On concrete instantiation, register implementation with function to implementation container
					//
					classRegistry.putInstance(FuncFoo.class, this);
					this.classRegistry = classRegistry;	
				}
					
				/**
				 * @param result 
				 * @param result2 
				 * @return out 
				 */
				public Integer evaluate(Integer result, String result2) {
					
					// Delegate to implementation
					//
					 Integer out = doEvaluate(result, result2);
					// post-conditions
					//
					assert
						greaterThan(MapperS.of(result), MapperS.of(Integer.valueOf(42))).get()
							: "";
					return out;
				}
					
				protected abstract Integer doEvaluate(Integer result, String result2);
			}
			'''.toString,
			'''
				func FuncFoo:
					inputs:
						result int (1..1)
						result2 string (1..1)
					
					output:
						out int(1..1)
					post-condition: result > 42;
			'''.generateCode(generator).get('com.rosetta.test.model.functions.FuncFoo')
		)
	}
}
