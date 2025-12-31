package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ConditionValidatorTest extends AbstractValidatorTest {
    @Test
    void shouldGenerateNoConditionNameWarning() {
        assertIssues("""
				type Foo:
					x string (0..1)

					condition:
						x exists
				""", """
                WARNING (RosettaIssueCodes.invalidName) 'Condition name should be specified' at 7:2, length 21, on Condition
                """);
    }

    @Test
    void shouldGenerateConditionNameInvalidCaseWarning() {
        assertIssues("""
				type Foo:
					x string (0..1)

					condition xExists:
						x exists
				""", """
                WARNING (RosettaIssueCodes.invalidCase) 'Condition name should start with a capital' at 7:12, length 7, on Condition
                """);
    }

	@Test
	void shouldGenerateConditionNameInvalidCaseWarningCanBeSuppressed() {
		assertNoIssues("""
				type Foo:
					x string (0..1)

					condition xExists:
					  [suppressWarnings capitalisation]
						x exists
				""");
	}
}
