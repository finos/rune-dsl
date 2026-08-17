package com.regnosys.rosetta.validation;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

/**
 * A type alias accepts annotations only to carry {@code [suppressUnused]}. The grammar fragment allows any
 * annotation and nothing reads the others, so everything else has to be rejected here — including the
 * annotations the {@code Annotated} supertype makes type aliases reachable from
 * ({@code [deprecated]}, {@code [metadata …]}, creation annotations).
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
                ERROR (null) '[rootType] is not allowed on a type alias. The only annotation a type alias supports is [suppressUnused].' at 5:6, length 8, on AnnotationRef
                """);
    }

    @Test
    void testDeprecatedIsRejected() {
        assertIssues("""
                typeAlias Max4String: string(minLength: 1, maxLength: 4)
                    [deprecated]
                """, """
                ERROR (null) '[deprecated] is not allowed on a type alias. The only annotation a type alias supports is [suppressUnused].' at 5:6, length 10, on AnnotationRef
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
}
