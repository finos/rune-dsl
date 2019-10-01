package com.regnosys.rosetta.generator.java.calculation

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaFunctionGenerationTest {

	@Inject extension CalculationGeneratorHelper

	@Test
	def void testSimpleFunctionGeneration() {
		'''
			func FuncFoo:
			 	inputs:
			 		name string  (0..1)
					name2 string (0..1)
				output:
					result string (0..1)
		'''.assertToGeneratedFunction(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.functions.RosettaFunction;
			import java.lang.String;
			import java.lang.UnsupportedOperationException;
			
			
			@ImplementedBy(FuncFoo.FuncFooDefault.class)
			public abstract class FuncFoo implements RosettaFunction {
			
				/**
				* @param name 
				* @param name2 
				* @return result 
				*/
				public String evaluate(String name, String name2) {
					
					String result = doEvaluate(name, name2);
					
					return result;
				}
				
				protected abstract String doEvaluate(String name, String name2);
				
				public static final class FuncFooDefault extends FuncFoo {
					@Override
					protected  String doEvaluate(String name, String name2) {
						throw new UnsupportedOperationException();
					}
				}
			}
			'''
		)
	}

}
