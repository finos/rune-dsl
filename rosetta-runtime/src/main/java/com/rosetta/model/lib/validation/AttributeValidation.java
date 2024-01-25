package com.rosetta.model.lib.validation;

import java.util.List;

public class AttributeValidation {
    private String attributeName;
    private ElementValidationResult cardinalityValidation;

    private List<ElementValidationResult> itemValidations;

    public AttributeValidation(String attributeName, ElementValidationResult cardinalityValidation, List<ElementValidationResult> itemValidations) {
        this.attributeName = attributeName;
        this.cardinalityValidation = cardinalityValidation;
        this.itemValidations = itemValidations;
    }

    public String getAttributeName() {
        return attributeName;
    }


    public ElementValidationResult getCardinalityValidation() {
        return cardinalityValidation;
    }

    public List<ElementValidationResult> getItemValidations() {
        return itemValidations;
    }
}
