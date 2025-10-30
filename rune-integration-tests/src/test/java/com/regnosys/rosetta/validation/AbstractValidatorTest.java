package com.regnosys.rosetta.validation;

import java.util.List;

import javax.inject.Inject;

import com.regnosys.rosetta.tests.testmodel.RosettaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;

public class AbstractValidatorTest {
	@Inject
    private RosettaValidationTestHelper validationHelper;
    @Inject
    private RosettaTestModelService modelService;
	
    protected void assertIssues(String model, List<String> dependencies, String expectedIssues) {
		RosettaTestModel parsedModel = modelService.toTestModel(model, false, dependencies.toArray(new String[0]));
		validationHelper.assertIssues(parsedModel.getModel(), expectedIssues);
	}
	protected void assertIssues(String model, String expectedIssues) {
		assertIssues(model, List.of(), expectedIssues);
	}

    protected void assertNoIssues(String model, List<String> dependencies) {
        modelService.toTestModel(model, true, dependencies.toArray(new String[0]));
    }
	protected void assertNoIssues(String model) {
		assertNoIssues(model, List.of());
	}
}
