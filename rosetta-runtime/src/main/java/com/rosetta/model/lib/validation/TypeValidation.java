package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.ModelSymbolId;

import java.util.List;

public class TypeValidation extends ValidationData {

    private final ModelSymbolId typeId;

    private final List<AttributeValidation> attributeValidations;
    private final List<ConditionValidation> conditionValidation;


    public TypeValidation(ModelSymbolId typeId, List<AttributeValidation> attributeValidations, List<ConditionValidation> conditionValidation) {
        this.typeId = typeId;
        this.attributeValidations = attributeValidations;
        this.conditionValidation = conditionValidation;
    }

    public ModelSymbolId getTypeId() {
        return typeId;
    }

    public List<AttributeValidation> getAttributeValidations() {
        return attributeValidations;
    }

    public List<ConditionValidation> getConditionValidation() {
        return conditionValidation;
    }
}
