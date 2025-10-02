package com.regnosys.rosetta.generator.java.object

import org.junit.jupiter.api.Test
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.RosettaModelObject
import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.InjectWith
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class RosettaProcessorTest {
	@Inject extension CodeGeneratorTestHelper
	
	private def void assertProcessEquals(String expected, RosettaModelObject rmo) {
		val fooProcessor = new RosettaAttributePathProcessor
		rmo.process(RosettaPath.valueOf("ROOT"), fooProcessor)
		assertEquals(expected, fooProcessor.result.join(System.lineSeparator) + System.lineSeparator)
	}
	
	@Test
	def void processFlatType() {
		val code = '''
		type Foo:
			attr1 int (0..1)
			attr2 string (0..2)
			attr3 int (1..1)
		'''.generateCode
		val classes = code.compileToClasses
		
		val foo1 = classes.createInstanceUsingBuilder('Foo', #{
			'attr1' -> 42,
			'attr2' -> #["A", "B"],
			'attr3' -> 0
		})
		assertProcessEquals(
			'''
			ROOT.attr1
			ROOT.attr2
			ROOT.attr3
			''',
			foo1
		)
		
		val foo2 = classes.createInstanceUsingBuilder('Foo', #{
			'attr1' -> null,
			'attr2' -> #[],
			'attr3' -> 0
		})
		assertProcessEquals(
			'''
			ROOT.attr1
			ROOT.attr2
			ROOT.attr3
			''',
			foo2
		)
	}
	
	@Test
	def void processNestedType() {
		val code = '''
		type Foo:
			attr1 int (0..1)
			attr2 Bar (0..2)
			attr3 Bar (1..1)
		
		type Bar:
			bar Bar (0..1)
		'''.generateCode
		val classes = code.compileToClasses
		
		val foo = classes.createInstanceUsingBuilder('Foo', #{
			'attr1' -> 42,
			'attr2' -> #[
					classes.createInstanceUsingBuilder('Bar', #{'bar' -> null}),
					classes.createInstanceUsingBuilder('Bar', #{'bar' -> classes.createInstanceUsingBuilder('Bar', #{'bar' -> null})})
				],
			'attr3' -> classes.createInstanceUsingBuilder('Bar', #{'bar' -> null})
		})
		assertProcessEquals(
			'''
			ROOT.attr1
			ROOT.attr2
			ROOT.attr2(0).bar
			ROOT.attr2(1).bar
			ROOT.attr2(1).bar.bar
			ROOT.attr3
			ROOT.attr3.bar
			''',
			foo
		)
	}
	
	@Test
	def void processTypeWithSupertype() {
		val code = '''
		type Foo:
			attr1 int (0..1)
		
		type Bar extends Foo:
			attr2 string (0..2)
			attr3 int (1..1)
		'''.generateCode
		val classes = code.compileToClasses
		
		val bar = classes.createInstanceUsingBuilder('Bar', #{
			'attr1' -> 42,
			'attr2' -> #["A", "B"],
			'attr3' -> 0
		})
		assertProcessEquals(
			'''
			ROOT.attr1
			ROOT.attr2
			ROOT.attr3
			''',
			bar
		)
	}
	
	@Test
	def void processTypeWithOverridenAttributes() {
		val code = '''
		type A:
			a int (1..1)
		
		type Foo:
			attr1 int (0..1)
			attr2 A (0..2)
			attr3 string (1..1)
		
		type Bar extends Foo:
			attr1 int (0..1)
			attr2 A (0..2)
			attr4 int (1..1)
		'''.generateCode
		val classes = code.compileToClasses
		
		val bar = classes.createInstanceUsingBuilder('Bar', #{
			'attr1' -> 42,
			'attr2' -> #[
				classes.createInstanceUsingBuilder('A', #{
					'a' -> 42
				})
			],
			'attr3' -> "Bla",
			'attr4' -> 0
		})
		assertProcessEquals(
			'''
			ROOT.attr1
			ROOT.attr2
			ROOT.attr2(0).a
			ROOT.attr3
			ROOT.attr4
			''',
			bar
		)
	}
}