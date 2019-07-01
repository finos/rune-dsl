package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaObjectInheritanceGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper 

	@Test
	def void shouldGenerateJavaClassWithMultipleParents() {
		val genereated = '''
			class A
			{
				aa string (0..1);
			}
			
			class B extends A
			{
				bb string (0..1);
			}
			
			class C extends B
			{
				cc string (0..1);
			}
			
			class D extends C
			{
				dd string (0..1);
			}
		'''.generateCode

		val classes = genereated.compileToClasses

		val classA = classes.get(javaPackages.model.packageName + ".A")
		val classB = classes.get(javaPackages.model.packageName + ".B")
		val classC = classes.get(javaPackages.model.packageName + ".C")
		val classD = classes.get(javaPackages.model.packageName + ".D")

		assertEquals(classD.superclass, classC)
		assertEquals(classC.superclass, classB)
		assertEquals(classB.superclass, classA)
	}

}
