package com.rosetta.model.lib.flatten;


import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.rosetta.model.lib.RosettaModelObject;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Creates instances of Rosetta model objects from Rosetta code.
 * This class uses the {@link RosettaTestModelService} to generate and compile
 * Java code.
 */
public class ModelInstanceCreator {

    @Inject
    private RosettaTestModelService modelService;

    /**
     * Creates a RosettaModelObject instance from the provided Rosetta code file.
     *
     * @param expression The Rune expression to create the model object.
     * @param rosettaFilePath The path to the Rosetta code file.
     * @return An instance of the RosettaModelObject defined in the Rosetta code.
     * @throws IOException if there are any errors during the loading of the Rosetta file.
     * @throws RuntimeException if there are any errors during code generation, compilation,
     *                          or instantiation of the model object.
     */
    public RosettaModelObject create(String expression, String rosettaFilePath) throws IOException {
    	JavaTestModel model = modelService.loadJavaTestModelFromResources(rosettaFilePath).compile();
    	return model.evaluateExpression(RosettaModelObject.class, expression);
    }
}