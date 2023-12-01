package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.ModelSymbolId;

import java.util.List;

public class TypeValidation extends ValidationData {

    private final ModelSymbolId typeID;

    private final List<ValidationResult> formatValidations;
    private final List<AttributeValidation> attributeValidations;
    private final List<ConditionValidation> conditionValidation;


    public TypeValidation(ModelSymbolId typeID, List<ValidationResult> formatValidations, List<AttributeValidation> attributeValidations, List<ConditionValidation> conditionValidation) {
        this.typeID = typeID;
        this.formatValidations = formatValidations;
        this.attributeValidations = attributeValidations;
        this.conditionValidation = conditionValidation;
    }

    public ModelSymbolId getTypeID() {
        return typeID;
    }

    public List<ValidationResult> getFormatValidations() {
        return formatValidations;
    }

    public List<AttributeValidation> getAttributeValidations() {
        return attributeValidations;
    }

    public List<ConditionValidation> getConditionValidation() {
        return conditionValidation;
    }
}
