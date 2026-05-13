package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.validation.names.RosettaUniqueNamesConfig;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.TYPE_PARAMETER;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class NamesValidationTest {
    @Inject
    private RosettaValidationTestHelper validationTestHelper;
    @Inject
    private ModelHelper modelHelper;
    @Inject
    private RosettaUniqueNamesConfig uniqueNamesConfig;
    @Inject
    private IResourceDescription.Manager resourceDescriptionManager;
    
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
    void testDuplicateTypeInDifferentFiles() {
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
    void testNamespaceOverrideDoesNotCauseDuplicateErrorForTypes() {
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
    void testNamespaceOverrideDoesNotCauseDuplicateErrorForRules() {
        var models = modelHelper.parseRosetta("""
                namespace test
                
                type Foo:
                    attr int (1..1)
                
                eligibility rule FooRule from Foo:
                   extract True
                """, """
                override namespace test
                
                type Foo:
                    attr string (1..1)
                
                eligibility rule FooRule from Foo:
                   extract True
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

    @Test
    void testLocalValidationScopeCacheDoesNotPersistAcrossValidationContexts() {
        var clusterScope = uniqueNamesConfig.getDuplicationCluster(SimplePackage.eINSTANCE.getAttribute()).clusterScope();

        var modelV1 = modelHelper.parseRosetta("""
                type Foo:
                    a int (1..1)
                """);
        validationTestHelper.assertNoIssues(modelV1);
        OptionalInt cacheSizeAfterV1 = persistentLocalValidationScopeCacheSize(clusterScope);

        var modelV2 = modelHelper.parseRosetta("""
                type Bar:
                    b int (1..1)
                """);
        validationTestHelper.assertNoIssues(modelV2);
        OptionalInt cacheSizeAfterV2 = persistentLocalValidationScopeCacheSize(clusterScope);

        if (cacheSizeAfterV1.isPresent() && cacheSizeAfterV2.isPresent()) {
            Assertions.assertEquals(cacheSizeAfterV1.getAsInt(), cacheSizeAfterV2.getAsInt(),
                    "Persistent local validation scope cache grew across independent validation contexts.");
        }
        Assertions.assertFalse(cacheSizeAfterV2.isPresent(),
                "Local cluster scope must not keep a persistent localValidationScopes cache.");
    }

    @Test
    void testLocalScopeStillFindsDuplicateAttributes() {
        var model = modelHelper.parseRosetta("""
                type Foo:
                    attr int (1..1)
                    attr int (1..1)
                """);
        EClass clusterType = SimplePackage.eINSTANCE.getAttribute();
        Function<IEObjectDescription, org.eclipse.xtext.resource.ISelectable> scopeFunction =
                uniqueNamesConfig.getDuplicationCluster(clusterType).clusterScope().getScope(model.eResource(), clusterType);

        IResourceDescription resourceDescription = resourceDescriptionManager.getResourceDescription(model.eResource());
        List<IEObjectDescription> attributes = new ArrayList<>();
        resourceDescription.getExportedObjectsByType(clusterType).forEach(attributes::add);
        Assertions.assertFalse(attributes.isEmpty(), "Expected exported attribute descriptions.");

        IEObjectDescription attribute = attributes.getFirst();
        var sameName = scopeFunction.apply(attribute).getExportedObjects(clusterType, attribute.getName(), false);
        Assertions.assertEquals(2, count(sameName),
                "Local scope should still include both duplicate attributes.");
    }

    private static OptionalInt persistentLocalValidationScopeCacheSize(Object clusterScope) {
        Field cacheField;
        try {
            cacheField = clusterScope.getClass().getDeclaredField("localValidationScopes");
        } catch (NoSuchFieldException e) {
            return OptionalInt.empty();
        }
        cacheField.setAccessible(true);
        try {
            Object value = cacheField.get(clusterScope);
            if (value instanceof Map<?, ?> cache) {
                return OptionalInt.of(cache.size());
            }
            throw new AssertionError("Expected localValidationScopes to be a Map.");
        } catch (IllegalAccessException e) {
            throw new AssertionError("Unable to inspect localValidationScopes via reflection.", e);
        }
    }

    private static int count(Iterable<?> values) {
        int count = 0;
        for (Object ignored : values) {
            count++;
        }
        return count;
    }
}
