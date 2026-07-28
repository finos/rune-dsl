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
	public void testMetadataAnnotationsAreNotAllowedOnChoiceTypes() {
		assertIssues("""
				choice SomeChoice:
					[metadata key]
					OptionA
					OptionB

				type OptionA:

				type OptionB:
				""",
				"""
				ERROR (null) '[metadata key] annotations are not allowed on a choice type.' at 5:2, length 14, on AnnotationRef
				"""
		);
	}

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

	@Test
	void testDeepFeatureKeyFailsWhenNotAllLeavesAreKeyed() {
		assertIssues("""
				metaType key string

				type B:
					[metadata key]
					field string (1..1)

				type C:
					field string (1..1)

				choice A:
					B
					C

				func MyFunc:
					inputs:
						a A (1..1)
					output:
						result string (0..1)
					set result:
						a ->> key
				""",
				"""
						ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Linking) 'Couldn't resolve reference to RosettaFeature 'key'.' at 23:9, length 3, on RosettaDeepFeatureCall
						""");
	}

	@Test
	void testDeepFeatureKeyFailsWhenANestedLeafIsUnkeyed() {
		assertIssues("""
				metaType key string

				type B:
					[metadata key]
					field string (1..1)

				type C:
					field string (1..1)

				type D:
					[metadata key]
					field string (1..1)

				choice Inner:
					B
					C

				choice A:
					Inner
					D

				func MyFunc:
					inputs:
						a A (1..1)
					output:
						result string (0..1)
					set result:
						a ->> key
				""",
				"""
						ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Linking) 'Couldn't resolve reference to RosettaFeature 'key'.' at 31:9, length 3, on RosettaDeepFeatureCall
						""");
	}

	@Test
	void testDeepFeatureKeyFailsWhenChoiceIsEmpty() {
		assertIssues("""
				metaType key string

				choice A:

				func MyFunc:
					inputs:
						a A (1..1)
					output:
						result string (0..1)
					set result:
						a ->> key
				""",
				"""
						ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Linking) 'Couldn't resolve reference to RosettaFeature 'key'.' at 14:9, length 3, on RosettaDeepFeatureCall
						""");
	}

	@Test
	void testDeepFeatureCallOnCyclicChoiceProducesLinkingErrorNotStackOverflow() {
		assertIssues("""
				choice CyclicA:
					CyclicB

				choice CyclicB:
					CyclicA

				func MyFunc:
					inputs:
						a CyclicA (1..1)
					output:
						result string (0..1)
					set result:
						a ->> someField
				""", """
				ERROR (null) 'Cyclic option: CyclicA includes CyclicB includes CyclicA' at 5:2, length 7, on ChoiceOption
				ERROR (null) 'Duplicate option 'CyclicB'' at 5:2, length 7, on ChoiceOption
				ERROR (null) 'Cyclic option: CyclicB includes CyclicA includes CyclicB' at 8:2, length 7, on ChoiceOption
				ERROR (null) 'Duplicate option 'CyclicA'' at 8:2, length 7, on ChoiceOption
				ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Linking) 'Couldn't resolve reference to RosettaFeature 'someField'.' at 16:9, length 9, on RosettaDeepFeatureCall
				""");
	}

	@Test
	public void testChoiceAliasOfNestedChoiceOptionsDoNotOverlap() {
		assertIssues("""
				choice Outer:
					InnerAlias
					TargetOpt

				typeAlias InnerAlias: Inner

				choice Inner:
					TargetOpt
					Sibling

				type TargetOpt:

				type Sibling:
				""",
				"""
				ERROR (null) 'Option 'TargetOpt' is already included by option 'InnerAlias'' at 6:2, length 9, on ChoiceOption
				"""
		);
	}

	@Test
	public void supportDeprecatedAnnotationOnChoice() {
		assertIssues("""
			choice FooDeprecated:
			 [deprecated]
				string
				int

			func Foo:
				output:
					result FooDeprecated (1..1)

				set result:
					FooDeprecated { string: "My string", ... }
			""", """
				INFO (null) 'FooDeprecated is deprecated' at 11:10, length 13, on TypeCall
				INFO (null) 'FooDeprecated is deprecated' at 14:3, length 13, on TypeCall
				""");
	}
}
