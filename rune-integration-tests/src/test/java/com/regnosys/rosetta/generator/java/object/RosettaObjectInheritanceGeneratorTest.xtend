package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class RosettaObjectInheritanceGeneratorTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper 

	@Test
	def void shouldGenerateJavaClassWithMultipleParents() {
		val genereated = '''
			type A:
				aa string (0..1)
			
			type B extends A:
				bb string (0..1)
			
			type C extends B:
				cc string (0..1)
			
			type D extends C:
				dd string (0..1)

		'''.generateCode
		
		val classes = genereated.compileToClasses

		val classA = classes.get(rootPackage + ".A")
		val classB = classes.get(rootPackage + ".B")
		val classC = classes.get(rootPackage + ".C")
		val classD = classes.get(rootPackage + ".D")

		assertTrue(classC.isAssignableFrom(classD))
		assertTrue(classB.isAssignableFrom(classC))
		assertTrue(classA.isAssignableFrom(classB))
	}

	@Disabled //override is deprecated
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

	@Disabled //override is deprecated
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
	
	@Disabled
	@Test
	def void shouldGenerateJavaClassWithConditionsListAttributesAcrossNamespaces() {
		val generated = newArrayList(
			'''
			namespace "extending"
			
			import original.*
			
			type A extends original.A:
				override b B (0..1)
			
			type B extends original.B:
				override c C (0..1)
			
			type C extends original.C:
				cField2 string (0..1)
				cField3 B (0..1)
			
				 condition cField3Exists: cField3 -> c exists
						
		''',
		'''
			namespace "original"
			type A:
				b B (0..1)
				
			type B:
				c C (0..1)
			
			type C:
				cField1 string (0..*)
			'''
			).reverse.generateCode
			//This test only works if the order of the "files" is the opposite from that provided (hence the reverse)
			//.writeClasses("shouldGenerateJavaClassWithConditionsListAttributesAcrossNamespaces")


		generated.compileToClasses

	}
	
}
