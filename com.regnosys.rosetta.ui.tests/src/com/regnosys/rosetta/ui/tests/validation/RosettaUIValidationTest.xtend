package com.regnosys.rosetta.ui.tests.validation

import com.google.inject.Inject
import com.regnosys.rosetta.ui.tests.RosettaUiInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static com.regnosys.rosetta.validation.RosettaIssueCodes.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaUiInjectorProvider)
class RosettaUIValidationTest extends AbstractProjectAwareTest {
	@Inject extension ValidationTestHelper

	@Test
	def rootClassesMayNotShareSameClassTypeAttributes() {
		val model = '''
			class Quote
			{
			}
			
			root class OtherRoot
			{
				clazzAttr Quote (0..1);
			}
			
			root class Root
			{
				attr Quote (1..1); // <-- ERROR here:  Attribute with Type Quote is already used in OtherRoot.clazzAttr
			}
		'''.createRosettaTestFile

		model.assertError(ROSETTA_REGULAR_ATTRIBUTE, null, "Attribute with Type Quote is already used in OtherRoot.clazzAttr")
	}

	@Test
	def rootUniqueTypeName() {
		val cl1 = '''
			class Quote
			{
			}
		'''.createRosettaTestFile
		val cl2 = '''
			class Quote
			{
			}
		'''.createRosettaFile("otherfile.rosetta")

		cl1.assertError(ROSETTA_CLASS, DUPLICATE_ELEMENT_NAME, "Duplicate element named 'Quote' in otherfile.rosetta")
		cl2.assertError(ROSETTA_CLASS, DUPLICATE_ELEMENT_NAME, "Duplicate element named 'Quote' in test.rosetta")
	}

}
