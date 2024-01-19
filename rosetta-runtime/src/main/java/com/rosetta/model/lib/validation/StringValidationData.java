package com.rosetta.model.lib.validation;

import java.util.Optional;
import java.util.regex.Pattern;

public class StringValidationData extends ValidationData{

    private int minLength;
    private int maxLength;
    private Optional<Pattern> pattern;
    private String actual;

    public StringValidationData(int minLength, int maxLength,  Optional<Pattern> pattern, String actual) {
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

    public  Optional<Pattern> getPattern() {
        return pattern;
    }

    public String getActual() {
        return actual;
    }
}
