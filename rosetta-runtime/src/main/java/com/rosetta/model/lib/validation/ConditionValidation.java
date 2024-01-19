package com.rosetta.model.lib.validation;

import java.util.List;

public class ConditionValidation {
    private String conditionName;

    private ValidationResult validationResult;


    public ConditionValidation(String conditionName, ValidationResult validationResult) {
        this.conditionName = conditionName;
        this.validationResult = validationResult;
    }

    public String getConditionName() {
        return conditionName;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }
}
