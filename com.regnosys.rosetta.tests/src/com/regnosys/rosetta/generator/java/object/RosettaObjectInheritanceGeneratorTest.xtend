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

		val classA = classes.get(rootPackage.name + ".A")
		val classB = classes.get(rootPackage.name + ".B")
		val classC = classes.get(rootPackage.name + ".C")
		val classD = classes.get(rootPackage.name + ".D")

		assertEquals(classD.superclass, classC)
		assertEquals(classC.superclass, classB)
		assertEquals(classB.superclass, classA)
	}
	
	@Test
	def void shouldGenerateJavaClassWithOverridenAttributes() {
		val genereated = '''
			class A
			{
				aa string (0..1);
			}
			
			class B extends A
			{
				bb string (0..1);
			}
			
			class Top1
			{
				field A (0..1);
			}
			
			class Top2 extends Top1
			{
				field B (0..1);
			}
		'''.generateCode

		val classes = genereated.compileToClasses

		val classA = classes.get(rootPackage.name + ".A")
		val classB = classes.get(rootPackage.name + ".B")
		val classTop1 = classes.get(rootPackage.name + ".Top1")
		val classTop2 = classes.get(rootPackage.name + ".Top2")

		assertEquals(classTop2.superclass, classTop1)
		assertEquals(classB.superclass, classA)
	}

	
	@Test
	def void shouldGenerateJavaClassWithOverridenAttributesAcrossNamespaces() {
		val generated = newArrayList(
			
			'''
			namespace "extending"
			
			import original.*
			
			type A extends original.A:
				bb string (0..1)
			
			type Top extends original.Top:
				override aField A (0..1)
		''',
		'''
			namespace "original"
			type A:
				aa string (0..1)
			
			type Top:
				aField A (0..1)
			'''
			).reverse.generateCode
			//This test only works if the order of the "files" is the opposite from that provided (hence the reverse)
		//.writeClasses("shouldGenerateJavaClassWithOverridenAttributesAcrossNamespaces")


		val classes = generated.compileToClasses
		val extendingTop = classes.get("extending.Top")
		assertThrows(NoSuchFieldException, [|extendingTop.getDeclaredField("aField")]);
	}

	@Test
	def void shouldGenerateJavaClassWithOverridenListAttributesAcrossNamespaces() {
		val generated = newArrayList(
			
			'''
			namespace "extending"
			
			import original.*
			
			type A extends original.A:
				bb string (0..1)
			
			type Top extends original.Top:
				override aField A (0..*)
		''',
		'''
			namespace "original"
			type A:
				aa string (0..1)
			
			type Top:
				aField A (0..*)
			'''
			).reverse.generateCode
			//This test only works if the order of the "files" is the opposite from that provided (hence the reverse)
		//.writeClasses("shouldGenerateJavaClassWithOverridenListAttributesAcrossNamespaces")


		val classes = generated.compileToClasses
		val extendingTop = classes.get("extending.Top")
		assertThrows(NoSuchFieldException, [|extendingTop.getDeclaredField("aField")]);
	}
	
}
