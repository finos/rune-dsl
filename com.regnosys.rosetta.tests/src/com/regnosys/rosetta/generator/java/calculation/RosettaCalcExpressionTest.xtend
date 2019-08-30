package com.regnosys.rosetta.generator.java.calculation

import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.util.ImportingStringConcatination
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.rosetta.RosettaArgumentFeature
import com.regnosys.rosetta.rosetta.RosettaCalculation
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaCalcExpressionTest {

	@Inject extension RosettaToJavaExtensions
	@Inject extension ModelHelper
	@Inject extension CodeGeneratorTestHelper
	@Inject JavaNames.Factory factory
	
	@Test
	def void shouldResolveEnumValue() {
		val model = '''
			enum Foo {
				BAR, BAZ
			}
			
			class Bar {
				bar Foo (1..1);
			}
			
			calculation Fizz {
				defined by : buzz
				
				where 
					buzz: 
						if Bar -> bar = Foo.BAR then 1 else 0
			}
		'''.parseRosettaWithNoErrors
		
		val javaNames = factory.create(model)
		
		val expression = model.eContents.filter(RosettaCalculation)
			.flatMap[arguments.features]
			.filter(RosettaArgumentFeature)
			.map[expression]
			.head
					
		val java = toJava(javaNames, expression)
		
		val concat = new ImportingStringConcatination()
		concat.append(java)
		val imports = concat.imports
		
		assertThat(concat.toString, is('Objects.equals(inputParam.getBar(), Foo.BAR) ? 1 : 0'))
		assertThat(imports, hasItem('com.rosetta.test.model.Foo'))
	}
	
	@Test
	def void shouldCompileArgumentsReferringOtherArguments() {
		// arg: fuzz refers to arg: buzz
		
		'''
			class Foo {
				wizz number (1..1);
			}
			
			class Bar {
				bar Foo (1..1);
			}
			
			calculation Fizz {
				: fuzz * fuzz
				
				where 
					buzz: 
						Bar -> bar
					fuzz: 
						buzz -> wizz 
			}
		'''.generateCode.compileToClasses
	}
	
	@Test
	def void shouldGenerateExistsExpressionInsideConditional() {
		val model = '''
			class Period {
				frequency int (1..1);
			}

			calculation DayFraction {
				res defined by: p / 360
				
				where
					period : Period
					p : if Period -> frequency exists then 1 else 0
			}
		'''.parseRosettaWithNoErrors
		
		val javaNames = factory.create(model)
		
		val calculation = model.eContents.filter(RosettaCalculation).head
		val conditional = calculation.arguments.features.filter(RosettaArgumentFeature)
			.map[expression].filter(RosettaConditionalExpression).head
		
		val java = toJava(javaNames, conditional)
		
		val concat = new ImportingStringConcatination()
		concat.append(java)
		val staticImports = concat.staticImports
				
		println(concat.toString)
		
		assertThat(staticImports, hasItems('com.rosetta.model.lib.functions.ExpressionOperators.exists'))
		assertThat(concat.toString, is('exists(inputParam.getFrequency()) ? 1 : 0'))		
	}
	
	@Test
	def void shouldCompile() {
		'''
			class Period {
				frequency int (1..1);
			}
			
			function Blah() {
				result number;
			}

			calculation DayFraction {
				res defined by: p / 360
				
				where
					p : if Period -> frequency exists then 1 else 0
					q : if Blah() -> result exists then 1 else 0
			}
		'''.compileJava8
		
//		println(code.get('com.rosetta.test.model.calculation.DayFraction'))
	}
	
	@Test
	def void shouldCompile3() {
		'''
			function Blah(n number) {
				n number;
			}
			
			function Foo() {
				n number;
			}

			calculation DayFraction {
				res defined by: q / 360
				
				where
					q : if Blah( Foo() -> n ) -> n exists then 1 else 0
			}
		'''.compileJava8
		
//		println(code.get('com.rosetta.test.model.calculation.DayFraction'))
	}
}
