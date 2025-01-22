package com.regnosys.rosetta.docrefs

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith
import javax.inject.Inject

@InjectWith(RosettaTestInjectorProvider)
@ExtendWith(InjectionExtension)

class DocReferenceTest {

	@Inject extension ModelHelper
	
	@Test
	def void declearCorpusWithBodyReference() {
		'''
			body Organisation Org1 <"some description 1">
			corpus Agreement Org1 "Agreement 1" Agr1 <"some description 2">
			
		'''.parseRosettaWithNoErrors
	}
	
	
	@Test
	def void declearCorpusWithoutBodyReference() {
		'''
			corpus Agreement "Agreement 1" Agr1 <"some description 2">
						
		'''.parseRosettaWithNoErrors
	}
	
	
	@Test
	def void corpusDisplaytNameIsOptional() {
		'''
			corpus Agreement Agr1
		'''.parseRosettaWithNoErrors
	}
	
	@Test
	def void typeCanHaveDocRef() {
		'''
			body Organisation Org1
			corpus Agreement Org1 "Agreement 1" Agr1
			
			segment name
			
			type Foo:
				[docReference Org1 Agr1 name "something" provision "some provision"]
				bar string (1..1)
			
		'''.parseRosettaWithNoErrors
	}
	
	@Test
	def void docRefProvisionIsOptional() {
		'''
			body Organisation Org1
			corpus Agreement Org1 "Agreement 1" Agr1
			
			segment name
			
			type Foo:
				[docReference Org1 Agr1 name "something"]
				bar string (1..1)
			
		'''.parseRosettaWithNoErrors
	}
	
	@Test
	def void attributeCanHaveDocRef() {
		'''
			body Organisation Org1
			corpus Agreement Org1 "Agreement 1" Agr1
			
			segment name
			
			type Foo:
				bar string (1..1)
					[docReference Org1 Agr1 name "something"]
			
		'''.parseRosettaWithNoErrors
	}
	
	@Test
	def void enumCanHaveDocRef() {
		'''
			body Organisation Org1
			corpus Agreement Org1 "Agreement 1" Agr1
			
			segment name
			
			enum Foo:
			[docReference Org1 Agr1 name "something"]
				bar
					
			
		'''.parseRosettaWithNoErrors
	}
	
	@Test
	def void enumValueCanHaveDocRef() {
		'''
			body Organisation Org1
			corpus Agreement Org1 "Agreement 1" Agr1
			
			segment name
			
			enum Foo:
				bar
					[docReference Org1 Agr1 name "something"]
		'''.parseRosettaWithNoErrors
	}
	
	@Test
	def void functionsCanHaveDocRef() {
		'''
			body Organisation Org1
			corpus Agreement Org1 "Agreement 1" Agr1
			
			segment name
			
			func Sum:
				[docReference Org1 Agr1 name "something"]
				inputs: x number (0..*)
				output: s number (1..1)

		'''.parseRosettaWithNoErrors
	}
	
	@Test
	def void conditionsCanHaveDocRef() {
		'''
			body Organisation Org1
			corpus Agreement Org1 "Agreement 1" Agr1
			
			segment name
			
			type Foo:
				a int (1..1)
				
				condition:
					[docReference Org1 Agr1 name "something"]
					a > 0
			
			
		'''.parseRosettaWithNoErrors
	}
}

