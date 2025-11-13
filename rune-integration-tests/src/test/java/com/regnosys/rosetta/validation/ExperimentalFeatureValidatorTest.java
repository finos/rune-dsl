package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ExperimentalFeatureValidatorTest extends AbstractValidatorTest {
    @Test
    void testScopesAreExperimental() {
        assertIssues("""
				namespace test
				scope MyScope
				""", """
                ERROR (null) 'Scopes are an experimental feature, and are not enabled for this project' at 2:1, length 13, on RosettaScope
                """);
    }
}
