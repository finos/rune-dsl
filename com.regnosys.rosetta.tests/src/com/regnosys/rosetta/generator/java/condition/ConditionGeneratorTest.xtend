package com.regnosys.rosetta.generator.java.condition

import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.junit.jupiter.api.Test
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper

import static com.google.common.collect.ImmutableMap.*
import static org.junit.jupiter.api.Assertions.*
import com.regnosys.rosetta.generator.java.rule.ConditionTestHelper

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ConditionGeneratorTest {
	@Inject extension CodeGeneratorTestHelper
	@Inject extension ConditionTestHelper
	
	@Test
	def void omittedParameterInConditionTest() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			type Foo:
				a int (0..1)
				
				condition C:
				    FooIsValid
			
			func FooIsValid:
				inputs: foo Foo (1..1)
				output: result boolean (1..1)
				set result:
					foo -> a exists
		'''.generateCode
		val classes = code.compileToClasses
		
		val foo1 = classes.createInstanceUsingBuilder('Foo', of('a', 42))
		assertTrue(classes.runDataRule(foo1, 'FooC').isSuccess)
		
		val foo2 = classes.createInstanceUsingBuilder('Foo', of())
		assertFalse(classes.runDataRule(foo2, 'FooC').isSuccess)
	}
	
	@Test
	def void useImplicitVariableInConditionTest() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			type Foo:
				a int (0..1)
				
				condition C:
				    it -> a exists and [it, it] any = it
		'''.generateCode
		val classes = code.compileToClasses
		
		val foo1 = classes.createInstanceUsingBuilder('Foo', of('a', 42))
		assertTrue(classes.runDataRule(foo1, 'FooC').isSuccess)
		
		val foo2 = classes.createInstanceUsingBuilder('Foo', of())
		assertFalse(classes.runDataRule(foo2, 'FooC').isSuccess)
	}
}