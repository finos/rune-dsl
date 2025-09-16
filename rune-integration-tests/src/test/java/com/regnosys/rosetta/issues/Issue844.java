package com.regnosys.rosetta.issues;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

/*
 * Regression test for https://github.com/finos/rune-dsl/issues/844
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Issue844 {

    @Inject
    private CodeGeneratorTestHelper codeGeneratorTestHelper;

    private Map<String, String> code;

    @BeforeAll
    void setup() {
        code = codeGeneratorTestHelper.generateCode("""
            type Foo:
                foo Foo (0..1)
            """);
    }

    @Test
    void shouldNotGenerateFieldWithMeta() {
        assertFalse(code.containsKey("com.rosetta.test.model.metafields.FieldWithMetaFoo"));
    }

    @Test
    void shouldNotGenerateReferenceWithMeta() {
        assertFalse(code.containsKey("com.rosetta.test.model.metafields.ReferenceWithMetaFoo"));
    }
}