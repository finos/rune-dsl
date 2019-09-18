package com.regnosys.rosetta.generator.java.calculation

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.regnosys.rosetta.validation.RosettaIssueCodes
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.functions.IResult
import java.math.BigDecimal
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.core.Is.is

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaCalculationTest  implements RosettaIssueCodes {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ValidationTestHelper
	@Inject extension ModelHelper
	
	@Test
	def checkEnumCalculationName() {
		val model = '''
			enum Foo {
				BAR, BAZ
			}
			
			calculation Foo.BAR {
			}
			
			calculation Foo.FOOBAR {
			}
		'''.parseRosetta
		model.elements.last.assertError(ROSETTA_CALCULATION, MISSING_ENUM_VALUE, "Cannot find enum value 'Foo.FOOBAR'")
	}

	@Test
	def checkEnumCalculationConverison() {
		val model = '''
			class C calculation Calc {
				foo Foo (1..1);
			}
			
			enum Foo {
				BAR, BAZ
			}
			
			calculation Calc {
				defined by: 1 + fooArg 
				
				where
					fooArg: is C -> foo
			}
			
			calculation Foo.BAR {
				defined by: 1 
			}
		'''.parseRosetta
		model.assertNoErrors
	}
	
	@Test
	def checkEnumCalculationConverison1() {
		val model = '''
			class C calculation Calc {
				foo Foo (1..1);
			}
			
			enum Foo {
				BAR, BAZ
			}
			
			calculation Calc {
				number: 1 + fooArg 
				
				where
					fooArg: is C -> foo
			}
		'''.parseRosetta
		model.assertError(ROSETTA_CALCULATION_FEATURE, TYPE_ERROR)
	}
	
	
	@Test
	def checkEnumCalculationReturnTypes() {
		val model = '''
			enum Foo {
				BAR, BAZ
			}
			
			calculation Foo.BAR {
				int res: 1 
			}
			
			calculation Foo.BAZ {
				number res: 1 
			}
			
		'''.parseRosetta
		model.assertError(ROSETTA_CALCULATION, TYPE_ERROR)
	}
	
	@Test
	def checkOperations() {
		val model = '''
			calculation Foo {
				int intInt:     1 + 1
				int intInt2:    1 - 1
				int intInt3:    1 * 1
				number intInt4: 1 / 1
				
				number intBd  : 1 + 1.0
				number bdInt  : 1.0 + 1
				number bdInt2 : 1.0 - 1
				number bdInt3 : 1.0 * 1
				number bdInt4 : 1.0 / 1
			}'''.parseRosetta
		model.assertNoErrors
	}
	
	@Test
	def checkEnumCalculationReturnTypes1() {
		val model = '''
			enum Foo {
				BAR, BAZ
			}
			
			calculation Foo.BAR {
				int : 1 
			}
			
			calculation Foo.BAZ {
				int : 1 
			}
			
		'''.parseRosetta
		model.assertNoErrors
	}
	
	@Test
	def void shouldCalculateResultFromNumberInputs() {
		val code = '''
			class Foo {
				a number (1..1);
				b number (1..1);
			}
						
			func FooNumberCalc:
				inputs: f Foo(1..1)
				output:
					out number (1..1)
				alias aArg: f -> a
				alias bArg: f -> b
				
				assign-output  out : 1 + aArg + bArg 
		'''.generateCode
		
		val classes = code.compileToClasses
		
		val foo = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('a', BigDecimal.valueOf(2), 'b', BigDecimal.valueOf(3))))
		val fooCalc = classes.createCalculationInstance('FooNumberCalc')
		val value = calculate(fooCalc, foo)
		
		assertThat('Unexpected calculation result', value, is(BigDecimal.valueOf(6)))
	}
	
	@Test
	def void shouldCalculateResultFromIntInputsWithIfCalc() {
		val code = '''
			class Foo {
				a int (1..1);
				b int (1..1);
			}
			
			func FooIntCalc:
				inputs: f Foo(1..1)
				output:
					out int (1..1)
				alias aArg: f -> a
				alias bArg: f -> b
				
				assign-output  out : if aArg > 0 then aArg * 5 else bArg + 5
		'''.generateCode
		
		val classes = code.compileToClasses
		
		val fooThen = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('a', 2, 'b', 3)))
		val fooCalcThen = classes.createCalculationInstance('FooIntCalc')
		val valueThen = calculate(fooCalcThen, fooThen)
		
		assertThat('Unexpected calculation result', valueThen, is(10))
		
		val fooElse = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('a', -2, 'b', 10)))
		val fooCalcElse = classes.createCalculationInstance('FooIntCalc')
		val valueElse = calculate(fooCalcElse, fooElse)
		
		assertThat('Unexpected calculation result', valueElse, is(15))
	}
	
	@Test
	def void shouldCalculateResultFromNumberInputsWithIfCalc() {
		val code = '''
			class Foo {
				a number (1..1);
				b number (1..1);
			}
			
			func FooNumberCalc:
				inputs: f Foo(1..1)
				output:
					out number (1..1)
				alias aArg: f -> a
				alias bArg: f -> b
				
				assign-output  out : if aArg >= 0.0 then aArg * 5.0 else bArg / 5
		'''.generateCode
		
		val classes = code.compileToClasses
		
		val fooThen = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('a', BigDecimal.valueOf(0), 'b', BigDecimal.valueOf(3))))
		val fooCalcThen = classes.createCalculationInstance('FooNumberCalc')
		val valueThen = calculate(fooCalcThen, fooThen)
		
		assertThat('Unexpected calculation result', valueThen, is(BigDecimal.valueOf(0.0)))
		
		val fooElse = RosettaModelObject.cast(classes.createInstanceUsingBuilder('Foo', of('a', BigDecimal.valueOf(-2), 'b', BigDecimal.valueOf(6))))
		val fooCalcElse = classes.createCalculationInstance('FooNumberCalc')
		val valueElse = calculate(fooCalcElse, fooElse)
		
		assertThat('Unexpected calculation result', valueElse, is(BigDecimal.valueOf(1.2)))
	}

	def private calculate(Object calc, Object input) {
		val result =  calc.class.getMethod('evaluate', input.class).invoke(calc, input)
		result
	}
}
