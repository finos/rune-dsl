package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ChoiceValidatorTest extends AbstractValidatorTest {
	@Test
	public void testChoiceOptionsDoNotOverlap() {
		assertIssues("""
				choice Foo:
					Opt1
					Nested
				
				type Opt1:
				
				choice Nested:
					Opt1
					Opt2
				
				type Opt2:
				""",
				"""
				ERROR (null) 'Option 'Opt1' is already included by option 'Nested'' at 5:2, length 4, on ChoiceOption
				"""
		);
	}

	@Test
	void checkReferenceToFullyKeyedChoiceIsValid() {
		assertNoIssues("""
				type A:
					[metadata key]
				
				type B:
					[metadata key]
				
				choice Foo:
					A
					B
				
				type TypeToUse:
					attr Foo (0..1)
						[metadata reference]
				""");
	}

	@Test
	void checkReferenceToChoiceWithUnkeyedLeafIsInvalid() {
		assertIssues("""
				type A:
					[metadata key]
				
				type B:
				
				choice Foo:
					A
					B
				
				type TypeToUse:
					attr Foo (0..1)
						[metadata reference]
				""",
				"""
				ERROR (null) 'Choice 'Foo' cannot be a [metadata reference] target: every option type must be annotated with [metadata key]' at 14:7, length 3, on Attribute
				"""
			);
	}

	@Test
	public void testNoCircularReferenceInChoiceOptions() {
		assertIssues("""
				choice Foo:
					Opt1
					Bar
				
				type Opt1:
				
				choice Bar:
					Foo
				""",
			"""
			ERROR (null) 'Option 'Opt1' is already included by option 'Bar'' at 5:2, length 4, on ChoiceOption
			ERROR (null) 'Cyclic option: Foo includes Bar includes Foo' at 6:2, length 3, on ChoiceOption
			ERROR (null) 'Duplicate option 'Bar'' at 6:2, length 3, on ChoiceOption
			ERROR (null) 'Cyclic option: Bar includes Foo includes Bar' at 11:2, length 3, on ChoiceOption
			ERROR (null) 'Duplicate option 'Foo'' at 11:2, length 3, on ChoiceOption
			"""
			);
	}
}
