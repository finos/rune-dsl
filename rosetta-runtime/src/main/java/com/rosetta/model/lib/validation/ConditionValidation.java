package com.rosetta.model.lib.validation;

import java.util.List;

public class ConditionValidation {
    private String attributeName;
    private String modelURI;

    private ValidationResult cardinalityValidation;

    private List<ValidationResult> itemValidations;

    public ConditionValidation(String attributeName, String modelURI, ValidationResult cardinalityValidation, List<ValidationResult> itemValidations) {
        this.attributeName = attributeName;
        this.modelURI = modelURI;
        this.cardinalityValidation = cardinalityValidation;
        this.itemValidations = itemValidations;
    }
}
