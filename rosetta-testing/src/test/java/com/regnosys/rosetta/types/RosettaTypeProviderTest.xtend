package com.regnosys.rosetta.types

import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaTypeProviderTest {

	@Inject extension RosettaTypeProvider
	@Inject extension ModelHelper modelHelper
	@Inject extension ValidationTestHelper
	
	@Test
	def void testAttributeSameNameAsAnnotationTest() {
		val model =
		'''		
		type A:
			[rootType]
			rootType string (0..1)
		
			condition C:
				rootType exists
		'''.parseRosetta
		
		model.assertNoIssues
	}

	@Test
	def void testBinaryExpressionCommonType() {
		val funcs = '''
			isEvent root Foo;
			
			type Foo:
				iBar int (0..*)
				nBar number (0..*)
				nBuz number (0..*)
			
			func Qualify_AllNumber:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				set is_event:
					[foo -> nBar, foo -> nBuz] contains 4.0
			
			func Qualify_MixedNumber:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				set is_event:
					[foo -> nBar, foo -> iBar] contains 4.0
			
			func Qualify_IntOnly:
				[qualification BusinessEvent]
				inputs: foo Foo (1..1)
				output: is_event boolean (1..1)
				set is_event:
					foo -> iBar = 4.0
		'''.parseRosettaWithNoErrors.elements.filter(Function)
		
		val allNumber = funcs.filter[name == "Qualify_AllNumber"].head
		assertEquals('number', (allNumber.operations.head.expression as RosettaContainsExpression).left.RType.RType.name)
		val mixed = funcs.filter[name == "Qualify_MixedNumber"].head
		assertEquals('number', (mixed.operations.head.expression as RosettaContainsExpression).left.RType.RType.name)
		val intOnly = funcs.filter[name == "Qualify_IntOnly"].head
		assertEquals('int', (intOnly.operations.head.expression as RosettaBinaryOperation).left.RType.RType.name)
	}
}
