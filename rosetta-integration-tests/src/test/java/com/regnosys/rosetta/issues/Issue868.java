package com.regnosys.rosetta.issues;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

/*
 * Regression test for https://github.com/finos/rune-dsl/issues/844
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Issue868 {

    @Inject
    private ModelHelper modelHelper;

    @Test
    void singleSwitchBeforeOtherFunctionShouldParse() {
        modelHelper.parseRosettaWithNoIssues("""
            choice AssetCriterium:
                AssetType

            type AssetType:

            func Foo:
                inputs:
                    criterium AssetCriterium (1..1)
                output:
                    result boolean (1..1)
                set result:
                    criterium switch
                        AssetType then empty

            func OtherFunc:
              [codeImplementation]
                inputs:
                    inp int (1..1)
                output:
                    outp int (1..1)
            """);
    }
}