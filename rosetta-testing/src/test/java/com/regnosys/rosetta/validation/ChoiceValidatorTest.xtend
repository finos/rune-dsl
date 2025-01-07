package com.regnosys.rosetta.validation

import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*
import javax.inject.Inject
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class ChoiceValidatorTest implements RosettaIssueCodes {

	@Inject extension ValidationTestHelper
	@Inject extension ModelHelper
	
	@Test
	def void testChoiceOptionsDoNotOverlap() {
 		'''
			choice Foo:
				Opt1
				Nested
			
			type Opt1:
			
			choice Nested:
				Opt1
				Opt2
			
			type Opt2:
 		'''
 			.parseRosetta
 			.assertError(CHOICE_OPTION, null, "Option 'Opt1' is already included by option 'Nested'")
	}
	
	@Test
	def void testNoCircularReferenceInChoiceOptions() {
 		'''
			choice Foo:
				Opt1
				Bar
			
			type Opt1:
			
			choice Bar:
				Foo
 		'''
 			.parseRosetta => [
 				it.assertError(CHOICE_OPTION, null, "Cyclic option: Foo includes Bar includes Foo")
 				it.assertError(CHOICE_OPTION, null, "Cyclic option: Bar includes Foo includes Bar")
 			]
	}
	
	@Test
	def void supportDeprecatedAnnotationOnChoice() {
        val model = '''
			choice FooDeprecated:
				[deprecated]
				string
				int
			
			func Foo:
				output:
					result FooDeprecated (1..1)
			
				set result:
					FooDeprecated { string: "My string", ... }
        '''.parseRosetta

        model.assertWarning(TYPE_CALL, null, "FooDeprecated is deprecated")
    }
}