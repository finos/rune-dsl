package com.regnosys.rosetta.scoping;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.util.ModelHelper;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IResourceDescriptionsProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ImportedNamesTest {
    @Inject 
    private IResourceDescriptionsProvider index;
    @Inject
    private ModelHelper modelHelper;
    
    @Test
    void testImportedNamesDoNotContainNonExistingNamespaces() {
        String model1 = """
			namespace a
			
			func F:
				output: r string (1..1)
				set r: "foo"
			""";

        String model2 = """
			namespace b

			type X:
				x string (1..1)
   
			reporting rule R from X: a.F
			""";

        var models = modelHelper.parseRosettaWithNoIssues(model1, model2);
        var modelBRes = models.get(1).eResource();
        
        var importedNamesForModelB = index.getResourceDescriptions(modelBRes.getResourceSet()).getResourceDescription(modelBRes.getURI()).getImportedNames();
        
        var expected = List.of(
                qualifiedName("a.f"),
                qualifiedName("b.a.f"),
                qualifiedName("b.string"),
                qualifiedName("com.rosetta.model.a.f"),
                qualifiedName("com.rosetta.model.b.a.f"),
                qualifiedName("com.rosetta.model.b.string"),
                qualifiedName("com.rosetta.model.string")
        );
        
        var actual = Lists.newArrayList(importedNamesForModelB);
        Collections.sort(actual);
        Assertions.assertEquals(expected, actual);
    }
    
    private QualifiedName qualifiedName(String name) {
        return QualifiedName.create(name.split("\\."));
    }
}
