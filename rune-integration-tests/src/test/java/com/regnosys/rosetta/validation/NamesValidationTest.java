package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.TYPE_PARAMETER;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class NamesValidationTest {
    @Inject
    private RosettaValidationTestHelper validationTestHelper;
    @Inject
    private ModelHelper modelHelper;
    
    @Test
    void testDuplicateType() {
        var model = modelHelper.parseRosetta("""
				type Bar:

				type Foo:

				enum Foo: BAR
				""");
        validationTestHelper.assertIssues(model, """
            ERROR (null) 'Duplicate type 'Foo' in namespace 'com.rosetta.test.model'' at 6:6, length 3, on Data
            ERROR (null) 'Duplicate type 'Foo' in namespace 'com.rosetta.test.model'' at 8:6, length 3, on RosettaEnumeration
            """);
    }

    @Test
    void testDuplicateTypeCaseInsensitive() {
        var model = modelHelper.parseRosetta("""
				type FooBar:

				enum Foobar: BAR
				""");
        validationTestHelper.assertIssues(model, """
            ERROR (null) 'Duplicate type 'FooBar' in namespace 'com.rosetta.test.model'' at 4:6, length 6, on Data
            ERROR (null) 'Duplicate type 'Foobar' in namespace 'com.rosetta.test.model'' at 6:6, length 6, on RosettaEnumeration
            """);
    }

    @Test
    void testDuplicateTypeInDifferentNamespaces() {
        var models = modelHelper.parseRosetta("""
                type Foo:
                """, """
                type Foo:
                """);
        
        validationTestHelper.assertIssues(models.getFirst(), """
            ERROR (null) 'Duplicate type 'Foo' in namespace 'com.rosetta.test.model'' at 4:6, length 3, on Data
            """);
    }
    
    @Test
    void testNamespaceOverrideDoesNotCauseDuplicateError() {
        var models = modelHelper.parseRosetta("""
                namespace test
                
                type Foo:
                    attr int (1..1)
                """, """
                override namespace test
                
                type Foo:
                    attr string (1..1)
                """);
        
        models.forEach(validationTestHelper::assertNoIssues);
    }

    @Test
    void testParametrizedBasicTypesWithDuplicateParameters() {
        var model = modelHelper.parseRosetta("""
				basicType int(digits int, digits int)
				""");
        validationTestHelper.assertError(model, TYPE_PARAMETER, null,
                "Duplicate parameter name `digits`.");
    }
}
