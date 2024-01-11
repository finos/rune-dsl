package com.rosetta.model.lib.validation;

import java.util.List;

public class AttributeValidation {
    private String attributeName;
    private ValidationResult cardinalityValidation;

    private List<ValidationResult> itemValidations;

    public AttributeValidation(String attributeName, ValidationResult cardinalityValidation, List<ValidationResult> itemValidations) {
        this.attributeName = attributeName;
        this.cardinalityValidation = cardinalityValidation;
        this.itemValidations = itemValidations;
    }

    public String getAttributeName() {
        return attributeName;
    }


    public ValidationResult getCardinalityValidation() {
        return cardinalityValidation;
    }

    public List<ValidationResult> getItemValidations() {
        return itemValidations;
    }
}
