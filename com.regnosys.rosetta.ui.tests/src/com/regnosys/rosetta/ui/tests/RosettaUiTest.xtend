package com.regnosys.rosetta.ui.tests

import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaUiInjectorProvider)
class RosettaUiTest {

	@Test
	def void testUI() {
		assertTrue(true)
	}
}
