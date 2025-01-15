package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.*
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class GlobalKeyGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper

	@Test
	def void shouldGenerateGlobalKeyFieldAndGetterWhenSet() {
		val code = '''
			type WithGlobalKey:
				[metadata key]
				foo string (1..1)
		'''.generateCode
		//code.writeClasses("shouldGenerateGlobalKeyFieldAndGetterWhenSet")
		val classess = code.compileToClasses
		val withGlobalKey = classess.get(rootPackage + '.WithGlobalKey')

		assertThat(withGlobalKey.declaredMethods.map[name], hasItem('getMeta'))
	}

	@Test
	def void shouldNotGenerateFieldsAndGetterWhenNotDefined() {
		val code = '''
			type WithoutGlobalKeys:
				foo string (1..1)
		'''.generateCode

		val classess = code.compileToClasses
		val withoutGlobalKeys = classess.get(rootPackage + '.WithoutGlobalKeys')

		assertThat(withoutGlobalKeys.methods.map[name], not(hasItem('getMeta')))
	}
	
	// TODO fails when the metaType is moved to annotations.rosetta or basictypes.rosetta (and removed from the code here).
	@Test
	def void shouldGenerateGlobalReferenceField() {
		val code = '''
			metaType reference string
			
			type Foo:
				[metadata key]
				bar string (1..1)
			
			type Baz:
				foo Foo (1..1)
					[metadata reference]
				
				condition:
					foo -> reference = "reference"
		'''.generateCode

		val classes = code.compileToClasses

		val foo = classes.get(rootPackage + '.Foo')
		assertThat(foo.declaredMethods.map[name], hasItem('getBar'))
		assertThat(foo.declaredMethods.map[name], hasItem('getMeta'))
		
		val baz = classes.get(rootPackage + '.Baz')
		assertThat(baz.declaredMethods.map[name], hasItem('getFoo'))
		val fooMethod = baz.getMethod("getFoo")
		val returnType = fooMethod.returnType
		assertThat(returnType.simpleName, is('ReferenceWithMetaFoo'))
	}
	
	// TODO the path containing a reference should work - the path is the same apart from it starts at the attribute rather than the type level).
	// TODO fails when the metaType is moved to annotations.rosetta or basictypes.rosetta (and removed from the code here).
	@Test
	@Disabled 
	def void shouldGenerateGlobalReferenceField2() {
		val code = '''
			metaType reference string
			
			type Foo:
				[metadata key]
				bar string (1..1)
			
			type Baz:
				foo Foo (1..1)
					[metadata reference]
				
				condition:
					foo -> reference exists
		'''.generateCode

		val classes = code.compileToClasses

		val foo = classes.get(rootPackage + '.Foo')
		assertThat(foo.declaredFields.map[name], hasItem('bar'))
		assertThat(foo.declaredFields.map[name], hasItem('meta'))
		
		val baz = classes.get(rootPackage + '.Baz')
		assertThat(baz.declaredFields.map[name], hasItem('foo'))
		val fooMethod = baz.getMethod("getFoo")
		val returnType = fooMethod.returnType
		assertThat(returnType.simpleName, is('ReferenceWithMetaFoo'))
	}
	
}
