package com.regnosys.rosetta.generator.java.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import javax.inject.Inject;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.tests.BrokenGeneratorTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(BrokenGeneratorTestInjectorProvider.class)
public class BrokenGeneratorTest {
    @Inject
    private CodeGeneratorTestHelper  generatorTestHelper;
    @Inject
    private ModelHelper modelHelper;

    @Test
    void testBrokenCodeGenerationErrorsPropogate() {
        var model = """
                type Foo:
                    aField string (1..1)
                """;
        
        RosettaModel parsedModel = modelHelper.parseRosettaWithNoErrors(model);
        
        generatorTestHelper.generateCode(parsedModel);
        
        Resource eResource = parsedModel.eResource();
        
        EList<Diagnostic> errors = eResource.getErrors();
        
        Diagnostic diagnostic = errors.get(0);
        
        System.out.println(diagnostic.getMessage());
        
        
        assertEquals(1, errors.size());
    }
}
