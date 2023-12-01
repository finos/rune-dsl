package com.rosetta.model.lib.validation;

public class CardinalityValidationData extends ValidationData{

    private int lowerBound;
    private int upperBound;
    private int actual;

    public CardinalityValidationData(int lowerBound, int upperBound, int actual) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.actual = actual;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public int getActual() {
        return actual;
    }
}
