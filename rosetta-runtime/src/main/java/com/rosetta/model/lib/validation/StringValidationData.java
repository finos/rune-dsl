package com.rosetta.model.lib.validation;

import java.util.regex.Pattern;

public class StringValidationData extends ValidationData{

    private int minLength;
    private int maxLength;
    private Pattern pattern;
    private String actual;

    public StringValidationData(int minLength, int maxLength, Pattern pattern, String actual) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.pattern = pattern;
        this.actual = actual;
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getActual() {
        return actual;
    }
}
