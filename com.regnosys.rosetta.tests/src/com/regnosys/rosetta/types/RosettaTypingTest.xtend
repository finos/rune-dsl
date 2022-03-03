package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaTypingTest {	
	@Inject
	extension TypeFactory
	
	@Inject
	extension TypeTestUtil
	
	@Test
	def void testLiteralTypeInference() {
		assertEquals(singleBoolean, 'False'.type)
		assertEquals(singleString, '"Some string"'.type)
		assertEquals(singleNumber, '3.14'.type)
		assertEquals(singleInt, '1'.type)
		assertEquals(emptyNothing, 'empty'.type)
	}
}
