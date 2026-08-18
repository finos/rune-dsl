package com.regnosys.rosetta.validation;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

/**
 * A type alias accepts annotations only to carry {@code [suppressUnused]} and {@code [deprecated]}. The grammar
 * fragment allows any annotation and nothing reads the others, so everything else has to be rejected here —
 * including the annotations the {@code Annotated} supertype makes type aliases reachable from
 * ({@code [metadata …]}, creation annotations).
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class TypeAliasValidatorTest extends AbstractValidatorTest {

    @Test
    void testSuppressUnusedIsAllowed() {
        assertNoIssues("""
                typeAlias Max4String: string(minLength: 1, maxLength: 4)
                    [suppressUnused]
                """);
    }

    @Test
    void testOtherAnnotationsAreRejected() {
        assertIssues("""
                typeAlias Max4String: string(minLength: 1, maxLength: 4)
                    [rootType]
                """, """
                ERROR (null) '[rootType] is not allowed on a type alias. The only annotations a type alias supports are [suppressUnused] and [deprecated].' at 5:6, length 8, on AnnotationRef
                """);
    }

    @Test
    void testMetadataAnnotationIsRejected() {
        assertIssues("""
                typeAlias Max4String: string(minLength: 1, maxLength: 4)
                    [metadata scheme]
                """, """
                ERROR (null) '[metadata] is not allowed on a type alias. The only annotations a type alias supports are [suppressUnused] and [deprecated].' at 5:6, length 8, on AnnotationRef
                ERROR (null) '[metadata scheme] annotation only allowed on an attribute or a type.' at 5:15, length 6, on AnnotationRef
                """);
    }

    @Test
    void testDeprecatedIsAllowed() {
        assertNoIssues("""
                typeAlias Max4String: string(minLength: 1, maxLength: 4)
                    [deprecated]
                """);
    }

    @Test
    void testDeprecatedIsReportedAtReferenceSites() {
        assertIssues("""
                typeAlias Max4String: string(minLength: 1, maxLength: 4)
                    [deprecated]

                type Foo:
                    attr Max4String (1..1)
                """, """
                INFO (null) 'Max4String is deprecated' at 8:10, length 10, on TypeCall
                """);
    }

    @Test
    void testDuplicateSuppressUnusedIsRejected() {
        assertIssues("""
                typeAlias Max4String: string(minLength: 1, maxLength: 4)
                    [suppressUnused]
                    [suppressUnused]
                """, """
                ERROR (null) 'Only 1 [suppressUnused] annotation allowed.' at 6:6, length 14, on AnnotationRef
                """);
    }

    @Test
    void testDuplicateDeprecatedIsRejected() {
        assertIssues("""
                typeAlias Max4String: string(minLength: 1, maxLength: 4)
                    [deprecated]
                    [deprecated]
                """, """
                ERROR (null) 'Only 1 [deprecated] annotation allowed.' at 6:6, length 10, on AnnotationRef
                """);
    }

    @Test
    void testSuppressUnusedAndDeprecatedTogetherAreAllowed() {
        assertNoIssues("""
                typeAlias Max4String: string(minLength: 1, maxLength: 4)
                    [suppressUnused]
                    [deprecated]
                """);
    }
}
