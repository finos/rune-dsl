package com.regnosys.rosetta.validation;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.validation.names.ClusterScope;
import com.regnosys.rosetta.validation.names.RosettaUniqueNamesConfig;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.ISelectable;
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
        EClass clusterType = SimplePackage.eINSTANCE.getAttribute();
        var clusterScope = uniqueNamesConfig.getDuplicationCluster(clusterType).clusterScope();

        var modelV1 = modelHelper.parseRosetta("""
                type Foo:
                    a int (1..1)
                """);
        validationTestHelper.assertNoIssues(modelV1);
        int cacheSizeAfterV1 = localValidationScopeCacheSize(clusterScope, modelV1.eResource(), clusterType);

        var modelV2 = modelHelper.parseRosetta("""
                type Bar:
                    b int (1..1)
                """);
        validationTestHelper.assertNoIssues(modelV2);
        int cacheSizeAfterV2 = localValidationScopeCacheSize(clusterScope, modelV2.eResource(), clusterType);

        Assertions.assertEquals(cacheSizeAfterV1, cacheSizeAfterV2,
                "Local validation-scope cache should not accumulate across independent validation contexts.");
        Assertions.assertEquals(1, cacheSizeAfterV1,
                "Expected exactly one local-scope cache entry for one attribute container.");
    }

    @Test
    void testLocalScopeStillFindsDuplicateAttributes() {
        var model = modelHelper.parseRosetta("""
                type Foo:
                    attr int (1..1)
                    attr int (1..1)
                """);
        EClass clusterType = SimplePackage.eINSTANCE.getAttribute();
        Function<IEObjectDescription, ISelectable> scopeFunction =
                uniqueNamesConfig.getDuplicationCluster(clusterType).clusterScope().getScope(model.eResource(), clusterType);

        IResourceDescription resourceDescription = resourceDescriptionManager.getResourceDescription(model.eResource());
        List<IEObjectDescription> attributes = new ArrayList<>();
        resourceDescription.getExportedObjectsByType(clusterType).forEach(attributes::add);
        Assertions.assertFalse(attributes.isEmpty(), "Expected exported attribute descriptions.");

        IEObjectDescription attribute = attributes.getFirst();
        var sameName = scopeFunction.apply(attribute).getExportedObjects(clusterType, attribute.getName(), false);
        Assertions.assertEquals(2, Iterables.size(sameName),
                "Local scope should still include both duplicate attributes.");
    }

    private int localValidationScopeCacheSize(Object clusterScope, Resource resource, EClass clusterType) {
        Function<IEObjectDescription, ISelectable> function =
                ((ClusterScope) clusterScope).getScope(resource, clusterType);
        IResourceDescription resourceDescription = resourceDescriptionManager.getResourceDescription(resource);
        List<IEObjectDescription> descriptions = new ArrayList<>();
        resourceDescription.getExportedObjectsByType(clusterType).forEach(descriptions::add);
        for (IEObjectDescription description : descriptions) {
            function.apply(description);
        }
        return reflectedCacheSize(function);
    }

    private static int reflectedCacheSize(Function<IEObjectDescription, ISelectable> scopedFunction) {
        List<Field> mapFields = new ArrayList<>();
        for (Field field : scopedFunction.getClass().getDeclaredFields()) {
            if (Map.class.isAssignableFrom(field.getType())) {
                mapFields.add(field);
            }
        }
        if (mapFields.isEmpty()) {
            throw new RuntimeException("Could not find captured localValidationScopes map on scope function.");
        }
        if (mapFields.size() > 1) {
            throw new RuntimeException("Expected a single captured map on scope function, found " + mapFields.size() + ".");
        }
        Field cacheField = mapFields.getFirst();
        cacheField.setAccessible(true);
        try {
            Object value = cacheField.get(scopedFunction);
            if (value instanceof Map<?, ?> cache) {
                return cache.size();
            }
            throw new RuntimeException("Expected captured localValidationScopes to be a Map.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to inspect localValidationScopes via reflection.", e);
        }
    }
}
