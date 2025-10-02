package com.rosetta.model.lib.validation;

import java.util.function.Function;

public enum ChoiceRuleValidationMethod {

    OPTIONAL("Zero or one field must be set", fieldCount -> fieldCount == 1 || fieldCount == 0),
    REQUIRED("One and only one field must be set", fieldCount -> fieldCount == 1);

    public String getDesc() {
        return desc;
    }

    private final String desc;
    private final Function<Integer, Boolean> check;

    ChoiceRuleValidationMethod(String desc, Function<Integer, Boolean> check) {
        this.desc = desc;
        this.check = check;
    }

    public boolean check(int fields) {
        return check.apply(fields);
    }

    public String getDescription() {
        return this.desc;
    }
}
