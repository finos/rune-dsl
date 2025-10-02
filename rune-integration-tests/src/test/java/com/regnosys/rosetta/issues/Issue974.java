package com.regnosys.rosetta.issues;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

/*
 * Regression test for https://github.com/finos/rune-dsl/issues/974
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class Issue974 {

    @Inject
    private ModelHelper modelHelper;
    @Inject
    private RosettaValidationTestHelper validationTestHelper;

    @Test
    void singleSwitchBeforeOtherFunctionShouldParse() {
        var model = modelHelper.parseRosetta("""
            func Foo:
                output:
                    result string (1..1)
            
                set result:
                    42 extract scheme
            """);
        
        validationTestHelper.assertIssues(model, """
                ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Linking) 'Couldn't resolve reference to RosettaSymbol 'scheme'.' at 9:20, length 6, on RosettaSymbolReference
                """);
    }
}
