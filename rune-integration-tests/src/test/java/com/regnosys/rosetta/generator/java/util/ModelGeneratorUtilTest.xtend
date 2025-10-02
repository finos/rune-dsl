package com.regnosys.rosetta.generator.java.util

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.tests.util.ModelHelper

import static org.junit.jupiter.api.Assertions.*
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class ModelGeneratorUtilTest {

	@Inject extension ModelHelper modelHelper
	@Inject ModelGeneratorUtil generatorUtil

	@Test
	def void testDocReferenceJavaDoc() {

		val model =
		'''
			body Organisation Org1
			corpus Agreement Org1 "Agreement 1" Agr1
			
			segment name
			
			type Foo:
				[docReference Org1 Agr1 name "something" provision "some provision"]
				bar string (1..1)
		'''

		val fooType = modelHelper
								.parseRosetta(model).elements
								.filter(Data)
								.filter[name == 'Foo']
								.get(0)

		val javaDoc = generatorUtil.javadoc(fooType)

		val expected = '''
			/**
			 *
			 * Body Org1
			 * Corpus Agreement Agr1 Agreement 1  
			 * name "something"
			 *
			 * Provision some provision
			 *
			 */
			'''

		assertEquals(expected, javaDoc.toString())
	}

	@Test
	def void testMultiDocReferenceJavaDoc() {

		val model =
		'''
			body Organisation Org1
			corpus Agreement Org1 "Agreement 1" Agr1
			
			body Organisation Org2
			corpus View Org2 "View 2" Vw2
			
			segment name
			
			type Foo:
				[docReference Org1 Agr1 name "something" provision "some provision"]
				[docReference Org2 Vw2 name "something else" provision "some other provision"]
				
				bar string (1..1)
		'''

		val fooType = modelHelper
								.parseRosetta(model).elements
								.filter(Data)
								.filter[name == 'Foo']
								.get(0)

		val javaDoc = generatorUtil.javadoc(fooType)

		val expected = '''
			/**
			 *
			 * Body Org1
			 * Corpus Agreement Agr1 Agreement 1  
			 * name "something"
			 *
			 * Provision some provision
			 *
			 *
			 * Body Org2
			 * Corpus View Vw2 View 2  
			 * name "something else"
			 *
			 * Provision some other provision
			 *
			 */
			'''

		assertEquals(expected, javaDoc.toString())
	}

	@Test
	def void testDocReferenceAndDefJavaDoc() {

		val model =
		'''
			body Organisation Org1
			corpus Agreement Org1 "Agreement 1" Agr1
						
			segment name
			
			type Foo: <"Foo def 12345">
				[docReference Org1 Agr1 name "something" provision "some provision"]
				
				bar string (1..1)
		'''

		val fooType = modelHelper
								.parseRosetta(model).elements
								.filter(Data)
								.filter[name == 'Foo']
								.get(0)

		val javaDoc = generatorUtil.javadoc(fooType)

		val expected = '''
			/**
			 * Foo def 12345
			 *
			 * Body Org1
			 * Corpus Agreement Agr1 Agreement 1  
			 * name "something"
			 *
			 * Provision some provision
			 *
			 */
			'''

		assertEquals(expected, javaDoc.toString())
	}

	@Test
	def void testDefJavaDoc() {

		val model =
		'''
			type Foo: <"Foo def 12345">
				bar string (1..1)
		'''

		val fooType = modelHelper
								.parseRosetta(model).elements
								.filter(Data)
								.filter[name == 'Foo']
								.get(0)

		val javaDoc = generatorUtil.javadoc(fooType)
		
		val expected = '''
			/**
			 * Foo def 12345
			 */
			'''

		assertEquals(expected, javaDoc.toString())
	}
	
	@Test
	def void testDocRefOnAttributeJavaDoc() {

		val model =
		'''
			body Organisation Org1
			corpus Agreement Org1 "Agreement 1" Agr1
			
			body Organisation Org2
			corpus View Org2 "View 2" Vw2
						
			segment name
			
			type Foo:
				bar string (1..1) <"Foo def 12345">
				[docReference Org1 Agr1 name "something" provision "some provision"]
				[docReference Org2 Vw2 name "something else" provision "some other provision"]
					
		'''

		val fooBarAttr = modelHelper
								.parseRosetta(model).elements
								.filter(Data)
								.filter[name == 'Foo']
								.flatMap[attributes]
								.filter[name == 'bar']
								.get(0)

		val javaDoc = generatorUtil.javadoc(fooBarAttr)
		
		val expected = '''
			/**
			 * Foo def 12345
			 *
			 * Body Org1
			 * Corpus Agreement Agr1 Agreement 1  
			 * name "something"
			 *
			 * Provision some provision
			 *
			 *
			 * Body Org2
			 * Corpus View Vw2 View 2  
			 * name "something else"
			 *
			 * Provision some other provision
			 *
			 */
			'''

		assertEquals(expected, javaDoc.toString())
	}

}