package com.rosetta.model.lib.validation;

import java.util.List;

public class ConditionValidation {
    private String conditionName;

    private ElementValidationResult validationResult;


    public ConditionValidation(String conditionName, ElementValidationResult validationResult) {
        this.conditionName = conditionName;
        this.validationResult = validationResult;
    }

    public String getConditionName() {
        return conditionName;
    }

    public ElementValidationResult getValidationResult() {
        return validationResult;
    }
}
