package com.regnosys.rosetta.generator.java.docrefs

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import com.regnosys.rosetta.validation.RosettaIssueCodes
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static org.hamcrest.MatcherAssert.*
import static org.junit.jupiter.api.Assertions.*
import static org.mockito.Mockito.mock
import com.google.inject.Injector
import com.google.inject.Guice
import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory
import java.util.Map

@InjectWith(RosettaInjectorProvider)
@ExtendWith(InjectionExtension)

class DocReferenceTest {

	@Inject extension CodeGeneratorTestHelper
	@Inject extension ModelHelper
	@Inject extension ValidationTestHelper

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
				output: sum number (1..1)
				
					
		'''.parseRosettaWithNoErrors
	}
}

