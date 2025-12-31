package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class TypeValidatorTest extends AbstractValidatorTest {
    @Test
    void testTypeNameCapitalisationWarningCanBeSuppressed() {
        assertNoIssues("""
                type partyIdentifier:
                  [suppressWarnings capitalisation]
                    partyId string (1..1)
                """);
    }

    @Test
    void testTypeNameShouldBeCapitalized() {
        assertIssues("""
                        type partyIdentifier:
                            partyId string (1..1)
                        """,
                """
                        WARNING (RosettaIssueCodes.invalidCase) 'Type name should start with a capital' at 4:6, length 15, on Data
                        """);
    }

    @Test
    void testTypeNameMayStartWithUnderscore() {
        assertNoIssues("""
                type _PartyIdentifier:
                    partyId string (1..1)
                """);
    }

    @Test
    void testAttributeOverridesMustComeFirst() {
        assertIssues("""
                        type Foo:
                            attr number (0..1)
                        
                        type Bar extends Foo:
                            barAttr int (1..1)
                            override attr number (1..1)
                        """,
                """
                        ERROR (null) 'Attribute overrides should come before any new attributes' at 9:5, length 27, on Attribute
                        """);
    }

    @Test
    void testCannotOverrideAttributeTwice() {
        assertIssues("""
                        type Foo:
                            attr number (0..1)
                        
                        type Bar extends Foo:
                            override attr number (1..1)
                            override attr int (1..1)
                        """,
                """
                        ERROR (null) 'Duplicate attribute 'attr' in type 'Bar'' at 8:14, length 4, on Attribute
                        ERROR (null) 'Duplicate attribute 'attr' in type 'Bar'' at 9:14, length 4, on Attribute
                        """);
    }

    @Test
    void testCannotHaveDuplicateAttributeToSuperTypeWithoutOverriding() {
        // Note: once support is dropped, this should become a duplicate attribute error.
        assertIssues("""
                        type Foo:
                            attr string (1..1)
                        
                        type Bar extends Foo:
                            attr string (1..1)
                        """,
                """
                        WARNING (null) 'Attribute 'attr' already defined in super type. To override the type, cardinality or annotations of this attribute, use the keyword `override`' at 8:5, length 4, on Attribute
                        """);
    }

    @Test
    void testCannotNotHaveDuplicateAttributeOnSameType() {
        assertIssues("""
                        type Foo:
                            attr number (0..1)
                            attr number (0..1)
                        """,
                """
                        ERROR (null) 'Duplicate attribute 'attr' in type 'Foo'' at 5:5, length 4, on Attribute
                        ERROR (null) 'Duplicate attribute 'attr' in type 'Foo'' at 6:5, length 4, on Attribute
                        """);
    }

    @Test
    void testDuplicateAttributeErrorWhenAttributeAlreadyOverridden() {
        assertIssues("""
                        type Foo:
                            attr number (0..1)

                        type Bar extends Foo:
                            override attr number (0..1)
                            attr string (1..1)
                        """,
                """
                        ERROR (null) 'Duplicate attribute 'attr' in type 'Bar'' at 8:14, length 4, on Attribute
                        WARNING (null) 'Attribute 'attr' already defined in super type. To override the type, cardinality or annotations of this attribute, use the keyword `override`' at 9:5, length 4, on Attribute
                        ERROR (null) 'Duplicate attribute 'attr' in type 'Bar'' at 9:5, length 4, on Attribute
                        """);
    }

    @Test
    void testSuperTypeMayNotBeAChoiceType() {
        assertIssues("""
                        choice StringOrNumber:
                            string
                            number
                        
                        type Foo extends StringOrNumber:
                        """,
                """
                        WARNING (null) 'Extending a choice type is deprecated' at 8:18, length 14, on Data
                        """);
    }

    @Test
    void testSuperTypesCannotHaveACycle() {
        String model = """
                type A extends C:
                
                type B extends A:
                
                type C extends B:
                """;
        assertIssues(model, """
                ERROR (null) 'Cyclic extension: A extends C extends B extends A' at 4:16, length 1, on Data
                ERROR (null) 'Cyclic extension: B extends A extends C extends B' at 6:16, length 1, on Data
                ERROR (null) 'Cyclic extension: C extends B extends A extends C' at 8:16, length 1, on Data
                """);
    }

    @Test
    void supportDeprecatedAnnotationOnType() {
        String model = """
                type TestTypeDeprecated:
                    [deprecated]
                    attr int (1..1)
                
                func Foo:
                    output:
                        result TestTypeDeprecated (1..1)
                
                    set result:
                        TestTypeDeprecated { attr: 42 }
                """;
        assertIssues(model, """
                INFO (null) 'TestTypeDeprecated is deprecated' at 10:16, length 18, on TypeCall
                INFO (null) 'TestTypeDeprecated is deprecated' at 13:9, length 18, on TypeCall
                """);
    }
}
