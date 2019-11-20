package com.regnosys.rosetta.ui.tests.validation

import com.google.inject.Inject
import com.regnosys.rosetta.ui.tests.RosettaUiInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*
import static com.regnosys.rosetta.validation.RosettaIssueCodes.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaUiInjectorProvider)
class RosettaUIValidationTest extends AbstractProjectAwareTest {
	@Inject extension ValidationTestHelper

	@Test
	def rootClassesMayNotShareSameClassTypeAttributes() {
		val model = '''
			namespace test
			
			class Quote {}
			
			root class OtherRoot
			{
				clazzAttr Quote (0..1);
			}
			
			root class Root
			{
				attr Quote (1..1); // <-- ERROR here:  Attribute with Type Quote is already used in test.OtherRoot.clazzAttr
			}
		'''.createRosettaTestFile

		model.assertError(ROSETTA_REGULAR_ATTRIBUTE, null, "Attribute with Type Quote is already used in test.OtherRoot.clazzAttr")
	}

	@Test
	def rootUniqueTypeName() {
		val cl1 = '''
			namespace test
			
			type Quote:
		'''.createRosettaTestFile
		val cl2 = '''
			namespace test
			
			type Quote:
		'''.createRosettaFile("otherfile.rosetta")

		cl1.assertError(DATA, DUPLICATE_ELEMENT_NAME, "Duplicate element named 'test.Quote' in otherfile.rosetta")
		cl2.assertError(DATA, DUPLICATE_ELEMENT_NAME, "Duplicate element named 'test.Quote' in test.rosetta")
	}

}
