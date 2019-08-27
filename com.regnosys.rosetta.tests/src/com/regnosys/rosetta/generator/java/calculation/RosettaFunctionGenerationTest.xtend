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
			function FuncFoo (name string, name2 string) {
				result string;
				result2 string;
			} 
		'''.assertToGeneratedFunction(
			'''
			package com.rosetta.test.model.functions;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.functions.IFunctionResult;
			import com.rosetta.model.lib.functions.IResult;
			import java.lang.String;
			import java.util.Arrays;
			import java.util.List;
			
			/**
			 * @version test
			 */
			@ImplementedBy(FuncFooImpl.class)
			public interface FuncFoo {
				
				CalculationResult execute(String name, String name2);
				
				class CalculationResult implements IFunctionResult {
				
				
					private String result;
					private String result2;
					
					public CalculationResult() {
					}
					public String getResult() {
						return this.result;
					}
					
					public CalculationResult setResult(String result) {
						this.result = result;
						return this;
					}
					
					public String getResult2() {
						return this.result2;
					}
					
					public CalculationResult setResult2(String result2) {
						this.result2 = result2;
						return this;
					}
					
					private static final List<Attribute<?>> ATTRIBUTES =  Arrays.asList(
						new Attribute<>("result", String.class, (IResult res) -> ((CalculationResult) res).getResult()),
						new Attribute<>("result2", String.class, (IResult res) -> ((CalculationResult) res).getResult2())
					);
				
					@Override
					public List<Attribute<?>> getAttributes() {
						return ATTRIBUTES;
					}
					
					@Override
					public boolean equals(Object o) {
						if (this == o) return true;
						if (o == null || getClass() != o.getClass()) return false;
					
						CalculationResult _that = (CalculationResult) o;
					
						if (result != null ? !result.equals(_that.result) : _that.result != null) return false;
						if (result2 != null ? !result2.equals(_that.result2) : _that.result2 != null) return false;
						return true;
					}
					
					@Override
					public int hashCode() {
						int _result = 0;
						_result = 31 * _result + (result != null ? result.hashCode() : 0);
						_result = 31 * _result + (result2 != null ? result2.hashCode() : 0);
						return _result;
					}
					
					@Override
					public String toString() {
						return "CalculationResult {" +
							"result=" + this.result + ", " +
							"result2=" + this.result2 +
						'}';
					}
				}
			}
			'''
		)
	}

}
