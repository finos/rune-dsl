package com.regnosys.rosetta.issues

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import javax.inject.Inject

import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import java.util.Map
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

// Regression test for https://github.com/finos/rune-dsl/issues/844
@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
@TestInstance(Lifecycle.PER_CLASS)
class Issue844 {
	
	@Inject extension CodeGeneratorTestHelper
	
	Map<String, String> code
	
	@BeforeAll
	def void setup() {
		code = '''
		type Foo:
			foo Foo (0..1)
		'''.generateCode
	}
	
	@Test
	def void shouldNotGenerateFieldWithMeta() {
		assertFalse(code.containsKey("com.rosetta.test.model.metafields.FieldWithMetaFoo"))
	}
	
	@Test
	def void shouldNotGenerateReferenceWithMeta() {
		assertFalse(code.containsKey("com.rosetta.test.model.metafields.ReferenceWithMetaFoo"))
	}
}