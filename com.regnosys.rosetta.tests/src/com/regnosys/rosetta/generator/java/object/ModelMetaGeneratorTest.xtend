package com.regnosys.rosetta.generator.java.object

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.rosetta.model.lib.functions.ConditionValidator
import com.rosetta.model.lib.functions.NoOpConditionValidator
import com.rosetta.model.lib.meta.RosettaMetaData
import com.rosetta.model.lib.qualify.QualifyFunctionFactory
import com.rosetta.model.lib.validation.ModelObjectValidator
import com.rosetta.model.lib.validation.NoOpModelObjectValidator
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ModelMetaGeneratorTest {
	
	@Inject extension ModelHelper
	@Inject extension CodeGeneratorTestHelper
	
	final QualifyFunctionFactory funcFactory
	
	new() {
		// don't use the Language Injector. This is the Test env for the model.
		funcFactory = Guice.createInjector(new AbstractModule() {

			override protected configure() {
				bind(ConditionValidator).toInstance(new NoOpConditionValidator)
				bind(ModelObjectValidator).toInstance(new NoOpModelObjectValidator)
			}
		}).getInstance(QualifyFunctionFactory.Default)
	}
	
	@Test
	def void shouldGenerateGetQualiifyFunctions() {
		val code = '''
			isEvent root Foo;
			
			type Foo:
				a string (0..1)
			
			type Bar extends Foo:
				b string (0..1)
			
			func Qualify_AExists:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				set is_event:
					foo -> a exists
			
			func Qualify_AEqualsSomeValue:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				set is_event:
					foo -> a = "someValue"
		'''.generateCode
		//code.writeClasses("shouldGenerateGetQualiifyFunctions")
		val classes = code.compileToClasses

		val fooMeta = RosettaMetaData.cast(classes.get(rootPackage.meta.name + '.FooMeta').newInstance)
		assertThat(fooMeta.getQualifyFunctions(funcFactory).size, is(2))
		
		val barMeta = RosettaMetaData.cast(classes.get(rootPackage.meta.name + '.BarMeta').newInstance)
		assertThat(barMeta.getQualifyFunctions(funcFactory).size, is(0))
		
	}
	
}