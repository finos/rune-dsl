package com.regnosys.rosetta.generator.java.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;

/**
 * This test is meant to prevent accidental changes to the generated code
 * of Rosetta Model Objects. If any unwanted change accidentally gets
 * through, please add to this test so it does not happen again.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
@TestInstance(Lifecycle.PER_CLASS)
public class PojoRegressionTest {
    @Inject
    private CodeGeneratorTestHelper codeGeneratorTestHelper;
    
    Map<String, String> code;
    
    @BeforeAll
    void setup() {
        var model = """
            type Pojo:
                [metadata key]
                simpleAttr string(maxLength: 42) (1..1)
                multiSimpleAttr string(maxLength: 42) (0..*)
        
                simpleAttrWithMeta string (1..1)
                    [metadata scheme]
                multiSimpleAttrWithMeta string (0..*)
                    [metadata scheme]
        
                simpleAttrWithId string (1..1)
                    [metadata id]
                multiSimpleAttrWithId string (0..*)
                    [metadata id]
        
                complexAttr Foo (1..1)
                multiComplexAttr Foo (0..*)
        
                complexAttrWithRef Foo (1..1)
                    [metadata reference]
                multiComplexAttrWithRef Foo (0..*)
                    [metadata reference]
        
            type Foo:
                [metadata key]
        
        
            type Qux:
                qux string (1..1)
                    [metadata location]
        
            type Bar:
                bar Qux (1..1)
        
            type Baz:
                baz string (1..1)
                    [metadata address "pointsTo"=Bar->bar->qux]
        """;
        
        code = codeGeneratorTestHelper.generateCode(model);
    }
    
    private void assertGeneratedCode(String className, String expectationFileName) {
        try {
			String expected = Resources.toString(getClass().getResource("/pojo-regressions/" + expectationFileName), Charsets.UTF_8);
        	assertEquals(expected, code.get(className));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
    @Test
    void testCodeCompiles() {
        codeGeneratorTestHelper.compileToClasses(code);
    }
    
    @Test
    void testScopedAttributeKeyPojo() {
        assertGeneratedCode("com.rosetta.test.model.Qux", "Qux.java");
    }

    @Test
    void testScopedAttributeReferencePojo() {
        assertGeneratedCode("com.rosetta.test.model.Baz", "Baz.java");
    }

    @Test
    void testPojoCode() {
        assertGeneratedCode("com.rosetta.test.model.Pojo", "Pojo.java");
    }

    @Test
    void testPojoValidator() {
        assertGeneratedCode("com.rosetta.test.model.validation.PojoValidator", "PojoValidator.java");
    }

    @Test
    void testPojoTypeFormatValidator() {
        assertGeneratedCode("com.rosetta.test.model.validation.PojoTypeFormatValidator", "PojoTypeFormatValidator.java");
    }

    @Test
    void testPojoOnlyExistsValidator() {
        assertGeneratedCode("com.rosetta.test.model.validation.exists.PojoOnlyExistsValidator", "PojoOnlyExistsValidator.java");
    }

    @Test
    void testFieldWithMetaStringCode() {
        assertGeneratedCode("com.rosetta.model.metafields.FieldWithMetaString", "FieldWithMetaString.java");
    }

    @Test
    void testReferenceWithMetaFooCode() {
        assertGeneratedCode("com.rosetta.test.model.metafields.ReferenceWithMetaFoo", "ReferenceWithMetaFoo.java");
    }
}
