package com.regnosys.rosetta.generator.java.validator

import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.junit.jupiter.api.Test
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper

import static com.google.common.collect.ImmutableMap.*
import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ValidatorGeneratorTest {
	@Inject extension CodeGeneratorTestHelper
	
	@Test
	def void validatorTest() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			type Foo:
				a int (0..1)
				
				condition C:
				    it -> a exists and [it, it] any = it
		'''.generateCode
		
		val valCode = code.get("com.rosetta.test.model.validation.FooValidator")
		assertEquals(
			'''
			package com.rosetta.test.model.validation;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.functions.RosettaFunction;
			
			
			@ImplementedBy(Foo.FooDefault.class)
			public abstract class Foo implements RosettaFunction {
			
				/**
				* @param func 
				* @return result 
				*/
				public Integer evaluate(Integer func) {
					Integer result = doEvaluate(func);
					
					return result;
				}
			
				protected abstract Integer doEvaluate(Integer func);
			
				public static class FooDefault extends Foo {
					@Override
					protected Integer doEvaluate(Integer func) {
						Integer result = null;
						return assignOutput(result, func);
					}
					
					protected Integer assignOutput(Integer result, Integer func) {
						result = func;
						
						return result;
					}
				}
			}
			'''.toString,
			valCode
		)
		
		val classes = code.compileToClasses
		
	}
}