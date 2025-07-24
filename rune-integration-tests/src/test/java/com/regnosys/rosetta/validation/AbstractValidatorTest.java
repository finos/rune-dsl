package com.regnosys.rosetta.validation;

import javax.inject.Inject;

import com.regnosys.rosetta.tests.testmodel.RosettaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;

public class AbstractValidatorTest {
	@Inject
    private RosettaValidationTestHelper validationHelper;
    @Inject
    private RosettaTestModelService modelService;
	
	protected void assertIssues(String model, String expectedIssues) {
		RosettaTestModel parsedModel = modelService.toTestModel(model, false);
		validationHelper.assertIssues(parsedModel.getModel(), expectedIssues);
	}
	
	protected void assertNoIssues(String model) {
		modelService.toTestModel(model, true);
	}
}
