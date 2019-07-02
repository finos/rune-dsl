package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.rosetta.model.lib.meta.RosettaMetaData
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
	
	@Test
	def void shouldGenerateGetQualiifyFunctions() {
		val code = '''
			class Foo
			{
				a string (0..1);
			}
			
			class Bar extends Foo
			{
				b string (0..1);
			}
			
			isEvent AExists
				Foo -> a exists
			
			isEvent AEqualsSomeValue
				Foo -> a = "someValue"
		'''.generateCode
		//code.writeClasses("shouldGenerateGetQualiifyFunctions")
		val classes = code.compileToClasses

		val fooMeta = RosettaMetaData.cast(classes.get(javaPackages.meta.packageName + '.FooMeta').newInstance)
		assertThat(fooMeta.qualifyFunctions.size, is(2))
		
		val barMeta = RosettaMetaData.cast(classes.get(javaPackages.meta.packageName + '.BarMeta').newInstance)
		assertThat(barMeta.qualifyFunctions.size, is(2))
		
	}
	
}